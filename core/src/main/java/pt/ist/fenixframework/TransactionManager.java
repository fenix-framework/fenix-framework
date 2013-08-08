package pt.ist.fenixframework;

import java.util.concurrent.Callable;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

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
     * #withTransaction(Callable<T>, Atomic)} with a default atomic behaviour.
     * 
     * @param command
     *            The command to execute.
     */
    public <T> T withTransaction(CallableWithoutException<T> command);

    /**
     * Transactionally execute a command, possibly returning a result.
     * Implementations of this method normally invoke {@link
     * #withTransaction(Callable<T>, Atomic)} with a default atomic behaviour.
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
     * Transactionally execute a command, possibly returning a result.
     * Additionally, the transactional class being executed is identified by the programmer.
     * 
     * @param command
     *            The command to execute
     * @param transactionalClassId
     *            Identifier of the transactional class being executed.
     */
    public <T> T withTransaction(Callable<T> command, String transactionalClassId) throws Exception;
    
    /**
     * Create a new transaction and associate it with the current thread. This
     * method can be used as a hint from the programmer that she does (or does
     * not) intend do perform write operations. As such, implementations may
     * optimize for the read-only case, and fail as soon as a write is
     * attempted. However, if during the transaction the program attempts any
     * write, and the transactional system is able to cope with the request, it
     * may proceed without failing. In other words, write transactions are not
     * guaranteed to rollback (i.e. they may commit) when invoking this method
     * with <code>true</code>.
     * 
     * @throws NotSupportedException
     *             Thrown if the thread is already associated with a transaction
     *             and the Transaction Manager implementation does not support
     *             nested transactions.
     * @throws SystemException
     *             Thrown if the transaction manager encounters an unexpected
     *             error condition that prevents future transaction services
     *             from proceeding.
     */
    public void begin(boolean readOnly) throws NotSupportedException, SystemException;

    /**
     * Registers a commit listener, that will be called whenever any
     * {@link Transaction} managed by this Manager is committed.
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
