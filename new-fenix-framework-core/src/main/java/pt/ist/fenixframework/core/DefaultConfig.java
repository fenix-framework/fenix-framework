package pt.ist.fenixframework.core;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

/**
 * This is the default configuration manager used by the fenix-framework-core.
 * 
 * @see Config
 *
 */
public class DefaultConfig extends Config {
    static class DefaultBackEnd implements BackEnd {
        protected final TransactionManager transactionManager;

        public DefaultBackEnd() {
            this.transactionManager = new NoOpTransactionManager();
        }

        @Override
        public DomainRoot getDomainRoot() {
            return CoreDomainObject.fromOid(1L);
        }

        @Override
        public <T extends DomainObject> T getDomainObject(String externalId) {
            return CoreDomainObject.fromOid(Long.parseLong(externalId));
        }


        @Override
        public TransactionManager getTransactionManager() {
            return this.transactionManager;
        }

    }

    static class NoOpTransactionManager implements TransactionManager {
        public void begin() {}
        public void begin(boolean readOnly) {}
        public void commit() {}
        public Transaction getTransaction() { return null; }
        public void rollback() {}
        public void withTransaction(TransactionalCommand command) {
            command.doIt();
        }
    }

    protected final BackEnd backEnd;

    public DefaultConfig() {
        this.backEnd = new DefaultBackEnd();
    }

    @Override
    protected void init() { }

    @Override
    protected BackEnd getBackEnd() {
        return this.backEnd;
    }


}
