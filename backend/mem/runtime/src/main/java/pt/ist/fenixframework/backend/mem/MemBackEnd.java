package pt.ist.fenixframework.backend.mem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.SharedIdentityMap;

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
    public boolean isNewInstance() {
        // In-Memory backend is always a new instance
        return true;
    }

    @Override
    public boolean isDomainObjectValid(DomainObject object) {
        // In memory, if it is reachable, it is valid
        return true;
    }
}
