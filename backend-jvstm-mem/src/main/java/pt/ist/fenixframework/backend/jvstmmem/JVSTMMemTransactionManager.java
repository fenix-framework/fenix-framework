package pt.ist.fenixframework.backend.jvstmmem;

import java.util.concurrent.Callable;

import javax.transaction.Transaction;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.TransactionManager;

public class JVSTMMemTransactionManager extends TransactionManager {

    @Override
    public void backendBegin(boolean readOnly) {
	jvstm.Transaction.begin(readOnly);
    }

    @Override
    public void backendCommit() {
	jvstm.Transaction.commit();
    }

    @Override
    public Transaction backendGetTransaction() {
	throw new RuntimeException("Should not had been called!");
    }

    @Override
    public void backendRollback() {
	jvstm.Transaction.abort();
    }

    @Override
    public <T> T backendWithTransaction(Callable<T> command) {
	try {
	    return jvstm.Transaction.doIt(command);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-11);
	    return null;
	}
    }

    @Override
    public <T> T backendWithTransaction(Callable<T> command, Atomic atomic) {
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
    protected <T> T backendWithTransaction(CallableWithoutException<T> command) {
	jvstm.Transaction.beginInevitable();
	T res = command.call();
	jvstm.Transaction.commit();
	return res;
    }


}
