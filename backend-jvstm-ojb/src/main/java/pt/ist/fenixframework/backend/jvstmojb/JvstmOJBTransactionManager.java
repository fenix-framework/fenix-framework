package pt.ist.fenixframework.backend.jvstmojb;

import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.TransactionManager;

public class JvstmOJBTransactionManager implements TransactionManager {

    @Override
    public void begin() throws NotSupportedException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public Transaction getTransaction() throws SystemException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void rollback() throws SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public <T> T withTransaction(Callable<T> command) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
	// TODO Auto-generated method stub

    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	// TODO Auto-generated method stub
	return null;
    }

}
