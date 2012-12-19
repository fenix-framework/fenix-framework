package pt.ist.fenixframework.backend.jvstmmem;

import java.util.concurrent.Callable;

import javax.transaction.Transaction;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.TransactionManager;

public class JVSTMMemTransactionManager implements TransactionManager {

    @Override
    public void begin() {
	jvstm.Transaction.begin();
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

}
