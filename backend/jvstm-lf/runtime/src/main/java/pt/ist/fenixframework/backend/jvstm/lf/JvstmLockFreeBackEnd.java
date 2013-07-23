/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;
import jvstm.TransactionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.LockFreeReadOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.LockFreeTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.OwnedVBox;
import pt.ist.fenixframework.backend.jvstm.pstm.StandaloneVBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VBoxCache;
import pt.ist.fenixframework.backend.jvstm.repository.ExtendedRepository;
import pt.ist.fenixframework.backend.jvstm.repository.LockFreeRepository;
import pt.ist.fenixframework.backend.jvstm.repository.Repository;

public class JvstmLockFreeBackEnd extends JVSTMBackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmLockFreeBackEnd.class);

    public static final String BACKEND_NAME = "jvstm-lf";

    protected JvstmLockFreeBackEnd() {
        super(new LockFreeRepository());
    }

    protected JvstmLockFreeBackEnd(Repository repository) {
        super(repository);
    }

    public static JvstmLockFreeBackEnd getInstance() {
        return (JvstmLockFreeBackEnd) FenixFramework.getConfig().getBackEnd();
    }

    @Override
    public void init(JVSTMConfig jvstmConfig) {
        JvstmLockFreeConfig thisConfig = (JvstmLockFreeConfig) jvstmConfig;

        logger.info("initializeTransactionFactory()");
        initializeTransactionFactory();

        /* When commit requests start coming in, we need to have the jvstm.Transaction
        class inited.  We ensure that by setting up the transaction factory above. */

        logger.info("initializeGroupCommunication()");
        LockFreeClusterUtils.initializeGroupCommunication(thisConfig);

        int serverId = obtainNewServerId();
        boolean firstNode = (serverId == 0);

        if (firstNode) {
            logger.info("This is the first node!");
            localInit(thisConfig, serverId, firstNode);
//            // initialize the global lock value to the most recent commit tx number
//            LockFreeClusterUtils.initGlobalCommittedNumber(Transaction.mostRecentCommittedRecord.transactionNumber);
            // any necessary distributed communication infrastructures must be configured/set before notifying others to proceed
            LockFreeClusterUtils.notifyStartupComplete();
            /* alternatively we can now use the initGlobalCommittedNumber as the
            notification mechanism. Otherwise, we're assuming that other nodes
            will see the correct value in the global committed number when they
            get the message about startup being complete */
        } else {
            logger.info("This is NOT the first node.");
            LockFreeClusterUtils.waitForStartupFromFirstNode();
            localInit(thisConfig, serverId, firstNode);
        }

        /* start a thread that can handle commits even it nothing else is going
        on.  This is important to allow the entrance of new members.  Otherwise,
        who would process their init transaction? */
        new CommitHelper().start();
        logger.debug("Started commit helper thread");
    }

    @Override
    protected void setupJVSTM(boolean firstNode) {
        // no op
        /* disable this step from the init sequence.  It is performed differently.  See init(...). */

        // initialize transaction system
        if (firstNode) {
            initializeJvstmTxNumber();
        } else {
            synchronizeJvstmState(DomainClassInfo.getServerId());
        }
    }

    protected void synchronizeJvstmState(int serverId) {
        ActiveTransactionsRecord activeRecord = Transaction.getRecordForNewTransaction();
        Transaction tx = new InitTransaction(activeRecord);
        tx.start();
        VBox<Integer> initBox = StandaloneVBox.makeNew("SERVER" + serverId, false);
        initBox.put(serverId);
        tx.commitTx(true);

        logger.info("Set the last committed TX number to {}", Transaction.mostRecentCommittedRecord.transactionNumber);
    }

    @Override
    protected int obtainNewServerId() {
        return LockFreeClusterUtils.obtainNewServerId();
    }

    @Override
    protected void initializeTransactionFactory() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                logger.debug("Creating a new top-level transaction");
                return new LockFreeTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                logger.debug("Creating a new top-level READ-ONLY transaction");
                return new LockFreeReadOnlyTransaction(record);
            }

            @Override
            public boolean reuseTopLevelReadOnlyTransactions() {
                return false;
            }
        });
    }

    // need to override because the API for this operation has changed in JVSTM 2
    @Override
    protected void initializeJvstmTxNumber() {
        int maxTx = getRepository().getMaxCommittedTxNumber();
        if (maxTx >= 0) {
            TransactionUtils.initializeTxNumber(maxTx);
        } else {
            throw new Error("Couldn't determine the last transaction number");
        }
    }

    @Override
    public VBox lookupCachedVBox(String vboxId) {
        VBox vbox = StandaloneVBox.lookupCachedVBox(vboxId);
        if (vbox != null) {
            return vbox;
        }
        // It may be an owned VBox
        return OwnedVBox.lookupCachedVBox(vboxId);
    }

    public VBox vboxFromId(String vboxId) {
        logger.debug("vboxFromId({})", vboxId);

        VBox vbox = lookupCachedVBox(vboxId);

        if (vbox == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("VBox not found after lookup: {}", vboxId);
            }

            /* we assume that well-written programs write to data grid entries
            before reading from them.  As such, when a VBox is not cached, we
            simply allocate it. This is especially relevant for StandaloneVBoxes
            as OwnedVBoxes are correctly created before being accessed. */
//            vbox = StandaloneVBox.makeNew(vboxId, true);
            vbox = allocateVBox(vboxId);
        }

        return vbox;
    }

    private static VBox allocateVBox(String vboxId) {

        // try an owned vbox first in case the id is valid.
        VBox vbox = OwnedVBox.fromId(vboxId);

        if (vbox == null) {
            // make a standalone one
            logger.debug("Allocating a StandaloneVBox for id {}", vboxId);

            vbox = StandaloneVBox.makeNew(vboxId, true);
            // cache vbox and return the canonical vbox
            vbox = VBoxCache.getCache().cache((StandaloneVBox) vbox);
        }
        return vbox;
    }

    @Override
    public void shutdown() {
        getRepository().closeRepository();
        LockFreeClusterUtils.shutdown();
        super.shutdown();
    }

    @Override
    public ExtendedRepository getRepository() {
        return (ExtendedRepository) super.getRepository();
    }

    public int getNumMembers() {
        return LockFreeClusterUtils.getNumMembers();
    }
}
