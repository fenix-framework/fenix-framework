package pt.ist.fenixframework.backend.jvstmmem;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import jvstm.CommitException;
import jvstm.WriteOnReadException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;

public class JVSTMMemTransactionManager implements TransactionManager {

    private static final ThreadLocal<JVSTMMemTransaction> currentJPATx = new ThreadLocal<JVSTMMemTransaction>() {};
    
    @Override
    public void begin() {
        begin(false);
    }

    @Override
    public void begin(boolean readOnly) {
	jvstm.Transaction.begin(readOnly);
	currentJPATx.set(new JVSTMMemTransaction());
    }

    @Override
    public void commit() {
	jvstm.Transaction.commit();
    }

    @Override
    public Transaction getTransaction() {
	return currentJPATx.get();
    }

    @Override
    public int getStatus() throws SystemException {
        return Status.STATUS_ACTIVE;
    }

    @Override
    public void resume(javax.transaction.Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException { }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException { }

    @Override
    public void setTransactionTimeout(int timeout) throws SystemException { }

    @Override
    public Transaction suspend() throws SystemException {
        return null;
    }

    @Override
    public void rollback() {
	jvstm.Transaction.abort();
    }

    @Override
    public <T> T withTransaction(Callable<T> command) {
	try {
	    return withTransaction(command, false);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-11);
	    return null;
	}
    }
    
    private <T> T withTransaction(Callable<T> command, boolean isReadOnly) throws Exception {
	T result = null;
	while (true) {
	    begin(isReadOnly);
	    boolean finished = false;
	    try {
		result = command.call();
		commit();
		finished = true;
		return result;
	    } catch (CommitException ce) {
		rollback();
		finished = true;
	    } finally {
		if (!finished) {
		    rollback();
		}
	    }
	}
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) {
	try {
	    return withTransaction(command, atomic.readOnly());
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-11);
	    return null;
	}
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	jvstm.Transaction.beginInevitable();
	currentJPATx.set(new JVSTMMemTransaction());
	T res = command.call();
	jvstm.Transaction.commit();
	return res;
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command, String transactionalClassId) {
        return withTransaction(command);
    }

    @Override
    public void addCommitListener(CommitListener listener) {}

    @Override
    public void removeCommitListener(CommitListener listener) {}

	@Override
	public <T> T withTransaction(Callable<T> command, String transactionalClassId) throws Exception {
		return withTransaction(command);
	}

}
