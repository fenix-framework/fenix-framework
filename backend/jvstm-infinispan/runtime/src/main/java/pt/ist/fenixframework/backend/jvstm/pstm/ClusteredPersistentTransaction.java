/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.concurrent.locks.Lock;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;
import jvstm.VBoxBody;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.backend.jvstm.cluster.ClusterUtils;
import pt.ist.fenixframework.backend.jvstm.cluster.RemoteCommit;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class ClusteredPersistentTransaction extends PersistentTransaction {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredPersistentTransaction.class);

    public ClusteredPersistentTransaction(ActiveTransactionsRecord record) {
        super(record);
        ActiveTransactionsRecord newRecord = applyRemoteCommits(record);
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

    public static Lock getCommitlock() {
        return COMMIT_LOCK;
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        logger.debug("Will get global cluster lock...");
        ClusterUtils.globalLock();
        logger.debug("Acquired global cluster lock");
        try {

            // update local data and re-validate, if needed
            ActiveTransactionsRecord myRecord = this.activeTxRecord;
            if (applyRemoteCommits(this.activeTxRecord) != myRecord) {
                // the cache may have been updated, so validate again
                logger.debug("Need to re-validate tx due to remote commits");
                if (!validateCommit()) {
                    logger.warn("Invalid commit. Restarting.");
                    throw new jvstm.CommitException();
                }
            }

            Cons<VBoxBody> temp = super.performValidCommit();
            ClusterUtils.sendCommitInfoToOthers(new RemoteCommit(DomainClassInfo.getServerId(), this.getNumber(),
                    this.boxesWritten));
            return temp;
        } finally {
            logger.debug("Will release global cluster lock");
            ClusterUtils.globalUnlock();
        }
    }

    /* this is a debug feature. This counter should only increase (although it
    may skip some numbers, because local commits are not enqueued). If the order
    of remote commits may become skewed then it's because the thread that processed
    the messages is being allowed to execute after the global global lock has been
    set free */
    private static int debug_hazelcast_last_commit_seen = Transaction.getMostRecentCommitedNumber();

    /*
    1. Synchronized on local commit lock? not needed assuming that:

    - there is a single global commit lock

    - commit clears the remote commits queue (synchronized on the
    REMOTE_COMMITS)
        
    So, while a commit is writing back, it is not possible for elements to
    appear in REMOTE_COMMITS (due to the global lock), and thus transactions
    that are starting will surely not be trying to update any vboxes due
    to finding anything in the queue to process
        
    2. Synchronized in each vbox is needed, because it's possible that some
    other tx (ongoing or commiting may need to reload it, while a starting tx
    is running this method.
    
    3. need to apply local commits as well, or remote only?  Need to apply all,
    because of this: on node A tx 1 is committing.  After committing to repository
    but before updating local activeRecord it takes a break.  Now, another node
    B runs a read tx 2 that happens to see values committed by tx 1 to the
    repository (by reloading them. no prob here).  Now, after committing tx2,
    the same client of tx 2 runs another tx 3 on node A.  This tx will need to
    update the stuff that the halted commit is yet to do, so that it can ensure
    that the client does not see things from the past (the tx would start on
    an older version).

    Actually, this means that I may be able to release the commit lock earlier,
    by not applying the write set within the commit lock, but by doing so only
    in applyRemoteCommit (regardless of whether its really remote!) :-)
    
    However, do I need to advance the activeRecord in the commit? If not, I can
    just wait for another starting transaction to do it...hmmm dangerous...  if
    I do need to update the activeRecord in the commit lock then this method
    also needs to get the commit lock to update that record... which comes back
    to whether we need the commit lock from the beginning.

    Playing it on the safe side I may as well just get the commit lock to
    start with... then re-evaluate its need as an optimization.
    
    */
    public static ActiveTransactionsRecord applyRemoteCommits(ActiveTransactionsRecord record) {
        // avoid locking if queue is empty
        if (ClusterUtils.getRemoteCommits().isEmpty()) {
            logger.debug("No remote commits to apply. Great.");
            return record;
        }

        // commitLock needs to be acquired before the lock on REMOTE_COMMITS (should it be necessary) to avoid deadlock with another commit operation.
        Lock commitLock = ClusteredPersistentTransaction.getCommitlock();
        commitLock.lock();
        try {
            int currentCommittedNumber = Transaction.getMostRecentCommitedNumber();

            if (ClusterUtils.getRemoteCommits().isEmpty()) {
                logger.debug("No remote commits to apply. Someone already did it. Great.");
                return findActiveRecordForNumber(record, currentCommittedNumber);
            }

            RemoteCommit remoteCommit = ClusterUtils.getRemoteCommits().poll();
            int txNum = remoteCommit.getTxNumber();

            /* the following block of code is commented because we assume the
            following invariant: There cannot exist a queued remote commit yet
            to process when it already exists a newer version that is most recent.
            (except when booting. see comment below) */
            /*
            // skip all the records already processed
            while ((txNum <= currentCommittedNumber) && (remoteCommit = ClusterUtils.getRemoteCommits().poll()) != null) {
                logger.debug("Ignoring old remote commit. This can only occur for the first transaction ever.");
                txNum = remoteCommit.getTxNumber();
            }

            if (txNum <= currentCommittedNumber) {
                // the records ended, so simply get out of here, with
                // the record corresponding to the higher number that
                // we got
                logger.debug("No remote commits to apply. All were from self.");
                return findActiveRecordForNumber(record, txNum);
            }
             */

            // now, it's time to process the new remote commits

            do {
                txNum = remoteCommit.getTxNumber();

                /* this may occur only when booting.  It happens when remote
                commit messages arrive between the time the topic channel is
                established, but the most recent committed version hasn't been
                initialized yet.  When it gets initialized, it is typically to
                a greater value than the one from the txs that are enqueue. */

                if (txNum <= currentCommittedNumber) {
                    logger.info("Ignoring outdated remote commit txNum={} <= mostRecentNum={}.", txNum, currentCommittedNumber);
                    continue;
                }
                if (txNum <= debug_hazelcast_last_commit_seen) {
                    logger.error("The remote commit has a number({}) <= last_seen({})", txNum, debug_hazelcast_last_commit_seen);
                    System.exit(-1);
                    throw new Error("Inconsistent remote commit. This should not happen");
                } else {
                    logger.warn(":-)");
                    debug_hazelcast_last_commit_seen = txNum;
                }
                applyRemoteCommit(remoteCommit);
            } while ((remoteCommit = ClusterUtils.getRemoteCommits().poll()) != null);

            return findActiveRecordForNumber(record, txNum);

        } finally {
            commitLock.unlock();
        }
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
