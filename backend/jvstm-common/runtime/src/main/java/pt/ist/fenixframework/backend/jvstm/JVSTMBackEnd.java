/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.FenixFrameworkData;
import pt.ist.fenixframework.backend.jvstm.pstm.NonPersistentTopLevelReadOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.NonPersistentTopLevelTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VBoxCache;
import pt.ist.fenixframework.backend.jvstm.repository.NoRepository;
import pt.ist.fenixframework.backend.jvstm.repository.Repository;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

/**
 *
 */
public class JVSTMBackEnd implements BackEnd {

    private static final Logger logger = LoggerFactory.getLogger(JVSTMBackEnd.class);
    public static final String BACKEND_NAME = "jvstm";

    private final DomainObjectAllocator allocator = new DomainObjectAllocator(JVSTMDomainObject.class);

    // the repository instance used to persist the changes
    protected final Repository repository;
    protected final JVSTMTransactionManager transactionManager;

    protected boolean newInstance;

    // this constructor is used by the JVSTMConfig when no sub-backend has been created 
    JVSTMBackEnd() {
        this(new NoRepository());
    }

    public JVSTMBackEnd(Repository repository) {
        this.repository = repository;
        this.transactionManager = new JVSTMTransactionManager();
    }

    public static JVSTMBackEnd getInstance() {
        return (JVSTMBackEnd) FenixFramework.getConfig().getBackEnd();
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public DomainRoot getDomainRoot() {
        return fromOid(1L);
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(Long.parseLong(externalId, 16));
    }

    @Override
    public JVSTMTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        logger.debug("fromOid({})", oid);

        AbstractDomainObject obj = SharedIdentityMap.getCache().lookup(oid);

        if (obj == null) {
            long longOid = ((Long) oid).longValue();

            if (logger.isDebugEnabled()) {
                logger.debug("Object not found in IdentityMap: {}", Long.toHexString(longOid));
            }

            obj = allocator.allocateObject(DomainClassInfo.mapOidToClass(longOid), oid);
            // cache object and return the canonical object
            obj = SharedIdentityMap.getCache().cache(obj);
        }

        return (T) obj;
    }

    /**
     * Initialize this backend. This method is invoked with the JVSTMConfig instance fully available.
     * 
     * @param jvstmConfig
     * @throws Exception
     */
    public void init(JVSTMConfig jvstmConfig) {
        int serverId = obtainNewServerId();
        localInit(jvstmConfig, serverId, true);
    }

    protected void localInit(JVSTMConfig jvstmConfig, int serverId, boolean firstNode) {
        logger.info("initializeRepository()");
        this.newInstance = initializeRepository(jvstmConfig);

        logger.info("initializeDomainClassInfos");
        initializeDomainClassInfos(serverId);

        logger.info("setupJVSTM");
        setupJVSTM(firstNode);

        // We need to ensure that the DomainRoot instance exists and is correctly initialized BEFORE the execution of any code that may need it.
        logger.info("createDomainRootIfNeeded");
        if (this.newInstance) {
            createDomainRoot();
        }

        // this method will be moved (probably to the core) when we have FenixFrameworkData (or a similarly named class) defined there
        logger.info("ensureFenixFrameworkDataExists");
        ensureFenixFrameworkDataExists();

//        logger.info("startStatisticsThread");
//        new StatisticsThread().start();

    }

    /**
     * Each concrete backend should override this method to provide a new server id when requested. Ideally this method could be
     * abstract, but the default implementation always returns 0. This is so for two reasons: 1) It works well as a default value
     * while this jvstm-common code does not wish enforce clustering support on all of its implementations; 2) The NoRepository is
     * currently implemented at this level (some time in the future it should probably be moved to a 'jvstm-mem' backend). The
     * Repository implementation does not support clustering, but still requires a serverId value.
     * 
     * @return
     */
    protected int obtainNewServerId() {
        return 0;
    }

    // returns whether the repository is new, so that we know we need to create the DomainRoot
    protected boolean initializeRepository(JVSTMConfig jvstmConfig) {
        return this.repository.init(jvstmConfig);
    }

    protected void initializeDomainClassInfos(int serverId) {
        DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), serverId);
    }

    protected void setupJVSTM(boolean firstNode) {
        // by default use JVSTM's transaction classes
        initializeTransactionFactory();

        // initialize transaction system
        initializeJvstmTxNumber();
    }

    protected void initializeTransactionFactory() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new NonPersistentTopLevelTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new NonPersistentTopLevelReadOnlyTransaction(record);
            }
        });
    }

    protected void initializeJvstmTxNumber() {
        int maxTx = getRepository().getMaxCommittedTxNumber();
        if (maxTx >= 0) {
            logger.info("Setting the last committed TX number to {}", maxTx);
            Transaction.setMostRecentActiveRecord(new ActiveTransactionsRecord(maxTx, null));
        } else {
            throw new Error("Couldn't determine the last transaction number");
        }
    }

    @Atomic(mode = TxMode.WRITE)
    protected void createDomainRoot() {
        new DomainRoot();
    }

    @Atomic(mode = TxMode.WRITE)
    // in the core we will not be able to use Atomic. Must do begin/commit
    private void ensureFenixFrameworkDataExists() {
        FenixFrameworkData data = FenixFramework.getDomainRoot().getFenixFrameworkData();
        if (data == null) {
            FenixFramework.getDomainRoot().setFenixFrameworkData(new FenixFrameworkData());
        }
    }

    /**
     * Looks up a cached VBox given its identifier.
     * 
     * @param vboxId The vbox identifier
     * @return The VBox if it is available in memory. Otherwise, <code>null</code> (if either the VBox does not exist or is not in
     *         cache).
     */
    public VBox lookupCachedVBox(String vboxId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void shutdown() {
        VBoxCache.getCache().shutdown();
    }

    public Repository getRepository() {
        return this.repository;
    }

    @Override
    public boolean isNewInstance() {
        return newInstance;
    }

    @Override
    public boolean isDomainObjectValid(DomainObject object) {
        throw new UnsupportedOperationException("Sorry, cannot determine if the object is valid");
    }

}
