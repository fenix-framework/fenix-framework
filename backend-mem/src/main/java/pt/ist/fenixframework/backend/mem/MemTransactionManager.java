package pt.ist.fenixframework.backend.mem;

import java.util.concurrent.Callable;

import javax.transaction.*;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;

public class MemTransactionManager implements TransactionManager {

    // Dummy transaction instance
    private static final Transaction TRANSACTION = new Transaction() {
        @Override public void commit() { }
        @Override public boolean delistResource(XAResource a, int b) { return false; }
        @Override public boolean enlistResource(XAResource a) { return false; }
        @Override public int getStatus() { return 0; }
        @Override public void registerSynchronization(Synchronization a) { }
        @Override public void rollback() { }
        @Override public void setRollbackOnly() { }
    };

    @Override
    public void begin() {}

    @Override
    public void begin(boolean readOnly) {}

    @Override
    public void commit() {}

    @Override
    public Transaction getTransaction() { return TRANSACTION; }

    @Override
    public void rollback() {}

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
        try {
            return withTransaction((Callable<T>)command, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

