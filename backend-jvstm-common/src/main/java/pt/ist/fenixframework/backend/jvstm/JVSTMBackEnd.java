/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstm.repository.NoRepository;
import pt.ist.fenixframework.backend.jvstm.repository.Repository;
import pt.ist.fenixframework.core.SharedIdentityMap;

/**
 *
 */
public class JVSTMBackEnd implements BackEnd {

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
        DomainRoot root = fromOid(1L);
        if (root == null) {
            root = new DomainRoot(); // which automatically caches this instance, but does not
            // ensure that it is the first, as a concurrent request
            // might create another

            // so we get it again from the cache before returning it
            root = fromOid(1L);
            assert root != null;
        }
        return root;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(Long.parseLong(externalId));
    }

    @Override
    public JVSTMTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        return (T) SharedIdentityMap.getCache().lookup(oid);
    }

    @Override
    public void shutdown() {
    }

    public Repository getRepository() {
        return this.repository;
    }

}
