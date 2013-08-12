package pt.ist.fenixframework.backend.mem;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.core.AbstractTransactionManager;

public class MemTransactionManager extends AbstractTransactionManager {

    private MemTransaction transaction;

    @Override
    public Transaction getTransaction() {
	return this.transaction;
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	if (transaction != null)
	    return command.call();

	try {
	    T ret = null;
	    begin();
	    ret = command.call();
	    commit();
	    return ret;
	} catch (RuntimeException e) {
	    try {
		rollback();
	    } catch (Exception ex) {
		throw new RuntimeException(ex);
	    }
	    throw e;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command, String transactionalClassId) {
        return withTransaction(command);
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
	if (transaction != null)
	    return command.call();

	try {
	    T ret = null;
	    begin();
	    ret = command.call();
	    commit();
	    return ret;
	} catch (Exception e) {
	    rollback();
	    throw e;
	}
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
	if (transaction != null)
	    return command.call();

	try {
	    T ret = null;
	    begin();
	    ret = command.call();
	    commit();
	    return ret;
	} catch (Exception e) {
	    rollback();
	    throw e;
	}
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
	this.transaction = new MemTransaction();
    }

    @Override
    public void resume(javax.transaction.Transaction tobj) throws InvalidTransactionException, IllegalStateException,
	    SystemException {
	if (!(tobj instanceof MemTransaction))
	    throw new InvalidTransactionException(String.valueOf(tobj));

	this.transaction = (MemTransaction) tobj;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
	throw new UnsupportedOperationException("Timeouts are not supported.");
    }

    @Override
    public javax.transaction.Transaction suspend() throws SystemException {
	Transaction tx = this.transaction;

	this.transaction = null;

	return tx;
    }

    @Override
    protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
	    IllegalStateException, SystemException {
	this.transaction.commit();
	this.transaction = null;
    }

    @Override
    protected void backendRollback() throws SecurityException, SystemException {
	this.transaction.rollback();
	this.transaction = null;
    }

	@Override
	public <T> T withTransaction(Callable<T> command, String transactionalClassId) throws Exception {
		return withTransaction(command);
	}
}
