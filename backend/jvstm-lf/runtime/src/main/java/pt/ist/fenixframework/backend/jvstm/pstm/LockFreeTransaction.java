/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import jvstm.ActiveTransactionsRecord;
import jvstm.CommitException;
import jvstm.TransactionSignaller;
import jvstm.cps.ConsistentTopLevelTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest.ValidationStatus;
import pt.ist.fenixframework.backend.jvstm.lf.LockFreeClusterUtils;
import pt.ist.fenixframework.backend.jvstm.lf.RemoteReadSet;
import pt.ist.fenixframework.backend.jvstm.lf.RemoteWriteSet;

public class LockFreeTransaction extends ConsistentTopLevelTransaction implements StatisticsCapableTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeTransaction.class);

    private boolean readOnly = false;

    // for statistics
    protected int numBoxReads = 0;
    protected int numBoxWrites = 0;

    public LockFreeTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    public void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public boolean txAllowsWrite() {
        return !this.readOnly;
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        return super.getBoxValue(vbox);
    }

    @Override
    public boolean isBoxValueLoaded(VBox vbox) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public int getNumBoxReads() {
        return numBoxReads;
    }

    @Override
    public int getNumBoxWrites() {
        return numBoxWrites;
    }

    /* This is the main entrance point for the lock-free commit. We override
    tryCommit, and we do not call super.trycommit().  We reuse the commitTx
    machinery in LocalCommitOnlyTransaction, which is the instance that we create
    to decorate DistributedTransactions from the local node.  In short, here we
    just broadcast a commit request and go process the queue until our
    LocalCommitOnlyTransction is either committed or found invalid. */
    @Override
    protected void tryCommit() {
        if (isWriteTransaction()) {

            if (this.perTxValues != EMPTY_MAP) {
                logger.error("PerTxValues are not supported in distributed transactions yet.");
                TransactionSignaller.SIGNALLER.signalCommitFail();
                throw new AssertionError("Impossible condition - Commit fail signalled!");
            }

// From ConsistentTopLevelTransaction:
            alreadyChecked = new HashSet();
            checkConsistencyPredicates();
            alreadyChecked = null; // allow gc of set

// From TopLevelTransaction:
//            validate();
//            ensureCommitStatus();
// replaced with:
            helpedTryCommit();

// From TopLevelTransaction:
            upgradeTx(getCommitTxRecord());  // it was set by the helper LocalCommitOnlyTransaction 
        }
    }

    protected void helpedTryCommit() throws CommitException {
        // smf TODO IMPORTANT: we may consider a local validation before broadcasting the commit request!

        CommitRequest myRequest = makeCommitRequest();

        // start by reading the current commit queue's head.  This is to ensure that we don't miss our own commit request
        CommitRequest currentRequest = LockFreeClusterUtils.getCommitRequestAtHead();

        UUID myRequestId = broadcastCommitRequest(myRequest);

        // the myRequest instance is different, because it was serialized and deserialized. So, just use its id.
        tryCommit(currentRequest, myRequestId);
    }

    private UUID broadcastCommitRequest(CommitRequest commitRequest) {
        // for later recovering this transaction
        CommitOnlyTransaction.commitsMap.put(commitRequest.getId(), this);

        LockFreeClusterUtils.sendCommitRequest(commitRequest);
        return commitRequest.getId();
    }

    private CommitRequest makeCommitRequest() {
        return new CommitRequest(DomainClassInfo.getServerId(), getNumber(), makeRemoteReadSet(), makeRemoteWriteSet());
    }

    private RemoteReadSet makeRemoteReadSet() {
        HashSet<String> vboxIds = new HashSet<String>();

        if (!this.bodiesRead.isEmpty()) {
            // the first may not be full
            jvstm.VBox[] array = this.bodiesRead.first();
            for (int i = next + 1; i < array.length; i++) {
                String vboxId = ((VBox) array[i]).getId();
                vboxIds.add(vboxId);
            }

            // the rest are full
            for (jvstm.VBox[] ar : bodiesRead.rest()) {
                for (int i = 0; i < ar.length; i++) {
                    String vboxId = ((VBox) array[i]).getId();
                    vboxIds.add(vboxId);
                }
            }
        }

        return new RemoteReadSet(vboxIds.toArray(new String[vboxIds.size()]));
    }

    private RemoteWriteSet makeRemoteWriteSet() {
        // code adapted from jvstm.WriteSet. It's a bit redundant, and cumbersome to maintain if the original code happens to change :-/  This should be refactored

        // CODE TO DEAL WITH PARALLEL NESTED TRANSACTIONS WAS REMOVED FROM THE ORIGINAL VERSION

        int maxRequiredSize = this.boxesWrittenInPlace.size() + this.boxesWritten.size();

        String[] vboxIds = new String[maxRequiredSize];
        Object[] values = new Object[maxRequiredSize];
        int pos = 0;

        // Deal with VBoxes written in place
        for (jvstm.VBox vbox : this.boxesWrittenInPlace) {
            vboxIds[pos] = ((VBox) vbox).getId();
            values[pos++] = vbox.getInplace().tempValue;
            vbox.getInplace().next = null;
        }

        // Deal with VBoxes written in the fallback write-set
        for (Map.Entry<jvstm.VBox, Object> entry : boxesWritten.entrySet()) {
            jvstm.VBox vbox = entry.getKey();
            if (vbox.getInplace().orec.owner == this) {
                // if we also wrote directly to the box, we just skip this value
                continue;
            }
            vboxIds[pos] = ((VBox) vbox).getId();
            values[pos++] = entry.getValue();
        }
        int writeSetLength = pos;

        return new RemoteWriteSet(vboxIds, values, writeSetLength);
    }

    /**
     * Try to commit everything up to (and including) myRequestId.
     * 
     * @param requestToProcess Head of the remote commits queue
     * @param myRequestId My commit request's ID
     * @return The {@link CommitRequest} that corresponds to myRequestId
     * @throws CommitException if the validation of my request failed
     */
    private CommitRequest tryCommit(CommitRequest requestToProcess, UUID myRequestId) throws CommitException {
        // get a transaction and invoke its commit.

        CommitRequest current = requestToProcess;

        while (true) {
            if (logger.isDebugEnabled()) {
                logger.debug("Will handle commit request: {}", current);
            }

            CommitRequest next = current.handle();
            if (current.getId().equals(myRequestId)) {
                logger.debug("Processed my commit request: {}. ValidationStatus: {}", myRequestId.toString(),
                        current.getValidationStatus());

                boolean valid = current.getValidationStatus() == ValidationStatus.VALID;
                if (!valid) {
                    TransactionSignaller.SIGNALLER.signalCommitFail();
                    throw new AssertionError("Impossible condition - Commit fail signalled!");
                }

                // we know that a valid commit request was written back. we're done
                return current;
            }

            if (next == null) {
                /* Next is null, so we know that there is nothing more for now.
                We just wait for 'next' to arrive, because we still need to
                handle our own request.  Note that the algorithm is lock-free
                under the assumption that delivery of our own request to our own
                node is guaranteed to occur! */
                logger.debug("Waiting for my own commit request to arrive to the queue.");
                continue;
            } else {
                current = next;
            }
        }
    }

