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

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.OID;
import pt.ist.fenixframework.backend.jvstm.JVSTMTransactionManager;

public class JvstmIspnBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmIspnBackEnd.class);

    public static final String BACKEND_NAME = "jvstmispn";

    private static final JvstmIspnBackEnd instance = new JvstmIspnBackEnd();

    protected final JVSTMTransactionManager transactionManager;

    private JvstmIspnBackEnd() {
        this.transactionManager = new JVSTMTransactionManager();
    }

    public static JvstmIspnBackEnd getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public DomainRoot getDomainRoot() {
        OID rootId = OID.ROOT_OBJECT_ID;
        DomainRoot domainRoot = fromOid(rootId);
        if (domainRoot == null) {
            domainRoot = new DomainRoot();
        }
        return domainRoot;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(new OID(externalId));
    }

    @Override
    public JVSTMTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        return null;
//        OID internalId = (OID)oid;
//        if (logger.isTraceEnabled()) {
//            logger.trace("fromOid(" + internalId + ")");
//        }
//        return (T)transactionManager.getEntityManager().find(internalId.getObjClass(),
//                                                             internalId.getPrimaryKey());
    }

    @Override
    public void shutdown() {
//        transactionManager.emf.close();
    }

    protected void configJvstmIspn(JvstmIspnConfig config) throws Exception {
//        transactionManager.setupTxManager(config);
    }

//    public void save(AbstractDomainObject obj) {
//        logger.debug("Saving " + obj.getClass());
//        transactionManager.getEntityManager().persist(obj);
//    }

}
