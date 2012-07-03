package pt.ist.fenixframework.core;

import java.io.Serializable;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

/**
 * This is the default configuration manager used by the fenix-framework-core.
 */
public class CoreConfigurationExtension implements ConfigurationExtension {
    static class NoOpTransactionManager implements TransactionManager {
        public void begin() {}
        public void commit() {}
        public void withTransaction(TransactionalCommand command) {
            command.doIt();
        }
    }

    static class NoOpRepository implements Repository { }

    protected final Repository repository;
    protected final TransactionManager transactionManager;

    public CoreConfigurationExtension() {
        this.repository = new NoOpRepository();
        this.transactionManager = new NoOpTransactionManager();
    }

    @Override
    public void initialize(Config config) {
        // nothing yet
    }

    public <T extends DomainObject> T getDomainObject(Serializable externalId) {
        if (! (externalId instanceof Long)) {
            throw new RuntimeException("The externalId in the CoreConfigurationExtension must be of type Long. Got: "
                                       + externalId.getClass());
        }
        return CoreDomainObject.fromOid((Long)externalId);
    }

    public Repository getRepository() {
        return this.repository;
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
