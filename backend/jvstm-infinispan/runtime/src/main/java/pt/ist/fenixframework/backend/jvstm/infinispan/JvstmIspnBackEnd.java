/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.ClusteredPersistentTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.PersistentReadOnlyTransaction;

import com.hazelcast.core.AtomicNumber;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class JvstmIspnBackEnd extends JVSTMBackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmIspnBackEnd.class);

    public static final String BACKEND_NAME = "jvstmispn";

    protected HazelcastInstance hazelcastInstance;

    JvstmIspnBackEnd() {
        super(new InfinispanRepository());
    }

    public static JvstmIspnBackEnd getInstance() {
        return (JvstmIspnBackEnd) FenixFramework.getConfig().getBackEnd();
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public void init(JVSTMConfig jvstmConfig) {
        JvstmIspnConfig thisConfig = (JvstmIspnConfig) jvstmConfig;

        logger.info("initializeGroupCommunication()");
        initializeGroupCommunication(thisConfig);

        int serverId = obtainNewServerId();
        boolean firstNode = (serverId == 0);

        if (firstNode) {
            logger.info("This is the first node!");
            localInit(thisConfig, serverId);
            // any necessary distributed communication infrastructures must be configured/set before notifying others to proceed
            notifyStartupComplete();

        } else {
            logger.info("This is NOT the first node.");
            waitForStartupFromFirstNode();
            localInit(thisConfig, serverId);
        }
    }

    protected void initializeGroupCommunication(JvstmIspnConfig thisConfig) {
        com.hazelcast.config.Config hzlCfg = thisConfig.getHazelcastConfig();
        this.hazelcastInstance = Hazelcast.newHazelcastInstance(hzlCfg);
    }

    private void notifyStartupComplete() {
        logger.info("Notify other nodes that startup completed");

        AtomicNumber initMarker = getHazelcastInstance().getAtomicNumber("initMarker");
        initMarker.incrementAndGet();
    }

    private void waitForStartupFromFirstNode() {
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

    @Override
    protected int obtainNewServerId() {
        /* currently does not reuse the server Id value while any server is up.
        This can be changed if needed.  However, we currently depend on the first
        server getting the AtomicNumber 0 to know that it is the first member
        to appear.  By reusing numbers with the cluster alive, we either don't
        reuse 0 or change the algorithm  that detects the first member */

        AtomicNumber serverIdGenerator = getHazelcastInstance().getAtomicNumber("serverId");
        long longId = serverIdGenerator.getAndAdd(1L);

        logger.info("Got (long) serverId: {}", longId);

        int shortId = (int) longId;
        if (shortId != longId) {
            throw new Error("Failed to obtain a valid id");
        }

        return shortId;
    }

    @Override
    protected void initializeTransactionFactory() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new ClusteredPersistentTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new PersistentReadOnlyTransaction(record);
            }
        });
    }

    public HazelcastInstance getHazelcastInstance() {
        return this.hazelcastInstance;
    }

    @Override
    public void shutdown() {
        getRepository().closeRepository();
        getHazelcastInstance().getLifecycleService().shutdown();
        super.shutdown();
    }

//    protected void configJvstmIspn(JvstmIspnConfig config) throws Exception {
//        transactionManager.setupTxManager(config);
//    }

}
