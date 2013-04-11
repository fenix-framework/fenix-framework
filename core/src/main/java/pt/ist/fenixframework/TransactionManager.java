package pt.ist.fenixframework;

import java.util.concurrent.Callable;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.core.WriteOnReadError;

/**
 * Fenix Framework's interface for all Transaction Managers. This interface is
 * similar to {@link javax.transaction.TransactionManager}'s interface with some
 * extensions added.
 * 
 * Please refer to the documentation in each individual backend for the list of
 * supported operations.
 * 
 * @see javax.transaction
 */
public interface TransactionManager extends javax.transaction.TransactionManager {

    /**
     * Get the {@link Transaction} object that represents the transaction
     * context of the calling thread.
     * 
     * Subsequent calls to this method, while in the scope of a given
     * transaction, will always return the same instance.
     * 
     */
    @Override
    public Transaction getTransaction();

    /**
     * Transactionally execute a command, possibly returning a result.
     * Implementations of this method normally invoke {@link
     * #withTransaction(Callable<T>, Atomic)} with a default atomic behavior.
     * 
     * @param command
     *            The command to execute.
     */
    public <T> T withTransaction(CallableWithoutException<T> command);

    /**
     * Transactionally execute a command, possibly returning a result.
     * Implementations of this method normally invoke {@link
     * #withTransaction(Callable<T>, Atomic)} with a default atomic behavior.
     * 
     * @param command
     *            The command to execute.
     */
    public <T> T withTransaction(Callable<T> command) throws Exception;

    /**
     * Transactionally execute a command, possibly returning a result.
     * 
     * @param command
     *            The command to execute
     * @param atomic
     *            the configuration for the execution of this command.
     */
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception;

    /**
     * Create a new transaction and associate it with the current thread. This
     * method can be used by the programmer to ensure that a transaction will
     * execute as read-only. Invoking this method with <code>true</code> attempts
     * to create a read-only transaction. Within such transaction any attempt
     * to perform a write operation will throw a WriteOnReadError.
     * 
     * @throws NotSupportedException
     *             Thrown if the thread is already associated with a transaction
     *             and the Transaction Manager implementation does not support
     *             nested transactions.
     * @throws SystemException
     *             Thrown if the transaction manager encounters an unexpected
     *             error condition that prevents future transaction services
     *             from proceeding.
     * @throws WriteOnReadError
     *             Thrown if an attempt is made to begin a write transaction
     *             within an existing read-only transaction (in case nested
     *             transactions are supported).
     */
    // para alem de rever o texto, explica que pode sair um writeonreaderror! se nao for possivel fazer o begin

    public void begin(boolean readOnly) throws NotSupportedException, SystemException, WriteOnReadError;

    /**
     * Registers a commit listener, that will be called whenever any {@link Transaction}managed by this Manager is committed.
     * 
     * @param listener
     *            The listener to be added.
     * @throws NullPointerException
     *             If the listener is null.
     */
    public void addCommitListener(CommitListener listener);

    /**
     * Unregisters the given commit listener.
     * 
     * @param listener
     *            The listener to be removed.
     */
    public void removeCommitListener(CommitListener listener);

}
