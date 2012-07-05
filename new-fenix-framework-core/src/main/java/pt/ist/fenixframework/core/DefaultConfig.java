package pt.ist.fenixframework.core;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

/**
 * This is the default configuration manager used by the fenix-framework-core.
 * 
 * @see Config
 *
 */
public class DefaultConfig extends Config {
    static class NoOpRepository implements Repository { }

    static class NoOpTransactionManager implements TransactionManager {
        public void begin() {}
        public void commit() {}
        public void withTransaction(TransactionalCommand command) {
            command.doIt();
        }
    }

    protected final Repository repository;
    protected final TransactionManager transactionManager;

    public DefaultConfig() {
        this.repository = new NoOpRepository();
        this.transactionManager = new NoOpTransactionManager();
    }

    @Override
    protected void init() { }

    @Override
    protected <T extends DomainObject> T getDomainObject(String externalId) {
        return CoreDomainObject.fromOid(Long.parseLong(externalId));
    }

    @Override
    protected Repository getRepository() {
        return this.repository;
    }

    @Override
    protected TransactionManager getTransactionManager() {
        return this.transactionManager;
    }


}
