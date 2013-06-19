/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;
import jvstm.VBoxBody;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.lf.LockFreeClusterUtils;
import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeBackEnd;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest.SpeculativeCommitRequest;

public class LockFreeTransaction extends PersistentTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeTransaction.class);
    private SpeculativeCommitRequest speculativeCommitRequest;

    public LockFreeTransaction(ActiveTransactionsRecord record) {
        super(record);
        ActiveTransactionsRecord newRecord = tryToApplyCommitRequests(record);
        if (newRecord != this.activeTxRecord) {
            // if a new record is returned, that means that this transaction
            // will belong
            // to that new record, so we must take it off from its current
            // record and set
            // it properly

            newRecord.incrementRunning();
            this.activeTxRecord.decrementRunning();
            this.activeTxRecord = newRecord;
            setNumber(newRecord.transactionNumber);
        }
    }

    @Override
    protected void tryCommit() {
        if ((JvstmLockFreeBackEnd.getInstance().getNumMembers() > 1) && isWriteTransaction() && this.perTxValues.isEmpty()) {
            makeSpeculativeCommitRequest();
        }
        super.tryCommit();
    }

    private void makeSpeculativeCommitRequest() {
        this.speculativeCommitRequest = new SpeculativeCommitRequest(DomainClassInfo.getServerId(), this.boxesWritten);
    }

    @Override
    protected boolean validateCommit() {
        applyCommitRequests(this.activeTxRecord);
        return super.validateCommit();
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        int mostRecentGlobalTxNum = LockFreeClusterUtils.globalLock();

        boolean commitSuccess = false;
        try {

            // re-validate, if needed, after updating local data
            ActiveTransactionsRecord myRecord = this.activeTxRecord;
            if (myRecord.transactionNumber != mostRecentGlobalTxNum) {
                updateToMostRecentCommit(myRecord, mostRecentGlobalTxNum);

                // the cache may have been updated, so validate again
                logger.debug("Need to re-validate tx due to commit requests");
                if (!validateCommit()) { // we may use a variation here, because we know we don't need to recheck the queue 
                    logger.warn("Invalid commit. Restarting.");
                    throw new jvstm.CommitException();
                }

                assert (this.activeTxRecord.transactionNumber == mostRecentGlobalTxNum);

            }

            Cons<VBoxBody> temp = super.performValidCommit();

            /* It is safe to test the number of elements even if  */
            if (this.speculativeCommitRequest != null) {
                this.speculativeCommitRequest.setTxNumber(this.getNumber());
                logger.debug("Sending commit request created before lock");
                LockFreeClusterUtils.sendCommitInfoToOthers(this.speculativeCommitRequest);
            } else if (JvstmLockFreeBackEnd.getInstance().getNumMembers() > 1) {
                /* Only send the commit request if there is at least other member
                in the cluster.  This test is safe: Even if another member joins
                after the test detects only one member, then such new member
                will not loose this commit, because it is already written to the
                repository (and members register as commit request listeners
                **before** reading the last committed tx from the repository.*/

                logger.debug("Creating commit request (within lock) to send others");
                LockFreeClusterUtils.sendCommitInfoToOthers(new CommitRequest(DomainClassInfo.getServerId(), this.getNumber(),
                        this.boxesWritten));
            }
            commitSuccess = true;

            return temp;
        } finally {
            // this tx number has been updated after performing the valid commit
            LockFreeClusterUtils.globalUnlock((commitSuccess ? this.getNumber() : mostRecentGlobalTxNum));
        }
    }

    /* this is a debug feature. This counter should only increase (although it
    may skip some numbers, because local commits are not enqueued). If the order
    of commit requests may become skewed then it's because the thread that processed
    the messages is being allowed to execute after the global global lock has been
    released */
    private static int debug_hazelcast_last_commit_seen = Transaction.getMostRecentCommitedNumber();

    /* this method only returns after having applied all commit requests up until
    the mostRecentGlobalTxNum. This way we ensure that no earlier commit request
    is missing, which would cause us to commit a wrong tx version */
    public static ActiveTransactionsRecord updateToMostRecentCommit(ActiveTransactionsRecord currentCommitRecord,
            int mostRecentGlobalTxNum) {
        logger.debug("Must apply commits from {} up to {}", currentCommitRecord.transactionNumber, mostRecentGlobalTxNum);

        while (currentCommitRecord.transactionNumber < mostRecentGlobalTxNum) {
            ActiveTransactionsRecord newCommitRecord = applyCommitRequests(currentCommitRecord);
            if (newCommitRecord == currentCommitRecord) {
                logger.debug("There was nothing yet to process");
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
                Thread.yield();
            } else {
                logger.debug("Processed commit requests up to {}", newCommitRecord.transactionNumber);
            }
            currentCommitRecord = newCommitRecord;
        }

        return currentCommitRecord;
    }

    /* this method tries to apply as many commit requests as it finds in the queue. if it fails to get the lock it returns without doing anything */
    public static ActiveTransactionsRecord tryToApplyCommitRequests(ActiveTransactionsRecord record) {
        logger.debug("Try to apply commit requests if any.");
        // avoid locking if queue is empty
        if (LockFreeClusterUtils.getCommitRequests().isEmpty()) {
//            logger.debug("No commit requests to apply. Great.");
            /* we need to always return the most recent committed number:

            - if inside a commit (already holding the commit lock), this ensures
            that we're able to detect record advances caused by the processing
            of the commit requests queue (e.g. while we were waiting to acquire
            the global lock) 
            
            - if starting a new transaction, it attempts to improve on the version
            we see (here it would be acceptable to just return the record had)
            */
            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
        }

//        COMMIT_LOCK.lock(); // change to tryLock to allow the starting transactions to begin without waiting for a long commit (which in fact may already have processed the queue :-))  
        if (COMMIT_LOCK.tryLock()) {
            try {
                return applyCommitRequests(record);
            } finally {
                COMMIT_LOCK.unlock();
            }
        } else {
            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
        }
    }

    // must be called while holding the local commit lock
    private static ActiveTransactionsRecord applyCommitRequests(ActiveTransactionsRecord record) {
        int currentCommittedNumber = Transaction.getMostRecentCommitedNumber();

        CommitRequest commitRequest;
        while ((commitRequest = LockFreeClusterUtils.getCommitRequests().poll()) != null) {
            int txNum = commitRequest.getTxNumber();

            /* this may occur only when booting.  It happens when commit
            request messages arrive between the time the topic channel is
            established, but the most recent committed version hasn't been
            initialized yet.  When it gets initialized, it is typically to
            a greater value than the one from the txs that are enqueued. */
            if (txNum <= currentCommittedNumber) {
                logger.info("Ignoring outdated commit request txNum={} <= mostRecentNum={}.", txNum, currentCommittedNumber);
                continue;
            }

            if (txNum <= debug_hazelcast_last_commit_seen) {
                logger.error("The commit request has a number({}) <= last_seen({})", txNum, debug_hazelcast_last_commit_seen);
                System.exit(-1);
                throw new Error("Inconsistent commit request. This should not happen");
            } else {
                logger.debug("remove me :-)");
                debug_hazelcast_last_commit_seen = txNum;
            }
            applyCommitRequest(commitRequest);
            currentCommittedNumber = commitRequest.getTxNumber();
        }

        return findActiveRecordForNumber(record, currentCommittedNumber);
    }

    // within commit lock
    private static void applyCommitRequest(CommitRequest commitRequest) {
        int serverId = commitRequest.getServerId();
        int txNumber = commitRequest.getTxNumber();

        logger.debug("Applying commit request: serverId={}, txNumber={}", serverId, txNumber);

        Cons<VBoxBody> newBodies = Cons.empty();

        int size = commitRequest.getIds().length;
        for (int i = 0; i < size; i++) {
            String vboxId = commitRequest.getIds()[i];

            JvstmLockFreeBackEnd backEnd = (JvstmLockFreeBackEnd) FenixFramework.getConfig().getBackEnd();

            VBox vbox = backEnd.lookupCachedVBox(vboxId);

            /* if the vbox is not found (not cached or reachable from a domain object), we don't need to update its
            slots. If a concurrent access to this objects causes it to be allocated
            and its slots reloaded, the most recent values will be fetched from
            the repository */
            if (vbox != null) {
                VBoxBody newBody = vbox.addNewVersion(txNumber);
                if (newBody != null) {
                    newBodies = newBodies.cons(newBody);
                }
            } else {
                logger.debug("Ignoring commit request for vbox not found in local memory: {}", vboxId);
            }
        }

        ActiveTransactionsRecord newRecord = new ActiveTransactionsRecord(txNumber, newBodies);
        Transaction.setMostRecentActiveRecord(newRecord);
    }

    private static ActiveTransactionsRecord findActiveRecordForNumber(ActiveTransactionsRecord rec, int number) {
        while (rec.transactionNumber < number) {
            rec = rec.getNext();
        }

        return rec;
    }

}
