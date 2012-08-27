package pt.ist.fenixframework.backend.infinispan;

import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

public class InfinispanTransactionManager implements TransactionManager {
    public void begin() {}
    public void begin(boolean readOnly) {}
    public void commit() {}
    public Transaction getTransaction() { return null; }
    public void rollback() {}
    public void withTransaction(TransactionalCommand command) {
        command.doIt();
    }
}

