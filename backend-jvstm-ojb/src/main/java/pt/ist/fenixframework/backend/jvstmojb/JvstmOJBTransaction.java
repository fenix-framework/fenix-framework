package pt.ist.fenixframework.backend.jvstmojb;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.CommitException;
import jvstm.Transaction;
import pt.ist.fenixframework.core.AbstractTransaction;
import pt.ist.fenixframework.core.CommitError;
import pt.ist.fenixframework.pstm.TopLevelTransaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class JvstmOJBTransaction extends AbstractTransaction {

    private final TopLevelTransaction underlyingTransaction;

    JvstmOJBTransaction(TopLevelTransaction underlyingTransaction) {
        super();
        this.underlyingTransaction = underlyingTransaction;
    }

    TopLevelTransaction getUnderlyingTransaction() {
        return underlyingTransaction;
    }

    // AbstractTransaction implementations

    @Override
    protected void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        if (Transaction.current() != underlyingTransaction) {
            throw new IllegalStateException("JVSTM does not support committing transactions from other threads!");
        }

        try {
            Transaction.commit();
        } catch (CommitException e) {
            throw new JvstmCommitError();
        }
    }

    @Override
    protected void backendRollback() throws IllegalStateException, SystemException {
        if (Transaction.current() != underlyingTransaction) {
            throw new IllegalStateException("JVSTM does not support committing transactions from other threads!");
        }

        Transaction.abort();
    }

    private static final class JvstmCommitError extends CommitError {
        private static final long serialVersionUID = 2031232654005283536L;
    }

    // TxIntrospector

    @Override
    public TxIntrospector getTxIntrospector() {
        return underlyingTransaction;
    }

}
