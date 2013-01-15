package pt.ist.fenixframework.backend.jvstmmem;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;

public class JVSTMMemTransactionManager implements TransactionManager {

    @Override
    public void begin() {
        begin(false);
    }

    @Override
    public void begin(boolean readOnly) {
	jvstm.Transaction.begin(readOnly);
    }

    @Override
    public void commit() {
	jvstm.Transaction.commit();
    }

    @Override
    public Transaction getTransaction() {
	throw new RuntimeException("Should not had been called!");
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
	    return jvstm.Transaction.doIt(command);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-11);
	    return null;
	}
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) {
	try {
	    jvstm.Transaction.beginInevitable();
	    T res = command.call();
	    jvstm.Transaction.commit();
	    return res;
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-11);
	    return null;
	}
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	jvstm.Transaction.beginInevitable();
	T res = command.call();
	jvstm.Transaction.commit();
	return res;
    }

    @Override
    public void addCommitListener(CommitListener listener) {}

    @Override
    public void removeCommitListener(CommitListener listener) {}


}
