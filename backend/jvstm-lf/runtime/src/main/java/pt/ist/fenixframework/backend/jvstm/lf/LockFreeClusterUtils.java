/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.CommitOnlyTransaction;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class LockFreeClusterUtils {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeClusterUtils.class);
//    private static final String FF_GLOBAL_LOCK_NAME = "ff.hzl.global.lock";
//    private static final String FF_GLOBAL_LOCK_NUMBER_NAME = "ff.hzl.global.lock.number";
//    private static final long FF_GLOBAL_LOCK_LOCKED_VALUE = -1;
    private static final String FF_COMMIT_TOPIC_NAME = "ff.hzl.commits";

    private static HazelcastInstance HAZELCAST_INSTANCE;

    // commit requests that have not been applied yet
    private static final AtomicReference<CommitRequest> commitRequestsHead = new AtomicReference<CommitRequest>();
    // this avoids iterating from the head every time a commit request arrives.  Is only used by the (single) thread that enqueues requests
    private static CommitRequest commitRequestsTail = null;

//    // where to append commit requests. may be outdated due to concurrency, so we need to be careful when updating this reference 
//    private static volatile AtomicReference<CommitRequest> commitRequestTail = new AtomicReference<CommitRequest>(null);

//    private static final ConcurrentLinkedQueue<CommitRequest> COMMIT_REQUESTS = new ConcurrentLinkedQueue<CommitRequest>();

    private LockFreeClusterUtils() {
    }

    public static void initializeGroupCommunication(JvstmLockFreeConfig thisConfig) {
        commitRequestsHead.set(CommitRequest.makeSentinelRequest());
        commitRequestsTail = getCommitRequestAtHead();

        com.hazelcast.config.Config hzlCfg = thisConfig.getHazelcastConfig();
        HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance(hzlCfg);

        // register listener for commit requests
        registerListenerForCommitRequests();
    }

//    private static final ReentrantLock ENQUEUE_LOCK = new ReentrantLock(true);

    private static void registerListenerForCommitRequests() {
        ITopic<CommitRequest> topic = getHazelcastInstance().getTopic(FF_COMMIT_TOPIC_NAME);

        topic.addMessageListener(new MessageListener<CommitRequest>() {

            @Override
            public final void onMessage(Message<CommitRequest> message) {
//                ENQUEUE_LOCK.lock();

//                try {
                CommitRequest commitRequest = message.getMessageObject();

                logger.debug("Received commit request message. id={}, serverId={}", commitRequest.getId(),
                        commitRequest.getServerId());

                commitRequest.assignTransaction();
                enqueueCommitRequest(commitRequest);
//                } finally {
//                    ENQUEUE_LOCK.unlock();
//                }
            }

            private final void enqueueCommitRequest(CommitRequest commitRequest) {
//                synchronized (LockFreeClusterUtils.FF_COMMIT_TOPIC_NAME) { // TODO REMOVE THIS !!!

                CommitRequest last = commitRequestsTail;

                // according to Hazelcast, onMessage() runs on a single thread, so this CAS should never fail
                if (!last.setNext(commitRequest)) {
                    enqueueFailed();
                }
                // update last known tail
                commitRequestsTail = commitRequest;
//                }
            }

            private void enqueueFailed() throws AssertionError {
                String message = "Impossible condition: failed to enqueue commit request";
                logger.error(message);
                throw new AssertionError(message);
            }

        });
    }

