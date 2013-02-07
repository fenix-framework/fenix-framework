package pt.ist.fenixframework.backend.jvstmmem;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.core.AbstractTransaction;

public class JVSTMMemTransaction extends AbstractTransaction {

    @Override
    protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	    SecurityException, IllegalStateException, SystemException {
	jvstm.Transaction.commit();	
    }

    @Override
    protected void backendRollback() throws IllegalStateException, SystemException {
	jvstm.Transaction.abort();
    }

}
