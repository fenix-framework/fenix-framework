package pt.ist.fenixframework.backend.mem;

import eu.cloudtm.LocalityHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.ClusterInformation;
import pt.ist.fenixframework.core.SharedIdentityMap;
import pt.ist.fenixframework.messaging.MessagingQueue;

public class MemBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(MemBackEnd.class);
    public static final String BACKEND_NAME = "mem";

    protected final TransactionManager transactionManager;

    public MemBackEnd() {
        this.transactionManager = new MemTransactionManager();
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

            // so we get it again from the cache before returning if
            root = fromOid(1L);
            assert root != null; // there must be at least one
        }
        return root;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(Long.parseLong(externalId));
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        if (logger.isTraceEnabled()) {
            logger.trace("fromOid(" + oid + ")");
        }
        return (T) SharedIdentityMap.getCache().lookup(oid);

    }

    @Override
    public void shutdown() {
    }

    @Override
    public <T extends DomainObject> T getOwnerDomainObject(String storageKey) {
        throw new UnsupportedOperationException("It does not make sense to invoke this method in a storage-less BackEnd");
    }

    @Override
    public String[] getStorageKeys(DomainObject domainObject) {
        throw new UnsupportedOperationException("It does not make sense to invoke this method in a storage-less BackEnd");
    }

    @Override
    public ClusterInformation getClusterInformation() {
        //local-only back end
        return ClusterInformation.LOCAL_MODE;
    }

    @Override
    public MessagingQueue createMessagingQueue(String appName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalityHints getLocalityHints(String externalId) {
        throw new UnsupportedOperationException();
    }
}