//// LOCK-BASED STUFF BELOW
//// LOCK-BASED STUFF BELOW
//// LOCK-BASED STUFF BELOW
//// LOCK-BASED STUFF BELOW
//// LOCK-BASED STUFF BELOW
//// LOCK-BASED STUFF BELOW

//    @Override
//    protected void tryCommit() {
//        if ((JvstmLockFreeBackEnd.getInstance().getNumMembers() > 1) && isWriteTransaction() && this.perTxValues.isEmpty()) {
//            makeSpeculativeCommitRequest();
//        }
//        super.tryCommit();
//    }
//
//    private void makeSpeculativeCommitRequest() {
//        this.speculativeCommitRequest = new SpeculativeCommitRequest(DomainClassInfo.getServerId(), this.boxesWritten);
//    }
//
//    @Override
//    protected boolean validateCommit() {
//        applyCommitRequests(this.activeTxRecord);
//        return super.validateCommit();
//    }
//
//    @Override
//    protected Cons<VBoxBody> performValidCommit() {
//        int mostRecentGlobalTxNum = LockFreeClusterUtils.globalLock();
//
//        boolean commitSuccess = false;
//        try {
//
//            // re-validate, if needed, after updating local data
//            ActiveTransactionsRecord myRecord = this.activeTxRecord;
//            if (myRecord.transactionNumber != mostRecentGlobalTxNum) {
//                updateToMostRecentCommit(myRecord, mostRecentGlobalTxNum);
//
//                // the cache may have been updated, so validate again
//                logger.debug("Need to re-validate tx due to commit requests");
//                if (!validateCommit()) { // we may use a variation here, because we know we don't need to recheck the queue 
//                    logger.warn("Invalid commit. Restarting.");
//                    TransactionUtils.signalCommitFail();
//                    throw new AssertionError("Impossible condition - Commit fail signalled!");
//                }
//
//                assert (this.activeTxRecord.transactionNumber == mostRecentGlobalTxNum);
//
//            }
//
//            Cons<VBoxBody> temp = super.performValidCommit();
//
//            /* It is safe to test the number of elements even if  */
//            if (this.speculativeCommitRequest != null) {
//                this.speculativeCommitRequest.setTxNumber(this.getNumber());
//                logger.debug("Sending commit request created before lock");
//                LockFreeClusterUtils.sendCommitInfoToOthers(this.speculativeCommitRequest);
//            } else if (JvstmLockFreeBackEnd.getInstance().getNumMembers() > 1) {
//                /* Only send the commit request if there is at least other member
//                in the cluster.  This test is safe: Even if another member joins
//                after the test detects only one member, then such new member
//                will not loose this commit, because it is already written to the
//                repository (and members register as commit request listeners
//                **before** reading the last committed tx from the repository.*/
//
//                logger.debug("Creating commit request (within lock) to send others");
//                LockFreeClusterUtils.sendCommitInfoToOthers(new OldCommitRequest(DomainClassInfo.getServerId(), this.getNumber(),
//                        this.boxesWritten));
//            }
//            commitSuccess = true;
//
//            return temp;
//        } finally {
//            // this tx number has been updated after performing the valid commit
//            LockFreeClusterUtils.globalUnlock((commitSuccess ? this.getNumber() : mostRecentGlobalTxNum));
//        }
//    }
//
//    /* this is a debug feature. This counter should only increase (although it
//    may skip some numbers, because local commits are not enqueued). If the order
//    of commit requests may become skewed then it's because the thread that processed
//    the messages is being allowed to execute after the global global lock has been
//    released */
//    private static int debug_hazelcast_last_commit_seen = Transaction.getMostRecentCommitedNumber();
//
//    /* this method only returns after having applied all commit requests up until
//    the mostRecentGlobalTxNum. This way we ensure that no earlier commit request
//    is missing, which would cause us to commit a wrong tx version */
//    public static ActiveTransactionsRecord updateToMostRecentCommit(ActiveTransactionsRecord currentCommitRecord,
//            int mostRecentGlobalTxNum) {
//        logger.debug("Must apply commits from {} up to {}", currentCommitRecord.transactionNumber, mostRecentGlobalTxNum);
//
//        while (currentCommitRecord.transactionNumber < mostRecentGlobalTxNum) {
//            ActiveTransactionsRecord newCommitRecord = applyCommitRequests(currentCommitRecord);
//            if (newCommitRecord == currentCommitRecord) {
//                logger.debug("There was nothing yet to process");
////                try {
////                    Thread.sleep(3000);
////                } catch (InterruptedException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
//                Thread.yield();
//            } else {
//                logger.debug("Processed commit requests up to {}", newCommitRecord.transactionNumber);
//            }
//            currentCommitRecord = newCommitRecord;
//        }
//
//        return currentCommitRecord;
//    }
//
//    /* this method tries to apply as many commit requests as it finds in the queue. if it fails to get the lock it returns without doing anything */
//    public static ActiveTransactionsRecord tryToApplyCommitRequests(ActiveTransactionsRecord record) {
//        logger.debug("Try to apply commit requests if any.");
//        // avoid locking if queue is empty
//        if (LockFreeClusterUtils.getCommitRequests().isEmpty()) {
////            logger.debug("No commit requests to apply. Great.");
//            /* we need to always return the most recent committed number:
//
//            - if inside a commit (already holding the commit lock), this ensures
//            that we're able to detect record advances caused by the processing
//            of the commit requests queue (e.g. while we were waiting to acquire
//            the global lock) 
//            
//            - if starting a new transaction, it attempts to improve on the version
//            we see (here it would be acceptable to just return the record had)
//            */
//            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
//        }
//
////        COMMIT_LOCK.lock(); // change to tryLock to allow the starting transactions to begin without waiting for a long commit (which in fact may already have processed the queue :-))  
//        if (COMMIT_LOCK.tryLock()) {
//            try {
//                return applyCommitRequests(record);
//            } finally {
//                COMMIT_LOCK.unlock();
//            }
//        } else {
//            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
//        }
//    }
//
//    // must be called while holding the local commit lock
//    private static ActiveTransactionsRecord applyCommitRequests(ActiveTransactionsRecord record) {
//        int currentCommittedNumber = Transaction.getMostRecentCommitedNumber();
//
//        OldCommitRequest commitRequest;
//        while ((commitRequest = LockFreeClusterUtils.getCommitRequests().poll()) != null) {
//            int txNum = commitRequest.getTxNumber();
//
//            /* this may occur only when booting.  It happens when commit
//            request messages arrive between the time the topic channel is
//            established, but the most recent committed version hasn't been
//            initialized yet.  When it gets initialized, it is typically to
//            a greater value than the one from the txs that are enqueued. */
//            if (txNum <= currentCommittedNumber) {
//                logger.info("Ignoring outdated commit request txNum={} <= mostRecentNum={}.", txNum, currentCommittedNumber);
//                continue;
//            }
//
//            if (txNum <= debug_hazelcast_last_commit_seen) {
//                logger.error("The commit request has a number({}) <= last_seen({})", txNum, debug_hazelcast_last_commit_seen);
//                System.exit(-1);
//                throw new Error("Inconsistent commit request. This should not happen");
//            } else {
//                logger.debug("remove me :-)");
//                debug_hazelcast_last_commit_seen = txNum;
//            }
//            applyCommitRequest(commitRequest);
//            currentCommittedNumber = commitRequest.getTxNumber();
//        }
//
//        return findActiveRecordForNumber(record, currentCommittedNumber);
//    }
//
//    // within commit lock
//    private static void applyCommitRequest(OldCommitRequest commitRequest) {
//        int serverId = commitRequest.getServerId();
//        int txNumber = commitRequest.getTxNumber();
//
//        logger.debug("Applying commit request: serverId={}, txNumber={}", serverId, txNumber);
//
//        Cons<VBoxBody> newBodies = Cons.empty();
//
//        int size = commitRequest.getIds().length;
//        for (int i = 0; i < size; i++) {
//            String vboxId = commitRequest.getIds()[i];
//
//            JvstmLockFreeBackEnd backEnd = (JvstmLockFreeBackEnd) FenixFramework.getConfig().getBackEnd();
//
//            VBox vbox = backEnd.lookupCachedVBox(vboxId);
//
//            /* if the vbox is not found (not cached or reachable from a domain object), we don't need to update its
//            slots. If a concurrent access to this objects causes it to be allocated
//            and its slots reloaded, the most recent values will be fetched from
//            the repository */
//            if (vbox != null) {
//                VBoxBody newBody = vbox.addNewVersion(txNumber);
//                if (newBody != null) {
//                    newBodies = newBodies.cons(newBody);
//                }
//            } else {
//                logger.debug("Ignoring commit request for vbox not found in local memory: {}", vboxId);
//            }
//        }
//
//        ActiveTransactionsRecord newRecord = new ActiveTransactionsRecord(txNumber, newBodies);
//        Transaction.setMostRecentActiveRecord(newRecord);
//    }
//
//    private static ActiveTransactionsRecord findActiveRecordForNumber(ActiveTransactionsRecord rec, int number) {
//        while (rec.transactionNumber < number) {
//            rec = rec.getNext();
//        }
//
//        return rec;
//    }

}
