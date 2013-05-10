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
import pt.ist.fenixframework.backend.jvstm.pstm.PersistentReadOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.PersistentTransaction;

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
    protected void initializeTransactionFactory() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new PersistentTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new PersistentReadOnlyTransaction(record);
            }
        });
    }

    @Override
    public void shutdown() {
        getRepository().closeRepository();
        super.shutdown();
    }

//    protected void configJvstmIspn(JvstmIspnConfig config) throws Exception {
//        transactionManager.setupTxManager(config);
//    }

}
