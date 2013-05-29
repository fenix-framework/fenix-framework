package pt.ist.fenixframework.backend.jvstmojb;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.CommitException;
import jvstm.Transaction;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TopLevelTransaction;
import pt.ist.fenixframework.core.AbstractTransaction;
import pt.ist.fenixframework.core.TransactionError;
import pt.ist.fenixframework.core.WriteOnReadError;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class JvstmOJBTransaction extends AbstractTransaction {

    private final TopLevelTransaction underlyingTransaction;

    private boolean markedSpeculative = false;

    JvstmOJBTransaction(TopLevelTransaction underlyingTransaction) {
        super();
        this.underlyingTransaction = underlyingTransaction;
    }

    TopLevelTransaction getUnderlyingTransaction() {
        return underlyingTransaction;
    }

    /**
     * Returns whether a write command can run within this transaction
     * (flattening the command).
     * 
     * A transaction can be flattened if is marked as being speculative
     * read-only (in which case {@link WriteOnReadError}s thrown by the
     * command should be handled by this transaction) or if the underlying
     * transaction is already a write transaction.
     */
    boolean canFlattenWriteCommand() {
        return !underlyingTransaction.isReadOnly() || markedSpeculative;
    }

    void setSpeculative(boolean speculative) {
        markedSpeculative = speculative;
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
            throw new JvstmTransactionError();
        }
    }

    @Override
    protected void backendRollback() throws IllegalStateException, SystemException {
        if (Transaction.current() != underlyingTransaction) {
            throw new IllegalStateException("JVSTM does not support committing transactions from other threads!");
        }

        Transaction.abort();
    }

    private static final class JvstmTransactionError extends TransactionError {
        private static final long serialVersionUID = 2031232654005283536L;
    }

    // TxIntrospector

    @Override
    public TxIntrospector getTxIntrospector() {
        return underlyingTransaction;
    }

}
