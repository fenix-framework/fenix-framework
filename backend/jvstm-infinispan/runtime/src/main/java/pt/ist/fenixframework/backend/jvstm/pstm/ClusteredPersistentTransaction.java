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

import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.backend.jvstm.cluster.ClusterUtils;
import pt.ist.fenixframework.backend.jvstm.cluster.RemoteCommit;
import pt.ist.fenixframework.backend.jvstm.cluster.RemoteCommit.SpeculativeRemoteCommit;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class ClusteredPersistentTransaction extends PersistentTransaction {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredPersistentTransaction.class);
    private SpeculativeRemoteCommit speculativeRemoteCommit;

    public ClusteredPersistentTransaction(ActiveTransactionsRecord record) {
        super(record);
        ActiveTransactionsRecord newRecord = tryToApplyRemoteCommits(record);
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
        if (isWriteTransaction() && this.perTxValues.isEmpty()) {
            makeSpeculativeRemoteCommit();
        }
        super.tryCommit();
    }

    private void makeSpeculativeRemoteCommit() {
        this.speculativeRemoteCommit = new SpeculativeRemoteCommit(DomainClassInfo.getServerId(), this.boxesWritten);
    }

    @Override
    protected boolean validateCommit() {
        applyRemoteCommits(this.activeTxRecord);
        return super.validateCommit();
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        int mostRecentGlobalTxNum = ClusterUtils.globalLock();

        boolean commitSuccess = false;
        try {

            // re-validate, if needed, after updating local data
            ActiveTransactionsRecord myRecord = this.activeTxRecord;
            if (myRecord.transactionNumber != mostRecentGlobalTxNum) {
                updateToMostRecentCommit(myRecord, mostRecentGlobalTxNum);

                // the cache may have been updated, so validate again
                logger.debug("Need to re-validate tx due to remote commits");
                if (!validateCommit()) { // we may use a variation here, because we know we don't need to recheck the queue 
                    logger.warn("Invalid commit. Restarting.");
                    throw new jvstm.CommitException();
                }

                // sanity check. to remove later
                if (this.activeTxRecord.transactionNumber != mostRecentGlobalTxNum) {
                    logger.error("this was not supposed to happen");
                    System.exit(-1);
                    throw new Error("not supposed to happen");
                }

            }

//            if (updateToMostRecentCommit(myRecord, mostRecentGlobalTxNum) != myRecord) {
//                // the cache may have been updated, so validate again
//                logger.debug("Need to re-validate tx due to remote commits");
//                if (!validateCommit()) {
//                    logger.warn("Invalid commit. Restarting.");
//                    throw new jvstm.CommitException();
//                }
//            }

            Cons<VBoxBody> temp = super.performValidCommit();

            if (this.speculativeRemoteCommit != null) {
                this.speculativeRemoteCommit.setTxNumber(this.getNumber());
                logger.debug("Sending remote commit created before lock");
                ClusterUtils.sendCommitInfoToOthers(this.speculativeRemoteCommit);
            } else {
                logger.debug("Creating remote commit (within lock) to send others");
                ClusterUtils.sendCommitInfoToOthers(new RemoteCommit(DomainClassInfo.getServerId(), this.getNumber(),
                        this.boxesWritten));
            }
            commitSuccess = true;

            return temp;
        } finally {
            // this tx number has been updated after performing the valid commit
            ClusterUtils.globalUnlock((commitSuccess ? this.getNumber() : mostRecentGlobalTxNum));
        }
    }

    /* this is a debug feature. This counter should only increase (although it
    may skip some numbers, because local commits are not enqueued). If the order
    of remote commits may become skewed then it's because the thread that processed
    the messages is being allowed to execute after the global global lock has been
    released */
    private static int debug_hazelcast_last_commit_seen = Transaction.getMostRecentCommitedNumber();

    /* this method only returns after having applied all remote commits up until
    the mostRecentGlobalTxNum. This way we ensure that no earlier remote commit
    is missing, which would cause us to commit a wrong tx version */
    public static ActiveTransactionsRecord updateToMostRecentCommit(ActiveTransactionsRecord currentCommitRecord,
            int mostRecentGlobalTxNum) {
        logger.debug("Must apply commits from {} up to {}", currentCommitRecord.transactionNumber, mostRecentGlobalTxNum);

        while (currentCommitRecord.transactionNumber < mostRecentGlobalTxNum) {
            ActiveTransactionsRecord newCommitRecord = applyRemoteCommits(currentCommitRecord);
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
                logger.debug("Processed remote commits up to {}", newCommitRecord.transactionNumber);
            }
            currentCommitRecord = newCommitRecord;
        }

        return currentCommitRecord;
    }

    /* this method tries to apply as many remote commits as it finds in the queue. if it fails to get the lock it returns without doing anything */
    public static ActiveTransactionsRecord tryToApplyRemoteCommits(ActiveTransactionsRecord record) {
        logger.debug("Try to apply remote commits if any.");
        // avoid locking if queue is empty
        if (ClusterUtils.getRemoteCommits().isEmpty()) {
//            logger.debug("No remote commits to apply. Great.");
            /* we need to always return the most recent committed number:

            - if inside a commit (already holding the commit lock), this ensures
            that we're able to detect record advances caused by the processing
            of the remote commits queue (e.g. while we were waiting to acquire
            the global lock) 
            
            - if starting a new transaction, it attempts to improve on the version
            we see (here it would be acceptable to just return the record had)
            */
            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
        }

//        COMMIT_LOCK.lock(); // change to tryLock to allow the starting transactions to begin without waiting for a long commit (which in fact may already have processed the queue :-))  
        if (COMMIT_LOCK.tryLock()) {
            try {
                return applyRemoteCommits(record);
            } finally {
                COMMIT_LOCK.unlock();
            }
        } else {
            return findActiveRecordForNumber(record, Transaction.getMostRecentCommitedNumber());
        }
    }

    // must be called while holding the local commit lock
    private static ActiveTransactionsRecord applyRemoteCommits(ActiveTransactionsRecord record) {
        int currentCommittedNumber = Transaction.getMostRecentCommitedNumber();

        RemoteCommit remoteCommit;
        while ((remoteCommit = ClusterUtils.getRemoteCommits().poll()) != null) {
            int txNum = remoteCommit.getTxNumber();

            /* this may occur only when booting.  It happens when remote
            commit messages arrive between the time the topic channel is
            established, but the most recent committed version hasn't been
            initialized yet.  When it gets initialized, it is typically to
            a greater value than the one from the txs that are enqueued. */
            if (txNum <= currentCommittedNumber) {
                logger.info("Ignoring outdated remote commit txNum={} <= mostRecentNum={}.", txNum, currentCommittedNumber);
                continue;
            }

            if (txNum <= debug_hazelcast_last_commit_seen) {
                logger.error("The remote commit has a number({}) <= last_seen({})", txNum, debug_hazelcast_last_commit_seen);
                System.exit(-1);
                throw new Error("Inconsistent remote commit. This should not happen");
            } else {
                logger.debug("remove me :-)");
                debug_hazelcast_last_commit_seen = txNum;
            }
            applyRemoteCommit(remoteCommit);
            currentCommittedNumber = remoteCommit.getTxNumber();
        }

        return findActiveRecordForNumber(record, currentCommittedNumber);
    }

    // within commit lock
    private static void applyRemoteCommit(RemoteCommit remoteCommit) {
        int serverId = remoteCommit.getServerId();
        int txNumber = remoteCommit.getTxNumber();

        logger.debug("Applying remote commit: serverId={}, txNumber={}", serverId, txNumber);

        Cons<VBoxBody> newBodies = Cons.empty();

        int size = remoteCommit.getOids().length;
        for (int i = 0; i < size; i++) {
            long oid = remoteCommit.getOids()[i];
            String slotName = remoteCommit.getSlotNames()[i];

            JVSTMDomainObject obj = (JVSTMDomainObject) SharedIdentityMap.getCache().lookup(oid);

            /* if the domain object is not cached, we don't need to update its
            slots. If a concurrent access to this objects causes it to be allocated
            and its slots reloaded, the most recent values will be fetched from
            the repository */
            if (obj != null) {
                VBoxBody newBody = obj.addNewVersion(slotName, txNumber);
                if (newBody != null) {
                    newBodies = newBodies.cons(newBody);
                }
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
