package pt.ist.fenixframework.backend.jvstm;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.CommitException;
import jvstm.Transaction;
import pt.ist.fenixframework.backend.jvstm.pstm.JvstmInFenixTransaction;
import pt.ist.fenixframework.core.AbstractTransaction;
import pt.ist.fenixframework.core.TransactionError;
import pt.ist.fenixframework.txintrospector.TxStats;

public class JVSTMTransaction extends AbstractTransaction {

    private final JVSTMTransaction parent;
    private final JvstmInFenixTransaction underlyingTransaction;

    JVSTMTransaction(JvstmInFenixTransaction underlyingTransaction, JVSTMTransaction parent) {
        super();
        this.underlyingTransaction = underlyingTransaction;
        this.parent = parent;
    }

    JvstmInFenixTransaction getUnderlyingTransaction() {
        return underlyingTransaction;
    }

    JVSTMTransaction getParent() {
        return this.parent;
    }

    void setReadOnly() {
        this.underlyingTransaction.setReadOnly();
    }

    boolean txAllowsWrite() {
        return underlyingTransaction.txAllowsWrite();
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

    private static final class JvstmCommitError extends TransactionError {
        private static final long serialVersionUID = 2031232654005283536L;
    }

    // TxIntrospector

    private final TxStats txIntrospector = TxStats.newInstance();

    @Override
    public TxStats getTxIntrospector() {
        return txIntrospector;
    }

}
