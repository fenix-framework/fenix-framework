/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.infinispan;

import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.cluster.ClusterUtils;
import pt.ist.fenixframework.backend.jvstm.pstm.ClusteredPersistentReadOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.ClusteredPersistentTransaction;

public class JvstmIspnBackEnd extends JVSTMBackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmIspnBackEnd.class);

    public static final String BACKEND_NAME = "jvstmispn";

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
        ClusterUtils.initializeGroupCommunication(thisConfig);

        int serverId = obtainNewServerId();
        boolean firstNode = (serverId == 0);

        if (firstNode) {
            logger.info("This is the first node!");
            localInit(thisConfig, serverId);
            // initialize the global lock value to the most recent commit tx number
            ClusterUtils.initGlobalLockNumber(Transaction.getMostRecentCommitedNumber());
            // any necessary distributed communication infrastructures must be configured/set before notifying others to proceed
            ClusterUtils.notifyStartupComplete();
            /* alternatively we can now use the initGlobalLockNumber as the
            notification mechanism. Otherwise, we're assuming that other nodes
            will see the correct value in the lock number when they get the
            message about startup being complete */

        } else {
            logger.info("This is NOT the first node.");
            ClusterUtils.waitForStartupFromFirstNode();
            localInit(thisConfig, serverId);
        }
    }

    @Override
    protected int obtainNewServerId() {
        return ClusterUtils.obtainNewServerId();
    }

    @Override
    protected void initializeTransactionFactory() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
//                return new PersistentTransaction(record);
                return new ClusteredPersistentTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
//                return new PersistentReadOnlyTransaction(record);
                return new ClusteredPersistentReadOnlyTransaction(record);
            }
        });
    }

    @Override
    public void shutdown() {
        getRepository().closeRepository();
        ClusterUtils.shutdown();
        super.shutdown();
    }

}
