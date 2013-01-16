package pt.ist.fenixframework.backend.mem;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.core.AbstractTransaction;

public class MemTransaction extends AbstractTransaction {

    @Override
    protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
	    SecurityException, IllegalStateException, SystemException {

    }

    @Override
    protected void backendRollback() throws IllegalStateException, SystemException {

    }

}
