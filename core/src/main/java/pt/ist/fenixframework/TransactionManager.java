package pt.ist.fenixframework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

// import pt.ist.fenixframework.atomic.AtomicContext;

/**
 * Fenix Framework's abstract class for all Transaction Managers.  This class has an interface
 * similar to {@link javax.transaction.TransactionManager}'s interface with some extensions added.
 *
 * @see javax.transaction
 */
public abstract class TransactionManager implements javax.transaction.TransactionManager {

    private List<TransactionListener> listeners = null;

    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @throws NotSupportedException Thrown if the thread is already associated with a transaction
     * and the Transaction Manager implementation does not support nested transactions.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition that prevents future transaction services from proceeding.
     */
    public final void begin() throws NotSupportedException, SystemException {
        begin(false);
    }

    /**
     * Complete the transaction associated with the current thread. When this method completes, the
     * thread is no longer associated with a transaction.
     *
     * @throws RollbackException Thrown to indicate that the transaction has been rolled back rather
     * than committed.
     * @throws HeuristicMixedException Thrown to indicate that a heuristic decision was made and
     * that some relevant updates have been committed while others have been rolled back.
     * @throws HeuristicRollbackException Thrown to indicate that a heuristic decision was made and
     * that some relevant updates have been rolled back.
     */
    public final void commit() throws RollbackException, HeuristicMixedException,
                                HeuristicRollbackException, SystemException {
        backendCommit();
    }

    /**
     * Get the transaction object that represents the transaction context of the calling thread.
     *
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition.
     */
    public final Transaction getTransaction() throws SystemException {
        Transaction tx = backendGetTransaction();
        return tx;
    }

    /**
     * Roll back the transaction associated with the current thread. When this method completes, the
     * thread becomes associated with no transaction.
     *
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition.
     */
    public final void rollback() throws SystemException {
        backendRollback();
    }

    // public int getStatus();
    // public void resume(Transaction tobj);
    // public void setRollbackOnly();
    // public void setTransactionTimeout(int seconds);
    // public Transaction suspend();


    // non-JTA API

    /**
     * Transactionally execute a command, possibly returning a result.  Implementations of this
     * method normally invoke {@link #withTransaction(Callable<T>, Atomic)} with a default atomic
     * behaviour.
     * @param command The command to execute.
     */
    public final <T> T withTransaction(CallableWithoutException<T> command) {
        T result = backendWithTransaction(command);
        return result;
    }

    /**
     * Transactionally execute a command, possibly returning a result.  Implementations of this
     * method normally invoke {@link #withTransaction(Callable<T>, Atomic)} with a default atomic
     * behaviour.
     * @param command The command to execute.
     */
    public final <T> T withTransaction(Callable<T> command) throws Exception {
        T result = backendWithTransaction(command);
        return result;
    }

    /**
     * Transactionally execute a command, possibly returning a result.
     * @param command The command to execute
     * @param atomic the configuration for the execution of this command.
     */
    public final <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
        T result = backendWithTransaction(command, atomic);
        return result;
    }

    /**
     * Create a new transaction and associate it with the current thread.  This method can be used
     * as a hint from the programmer that she does (or does not) intend do perform write operations.
     * As such, implementations may optimize for the read-only case, and fail as soon as a write is
     * attempted.  However, if during the transaction the program attempts any write, and the
     * transactional system is able to cope with the request, it may proceed without failing.  In
     * other words, write transactions are not guaranteed to rollback (i.e. they may commit) when
     * invoking this method with <code>true</code>.
     *
     * @throws NotSupportedException Thrown if the thread is already associated with a transaction
     * and the Transaction Manager implementation does not support nested transactions.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition that prevents future transaction services from proceeding.
     */
    public final void begin(boolean readOnly) throws NotSupportedException, SystemException {
        if (listeners != null) {
            for (TransactionListener listener : listeners) {
                listener.notifyBeforeBegin();
            }
        }

        backendBegin(readOnly);
        Transaction tx = backendGetTransaction();
        assert (tx != null);

        if (listeners != null) {
            for (TransactionListener listener : listeners) {
                listener.notifyAfterBegin(tx);
            }
        }
    }

    // For listeners

    public final void registerForBegin(TransactionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<TransactionListener>();
        }
        listeners.add(listener);
    }

    // Abstract
    protected abstract void backendBegin(boolean readOnly) throws NotSupportedException, SystemException;
    protected abstract void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException;
    protected abstract Transaction backendGetTransaction() throws SystemException;
    protected abstract void backendRollback() throws SystemException;
    protected abstract <T> T backendWithTransaction(CallableWithoutException<T> command);
    protected abstract <T> T backendWithTransaction(Callable<T> command) throws Exception;
    protected abstract <T> T backendWithTransaction(Callable<T> command, Atomic atomic) throws Exception;

    @Override
    public int getStatus() throws SystemException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void resume(Transaction transaction) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setRollbackOnly() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setTransactionTimeout(int timeout) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Transaction suspend() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
