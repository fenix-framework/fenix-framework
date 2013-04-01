/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.FenixFrameworkData;
import pt.ist.fenixframework.backend.jvstm.pstm.StatisticsThread;
import pt.ist.fenixframework.backend.jvstm.pstm.TransactionSupport;
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

    // the repository instance used to persist the changes
    protected final Repository repository;
    protected final JVSTMTransactionManager transactionManager;

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
        logger.trace("fromOid({})", oid);

        AbstractDomainObject obj = SharedIdentityMap.getCache().lookup(oid);

        if (obj == null) {
            long longOid = ((Long) oid).longValue();

            if (logger.isTraceEnabled()) {
                logger.trace("Object not found in IdentityMap: " + longOid);
            }

            obj = DomainObjectAllocator.allocateObject(DomainClassInfo.mapOidToClass(longOid), oid);
            // cache object and return the canonical object
            obj = SharedIdentityMap.getCache().cache(obj);
        }

        return (T) obj;
    }

    public void init(JVSTMConfig jvstmConfig) {
        logger.info("initializeDomainClassInfos");
        DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);

        logger.info("setupJVSTM");
        TransactionSupport.setupJVSTM();

        // We need to ensure that the DomainRoot instance exists and is correctly initialized BEFORE the execution of any code that may need it.
        logger.info("ensureDomainRoot");
        getRepository().ensureDomainRoot();

        // this method will be moved (probably to the core) when we have FenixFrameworkData (or a similarly named class) defined there
        logger.info("ensureFenixFrameworkDataExists");
        ensureFenixFrameworkDataExists();

        logger.info("startStatisticsThread");
        new StatisticsThread().start();
    }

    @Atomic(speculativeReadOnly = false)
    // in the core we will not be able to use Atomic. Must do begin/commit
    private void ensureFenixFrameworkDataExists() {
        FenixFrameworkData data = FenixFramework.getDomainRoot().getFenixFrameworkData();
        if (data == null) {
            FenixFramework.getDomainRoot().setFenixFrameworkData(new FenixFrameworkData());
        }
    }

    @Override
    public void shutdown() {
    }

    public Repository getRepository() {
        return this.repository;
    }

}
