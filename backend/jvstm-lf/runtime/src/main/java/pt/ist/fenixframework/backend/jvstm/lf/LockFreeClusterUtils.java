/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.core.TransactionError;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class LockFreeClusterUtils {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeClusterUtils.class);
    private static final String FF_GLOBAL_LOCK_NAME = "ff.hzl.global.lock";
    private static final String FF_GLOBAL_LOCK_NUMBER_NAME = "ff.hzl.global.lock.number";
    private static final long FF_GLOBAL_LOCK_LOCKED_VALUE = -1;
    private static final String FF_COMMIT_TOPIC_NAME = "ff.hzl.commits";

    private static HazelcastInstance HAZELCAST_INSTANCE;

    // commit requests that have not been applied yet
    private static final ConcurrentLinkedQueue<CommitRequest> COMMIT_REQUESTS = new ConcurrentLinkedQueue<CommitRequest>();

    private LockFreeClusterUtils() {
    }

    public static void initializeGroupCommunication(JvstmLockFreeConfig thisConfig) {
        com.hazelcast.config.Config hzlCfg = thisConfig.getHazelcastConfig();
        HAZELCAST_INSTANCE = Hazelcast.newHazelcastInstance(hzlCfg);

        // register listener for commit requests
        registerListenerForCommitRequests();
    }

    private static void registerListenerForCommitRequests() {
        ITopic<CommitRequest> topic = getHazelcastInstance().getTopic(FF_COMMIT_TOPIC_NAME);

        topic.addMessageListener(new MessageListener<CommitRequest>() {

            @Override
            public void onMessage(Message<CommitRequest> message) {
                CommitRequest commitRequest = message.getMessageObject();

                if (commitRequest.getServerId() == DomainClassInfo.getServerId()) {
                    logger.debug("Ignoring self commit message.");
                } else {
                    logger.debug("Received commit request message. serverId={}, tx={}", commitRequest.getServerId(),
                            commitRequest.getTxNumber());
//                    logger.debug("lets take a break");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        System.exit(-1);
//                    }
                    COMMIT_REQUESTS.offer(commitRequest);
//                    logger.debug("Enqueued commit request: serverId={}, tx={}", commitRequest.getServerId(),
//                            commitRequest.getTxNumber());
                }

            }
        });
    }

    public static void initGlobalLockNumber(int value) {
        AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);
        lockNumber.compareAndSet(0, value);
    }

    // the instance should have been initialized in a single thread within the
    // FenixFramework static initializer's lock (via the invocation of the method
    // initializeGroupCommunication.
    private static HazelcastInstance getHazelcastInstance() {
        return HAZELCAST_INSTANCE;
    }

    public static void notifyStartupComplete() {
        logger.info("Notify other nodes that startup completed");

        AtomicNumber initMarker = getHazelcastInstance().getAtomicNumber("initMarker");
        initMarker.incrementAndGet();
    }

    public static void waitForStartupFromFirstNode() {
        logger.info("Waiting for startup from first node");

        // check initMarker in AtomicNumber (value 1)
        AtomicNumber initMarker = getHazelcastInstance().getAtomicNumber("initMarker");

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

        AtomicNumber serverIdGenerator = getHazelcastInstance().getAtomicNumber("serverId");
        long longId = serverIdGenerator.getAndAdd(1L);

        logger.info("Got (long) serverId: {}", longId);

        int intId = (int) longId;
        if (intId != longId) {
            throw new Error("Failed to obtain a valid id");
        }

        return intId;
    }

    public static void sendCommitInfoToOthers(CommitRequest commitRequest) {
        // test for debug, because computing commitRequest.toString() is expensive
        if (logger.isDebugEnabled()) {
            logger.debug("Send commit info to others: {}", commitRequest);
        }

        ITopic<CommitRequest> topic = getHazelcastInstance().getTopic(FF_COMMIT_TOPIC_NAME);
        topic.publish(commitRequest);
    }

    public static ConcurrentLinkedQueue<CommitRequest> getCommitRequests() {
        return COMMIT_REQUESTS;
    }

    public static void shutdown() {
        getHazelcastInstance().getLifecycleService().shutdown();
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

    public static int globalLock() {
        logger.debug("Will get global cluster lock...");

        try {
            AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);

//            int ourLockValue = 0 - DomainClassInfo.getServerId();
            do {
                long currentValue = lockNumber.get();
                boolean unlocked = currentValue != FF_GLOBAL_LOCK_LOCKED_VALUE;

                if (unlocked && lockNumber.compareAndSet(currentValue, FF_GLOBAL_LOCK_LOCKED_VALUE)) {
                    logger.debug("Acquired global cluster lock. ({} -> {})", currentValue, FF_GLOBAL_LOCK_LOCKED_VALUE);

                    return (int) currentValue;  // transaction counters fit into an integer
                } else {
                    logger.debug("Global lock taken. Retrying...");

                    globalLockIsNotYetAvailable();
                }
            } while (true);
        } catch (RuntimeException e) {
            logger.error("Failed to acquire global lock");
            throw new TransactionError(e);
        }
    }

    /* We'll retry later. Several mechanisms can be used here.  Which is the
    best? For now we try to help a little by spending time checking if the
    COMMIT_REQUESTS queue requires any processing.  Also, this may help other
    transactions that are starting to go ahead */
    private static void globalLockIsNotYetAvailable() {
        // first naive version. just burn cpu
        Thread.yield();
    }

    public static void globalUnlock(int txNum) {
        logger.debug("Will release global cluster lock ( -> {})", txNum);
        try {
            AtomicNumber lockNumber = getHazelcastInstance().getAtomicNumber(FF_GLOBAL_LOCK_NUMBER_NAME);
            lockNumber.set(txNum);
        } catch (RuntimeException e) {
            logger.error("Failed to release global lock");
            throw new TransactionError(e);
        }
    }

}