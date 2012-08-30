package pt.ist.fenixframework.backend.mem;

import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

public class NoOpTransactionManager implements TransactionManager {
    public void begin() {}
    public void begin(boolean readOnly) {}
    public void commit() {}
    public Transaction getTransaction() { return null; }
    public void rollback() {}
    public <T> T withTransaction(TransactionalCommand<T> command) {
        return command.doIt();
    }
}

