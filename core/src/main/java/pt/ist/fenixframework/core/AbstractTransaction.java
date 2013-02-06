package pt.ist.fenixframework.core;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.FenixAbstractTransaction;

/**
 * Abstract implementation of {@link pt.ist.fenixframework.Transaction}. This
 * class provides life-cycle management for free, as well as an implementation
 * of TxIntrospector, requiring concrete implementations to provide the behavior
 * for committing/rolling back.
 * 
 */
public abstract class AbstractTransaction extends FenixAbstractTransaction {

    /**
     * List of synchronizations associated with the current transaction.
     */
    protected final ConcurrentLinkedQueue<Synchronization> synchronizations = new ConcurrentLinkedQueue<Synchronization>();

    /**
     * Status of the Transaction.
     */
    protected int status = Status.STATUS_ACTIVE;

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
	    IllegalStateException, SystemException {

	if (this.status == Status.STATUS_MARKED_ROLLBACK) {
	    rollback();
	    throw new RollbackException();
	}

	if (this.status != Status.STATUS_ACTIVE)
	    throw new IllegalStateException();

	this.status = Status.STATUS_COMMITTING;

	try {
	    notifyBeforeCommit();
	} catch (Exception e) {

	    /*
	     * An exception in any synchronization's beforeCommit will cause the
	     * transaction to be rolled back.
	     */
	    rollback();

	    throw new RollbackException(e.getMessage());
	}
	try {
	    backendCommit();
	    this.status = Status.STATUS_COMMITTED;
	} catch (Exception e) {
	    rollback();
	    return;
	} catch (CommitError e) {
	    rollback();
	    return;
	}
	notifyAfterCommit();
    }

    protected void notifyBeforeCommit() {
	for (Synchronization sync : synchronizations) {
	    sync.beforeCompletion();
	}
    }

    protected void notifyAfterCommit() {
	for (Synchronization sync : synchronizations) {
	    sync.afterCompletion(this.status);
	}
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
	throw new UnsupportedOperationException("XA Resources are not supported.");
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
	throw new UnsupportedOperationException("XA Resources are not supported.");
    }

    @Override
    public int getStatus() throws SystemException {
	return this.status;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
	if (this.status == Status.STATUS_MARKED_ROLLBACK)
	    throw new RollbackException();

	if (this.status != Status.STATUS_ACTIVE)
	    throw new IllegalStateException();

	synchronizations.offer(sync);
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
	if (this.status == Status.STATUS_PREPARED || this.status == Status.STATUS_COMMITTED
		|| this.status == Status.STATUS_ROLLEDBACK)
	    throw new IllegalStateException();

	this.status = Status.STATUS_ROLLING_BACK;
	backendRollback();
	this.status = Status.STATUS_ROLLEDBACK;

	notifyAfterCommit();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
	this.status = Status.STATUS_MARKED_ROLLBACK;
    }


    /*
     * Abstract part
     */

    protected abstract void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	    SecurityException, IllegalStateException, SystemException;

    protected abstract void backendRollback() throws IllegalStateException, SystemException;

}
