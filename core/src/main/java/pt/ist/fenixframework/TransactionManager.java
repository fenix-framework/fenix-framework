package pt.ist.fenixframework;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

// import pt.ist.fenixframework.atomic.AtomicContext;

/**
 * Fenix Framework's interface for all Transaction Managers.  This interface is similar to {@link
 * javax.transaction.TransactionManager}'s interface with some extensions added.
 *
 * @see javax.transaction
 */
public interface TransactionManager {
    /**
     * Create a new transaction and associate it with the current thread.
     *
     * @throws NotSupportedException Thrown if the thread is already associated with a transaction
     * and the Transaction Manager implementation does not support nested transactions.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition that prevents future transaction services from proceeding.
     */
    public void begin() throws NotSupportedException, SystemException;

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
    public void commit() throws RollbackException, HeuristicMixedException,
                                HeuristicRollbackException, SystemException;

    /**
     * Get the transaction object that represents the transaction context of the calling thread.
     *
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition.
     */
    public javax.transaction.Transaction getTransaction() throws SystemException;

    /**
     * Roll back the transaction associated with the current thread. When this method completes, the
     * thread becomes associated with no transaction.
     *
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     * condition.
     */
    public void rollback() throws SystemException;

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
    public <T> T withTransaction(Callable<T> command) throws Exception;

    /**
     * Transactionally execute a command, possibly returning a result.
     * @param command The command to execute
     * @param atomic the configuration for the execution of this command.
     */
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception;

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
    public void begin(boolean readOnly) throws NotSupportedException, SystemException;
    
}
