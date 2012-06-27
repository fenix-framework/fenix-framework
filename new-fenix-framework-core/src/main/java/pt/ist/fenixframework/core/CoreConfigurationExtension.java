package pt.ist.fenixframework.core;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
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

    public void initialize(Config config) {
        FenixFramework.setTransactionManager(new NoOpTransactionManager());
        FenixFramework.setRepository(new NoOpRepository());
    }
}
