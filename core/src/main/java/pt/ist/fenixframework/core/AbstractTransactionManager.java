package pt.ist.fenixframework.core;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;

/**
 * Abstract implementation of {@link TransactionManager}.
 * 
 * This class provides management of {@link CommitListener}s, as well as
 * delegating several operations to the underlying transaction.
 * 
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    private final ConcurrentLinkedQueue<CommitListener> listeners = new ConcurrentLinkedQueue<CommitListener>();

    @Override
    public void begin() throws NotSupportedException, SystemException {
	begin(false);
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
	    IllegalStateException, SystemException {

	Transaction toCommit = getTransaction();

	if (toCommit == null)
	    throw new IllegalStateException();

	try {
	    for (CommitListener listener : listeners) {
		listener.beforeCommit(toCommit);
	    }
	} catch (RuntimeException e) {
	    /**
	     * As specified in CommitListener.beforeCommit(), any unchecked
	     * exception will cause the transaction to be rolled back.
	     */
	    rollback();
	    throw new RollbackException(e.getMessage());
	}

	backendCommit();

	for (CommitListener listener : listeners) {
	    listener.afterCommit(toCommit);
	}

    }

    /**
     * @see pt.ist.fenixframework.TransactionManager#addCommitListener(pt.ist.fenixframework.CommitListener)
     */
    @Override
    public void addCommitListener(CommitListener listener) {
	listeners.add(listener);
    }

    /**
     * @see pt.ist.fenixframework.TransactionManager#removeCommitListener(pt.ist.fenixframework.CommitListener)
     */
    @Override
    public void removeCommitListener(CommitListener listener) {
	listeners.remove(listener);
    }

    /**
     * @see javax.transaction.TransactionManager#getStatus()
     */
    @Override
    public int getStatus() throws SystemException {
	return this.getTransaction().getStatus();
    }

    /**
     * @see javax.transaction.TransactionManager#rollback()
     */
    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {

	Transaction toRollback = getTransaction();

	if (toRollback == null)
	    throw new IllegalStateException();

	backendRollback();

	for (CommitListener listener : listeners) {
	    listener.afterCommit(toRollback);
	}
    }

    /**
     * @see javax.transaction.TransactionManager#setRollbackOnly()
     */
    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
	this.getTransaction().setRollbackOnly();
    }

    protected abstract void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	    SecurityException, IllegalStateException, SystemException;

    protected abstract void backendRollback() throws SecurityException, SystemException;

}
