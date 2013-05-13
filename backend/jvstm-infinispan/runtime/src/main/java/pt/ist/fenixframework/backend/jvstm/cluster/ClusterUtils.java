/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.infinispan.JvstmIspnConfig;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

public class ClusterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClusterUtils.class);
    private static final String FF_GLOBAL_LOCK_NAME = "ff.hzl.global.lock";
    private static final String FF_COMMIT_TOPIC = "ff.hzl.commits";

    private static HazelcastInstance hazelcastInstance;

    private static ClusterUtils instance = null;

    private ClusterUtils() {
    }

    public static ClusterUtils getInstance() {
        ClusterUtils localInstance = ClusterUtils.instance;
        if (localInstance != null) {
            return localInstance;
        }
        synchronized (ClusterUtils.class) {
            if (instance == null) {
                instance = new ClusterUtils();
            }
            return instance;
        }
    }

    public void initializeGroupCommunication(JvstmIspnConfig thisConfig) {
        com.hazelcast.config.Config hzlCfg = thisConfig.getHazelcastConfig();
        hazelcastInstance = Hazelcast.newHazelcastInstance(hzlCfg);
    }

    private static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public ILock globalLock() {
        return getHazelcastInstance().getLock(FF_GLOBAL_LOCK_NAME);
    }

    public void notifyStartupComplete() {
        logger.info("Notify other nodes that startup completed");

        AtomicNumber initMarker = getHazelcastInstance().getAtomicNumber("initMarker");
        initMarker.incrementAndGet();
    }

    public void waitForStartupFromFirstNode() {
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

    public int obtainNewServerId() {
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

    public void shutdown() {
        getHazelcastInstance().getLifecycleService().shutdown();
    }

}
