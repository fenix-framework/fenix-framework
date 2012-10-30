package pt.ist.fenixframework.backend.mem;

import java.util.concurrent.Callable;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;

public class MemTransactionManager implements TransactionManager {
    @Override
    public void begin() {}

    @Override
    public void begin(boolean readOnly) {}

    @Override
    public void commit() {}

    @Override
    public Transaction getTransaction() { return null; }

    @Override
    public void rollback() {}

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
        return withTransaction(command, null);
    }

    /**
     * Directly calls the command with no added behaviour, ignoring the given <code>atomic</code>
     * configuration.
     */
    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
        return command.call();
    }
}

