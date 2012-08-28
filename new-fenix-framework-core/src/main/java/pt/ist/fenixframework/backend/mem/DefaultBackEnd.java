package pt.ist.fenixframework.backend.mem;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;

public class DefaultBackEnd implements BackEnd {
    protected final TransactionManager transactionManager;

    public DefaultBackEnd() {
        this.transactionManager = new NoOpTransactionManager();
    }

    @Override
    public DomainRoot getDomainRoot() {
        DomainRoot root = CoreDomainObject.fromOid(1L);
        if (root == null) {
            root = new DomainRoot(); // which automatically caches this instance, but does not
            // ensure that it is the first, as a concurrent request
            // might create another

            // so we get it again from the cache before returning if
            root = CoreDomainObject.fromOid(1L);
            assert root != null; // there must be at least one
        }
        return root;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return CoreDomainObject.fromOid(Long.parseLong(externalId));
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        return CoreDomainObject.fromOid((Long)oid);
    }
}
