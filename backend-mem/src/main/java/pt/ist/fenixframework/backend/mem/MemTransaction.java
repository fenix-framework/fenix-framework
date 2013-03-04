package pt.ist.fenixframework.backend.mem;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import pt.ist.fenixframework.core.AbstractTransaction;
import pt.ist.fenixframework.txintrospector.TxStats;

public class MemTransaction extends AbstractTransaction {

    private final TxStats txIntrospector = TxStats.newInstance();

    @Override
    protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {

    }

    @Override
    protected void backendRollback() throws IllegalStateException, SystemException {

    }

    @Override
    public TxStats getTxIntrospector() {
        return txIntrospector;
    }

}
