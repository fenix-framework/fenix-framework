package pt.ist.fenixframework.backend.jvstmojb;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.core.AbstractTransactionManager;

public class JvstmOJBTransactionManager extends AbstractTransactionManager {

    @Override
    public void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public void backendRollback() throws SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public pt.ist.fenixframework.Transaction getTransaction() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
	jvstm.Transaction.begin();
	try {
	    return command.call();
	} finally {
	    jvstm.Transaction.commit();
	}
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public Transaction suspend() throws SystemException {
	// TODO Auto-generated method stub
	return null;
    }

}