//    public static void initGlobalCommittedNumber(int value) {
//        AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);
//        lockNumber.compareAndSet(0, value);
//    }

    // the instance should have been initialized in a single thread within the
    // FenixFramework static initializer's lock (via the invocation of the method
    // initializeGroupCommunication.
    private static HazelcastInstance getHazelcastInstance() {
        return HAZELCAST_INSTANCE;
    }

    public static void notifyStartupComplete() {
        logger.info("Notify other nodes that startup completed");

        IAtomicLong initMarker = getHazelcastInstance().getAtomicLong("initMarker");
        initMarker.incrementAndGet();
    }

    public static void waitForStartupFromFirstNode() {
        logger.info("Waiting for startup from first node");

        // check initMarker in AtomicNumber (value 1)
        IAtomicLong initMarker = getHazelcastInstance().getAtomicLong("initMarker");

        while (initMarker.get() == 0) {
            logger.debug("Waiting for first node to startup...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        logger.debug("First node startup is complete.  We can proceed.");
    }

    public static int obtainNewServerId() {
        /* currently does not reuse the server Id value while any server is up.
        This can be changed if needed.  However, we currently depend on the first
        server getting the AtomicNumber 0 to know that it is the first member
        to appear.  By reusing numbers with the cluster alive, we either don't
        reuse 0 or change the algorithm  that detects the first member */

        IAtomicLong serverIdGenerator = getHazelcastInstance().getAtomicLong("serverId");
        long longId = serverIdGenerator.getAndAdd(1L);

        logger.info("Got (long) serverId: {}", longId);

        int intId = (int) longId;
        if (intId != longId) {
            throw new Error("Failed to obtain a valid id");
        }

        return intId;
    }

    public static void sendCommitRequest(CommitRequest commitRequest) {
        // test for debug, because computing commitRequest.toString() is expensive
        if (logger.isDebugEnabled()) {
            logger.debug("Send commit request to others: {}", commitRequest);
        }

        ITopic<CommitRequest> topic = getHazelcastInstance().getTopic(FF_COMMIT_TOPIC_NAME);
        topic.publish(commitRequest);
    }

    /**
     * Get the first element in the commit requests queue.
     * 
     * @return The {@link CommitRequest} at the head of the queue.
     */
    public static CommitRequest getCommitRequestAtHead() {
        return commitRequestsHead.get();
    }

    public static CommitRequest getCommitRequestsTail() {
        return commitRequestsTail;
    }

    /**
     * Clears the given commit request from the head of the remote commits queue if: (1) there is a next one; AND (2) the head is
     * still the given request. This method should only be invoked when the commit request to remove is already handled (either
     * committed or marked as invalid).
     * 
     * @param commitRequest The commitRequest to remove from head
     * @return The commit request left at the head. This can be either: (1) The commit request given as argument (if it could not
     *         be removed); (2) the commit request following the one given in the argument; or (3) any other commit request (if
     *         the one given as argument was no longer at the head (which means it had been removed already by another thread.
     */
    public static CommitRequest tryToRemoveCommitRequest(CommitRequest commitRequest) {
        CommitRequest next = commitRequest.getNext();

        if (next == null) {
            logger.debug("Commit request {} has no next yet.  Must remain at the head", commitRequest.getId());
            return commitRequest;
        }

        if (commitRequestsHead.compareAndSet(commitRequest, next)) {
            logger.debug("Removed commit request {} from the head.", commitRequest.getId());
            return next;
        } else {
            logger.debug("Commit request {} was no longer at the head.", commitRequest.getId());
            return commitRequestsHead.get();
        }

    }

    public static void shutdown() {
        HazelcastInstance hzl = getHazelcastInstance();
        hzl.getTopic(FF_COMMIT_TOPIC_NAME).destroy();
        hzl.getLifecycleService().shutdown();

        /* strangely this is here.  Perhaps we should move these clear()s
        elsewhere. They are needed when the classes are reused via
        FenixFramework.shutdown()/initialize().  I guess these maps should be
        in this class not in COTx */
        CommitOnlyTransaction.txVersionToCommitIdMap.clear();
        CommitOnlyTransaction.commitsMap.clear();
    }

    /**
     * Get the number of members in the cluster.
     * 
     * @return The number of members in the cluster or <code>-1</code> if the information is not available
     */
    public static int getNumMembers() {
        if (!getHazelcastInstance().getLifecycleService().isRunning()) {
            return -1;
        } else {
            return getHazelcastInstance().getCluster().getMembers().size();
        }

    }

    //////////////// TO DELETE BELOW THIS ///////////////////////
    //////////////// TO DELETE BELOW THIS ///////////////////////
    //////////////// TO DELETE BELOW THIS ///////////////////////
    //////////////// TO DELETE BELOW THIS ///////////////////////
    //////////////// TO DELETE BELOW THIS ///////////////////////

//    public static int globalLock() {
//        logger.debug("Will get global cluster lock...");
//
//        try {
//            AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);
//
////            int ourLockValue = 0 - DomainClassInfo.getServerId();
//            do {
//                long currentValue = lockNumber.get();
//                boolean unlocked = currentValue != FF_GLOBAL_LOCK_LOCKED_VALUE;
//
//                if (unlocked && lockNumber.compareAndSet(currentValue, FF_GLOBAL_LOCK_LOCKED_VALUE)) {
//                    logger.debug("Acquired global cluster lock. ({} -> {})", currentValue, FF_GLOBAL_LOCK_LOCKED_VALUE);
//
//                    return (int) currentValue;  // transaction counters fit into an integer
//                } else {
//                    logger.debug("Global lock taken. Retrying...");
//
//                    globalLockIsNotYetAvailable();
//                }
//            } while (true);
//        } catch (RuntimeException e) {
//            logger.error("Failed to acquire global lock");
//            throw new TransactionError(e);
//        }
//    }
//
//    /* We'll retry later. Several mechanisms can be used here.  Which is the
//    best? For now we try to help a little by spending time checking if the
//    COMMIT_REQUESTS queue requires any processing.  Also, this may help other
//    transactions that are starting to go ahead */
//    private static void globalLockIsNotYetAvailable() {
//        // first naive version. just burn cpu
//        Thread.yield();
//    }
//
//    public static void globalUnlock(int txNum) {
//        logger.debug("Will release global cluster lock ( -> {})", txNum);
//        try {
//            AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);
//            lockNumber.set(txNum);
//        } catch (RuntimeException e) {
//            logger.error("Failed to release global lock");
//            throw new TransactionError(e);
//        }
//    }

}
