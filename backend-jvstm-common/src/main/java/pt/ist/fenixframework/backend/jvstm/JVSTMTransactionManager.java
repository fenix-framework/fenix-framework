package pt.ist.fenixframework.backend.jvstm;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.Transaction;
import jvstm.WriteOnReadException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.backend.jvstm.pstm.JvstmInFenixTransaction;
import pt.ist.fenixframework.core.AbstractTransactionManager;
import pt.ist.fenixframework.core.WriteOnReadError;

public class JVSTMTransactionManager extends AbstractTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(JVSTMTransactionManager.class);

    protected static final Atomic DEFAULT_ATOMIC = new Atomic() {
        @Override
        public TxMode mode() {
            return TxMode.SPECULATIVE_READ;
        }

        @Override
        public boolean flattenNested() {
            return true;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return pt.ist.fenixframework.Atomic.class;
        }

    };

    /*
     * JVSTMTransactions are stored in a thread local, because JVSTM's are also.
     */
    private final ThreadLocal<JVSTMTransaction> transactions = new ThreadLocal<JVSTMTransaction>();

    @Override
    public void begin(boolean readOnly) throws NotSupportedException {
        JVSTMTransaction parent = transactions.get();

        logger.trace("Begin {}Transaction. Read Only: {}", (parent != null ? "(nested)" : ""), readOnly);

        JvstmInFenixTransaction underlying = (JvstmInFenixTransaction) Transaction.begin(readOnly);

        transactions.set(new JVSTMTransaction(underlying, parent));
    }

    @Override
    public JVSTMTransaction getTransaction() {
        return transactions.get();
    }

    @Override
    public void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
        JVSTMTransaction currentTx = transactions.get();

        try {
            logger.trace("Committing Transaction");

            // Note that no null check is needed, because a
            // check has been made in the super-class.
            currentTx.commit();
        } finally {
            transactions.set(currentTx.getParent());
        }
    }

    @Override
    public void backendRollback() throws SystemException {
        JVSTMTransaction currentTx = transactions.get();

        try {
            logger.trace("Rolling Back Transaction");

            // Note that no null check is needed, because a
            // check has been made in the super-class.
            currentTx.rollback();
        } finally {
            transactions.set(currentTx.getParent());
        }

    }

    @Override
    public void resume(javax.transaction.Transaction tobj) throws InvalidTransactionException, IllegalStateException,
            SystemException {
        if (!(tobj instanceof JVSTMTransaction)) {
            throw new InvalidTransactionException("Expected JVSTMTransaction, got " + tobj);
        }

        if (transactions.get() != null) {
            throw new IllegalStateException("Already associated with a transaction!");
        }

        JVSTMTransaction tx = (JVSTMTransaction) tobj;

        Transaction.resume((jvstm.Transaction) tx.getUnderlyingTransaction());
        transactions.set(tx);
    }

    @Override
    public JVSTMTransaction suspend() {
        JVSTMTransaction current = transactions.get();

        if (current == null) {
            return current;
        }

        Transaction.suspend();
        transactions.remove();

        return current;
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new UnsupportedOperationException("Transaction timeouts are not supported in JVSTM");
    }

    @Override
    // allow unchecked exceptions to pass to the caller
    public <T> T withTransaction(CallableWithoutException<T> command) {
        try {
            return withTransaction(command, DEFAULT_ATOMIC);
        } catch (RuntimeException e) {
            throw e;
        } catch (Error e) {
            throw e;
        } catch (Exception e) {
            throw new Error("Unexpected exception ocurred while running transaction", e);
        }
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
        return withTransaction(command, DEFAULT_ATOMIC);
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception, NotSupportedException {
        final String commandName = command.getClass().getName();

        logger.trace("Handling callable {}", commandName);

        // preset based on atomic defaults
        boolean readOnly = false;
        boolean tryReadOnly = true;

        if (atomic != null) {
            readOnly = (atomic.mode() == TxMode.READ);
            tryReadOnly = readOnly || (atomic.mode() == TxMode.SPECULATIVE_READ);
        }

        int tries = 0;

        while (true) {
            begin(tryReadOnly);
            tries++;

            T result = null;
            boolean commandFinished = false;

            try {
                result = command.call();
                commandFinished = true;
            } catch (WriteOnReadException e) {
                tryReadOnly = handleWriteOnRead(commandName, readOnly, tryReadOnly, tries, e);
            } catch (WriteOnReadError e) {
                tryReadOnly = handleWriteOnRead(commandName, readOnly, tryReadOnly, tries, e);
            } catch (UnableToDetermineIdException e) {
                if (!readOnly) {
                    tryReadOnly = false;
                }
                logTransactionRestart(commandName, e, tries);
            } catch (Exception e) {
                // just log any other exception
                logGenericException(commandName, e, tries);
                throw e;
            } finally {
                try {
                    if (commandFinished) {
                        commit();
                        return result;
                    } else {
                        rollback();
                    }
                } catch (Exception e) {
                    logger.trace("Exception on transaction {}: {}", (commandFinished ? "commit" : "rollback"), e);
                }
            }
        }
    }

    private boolean handleWriteOnRead(final String commandName, boolean readOnly, boolean tryReadOnly, int tries, Throwable e)
            throws WriteOnReadError {
        if (readOnly) {
            // read-only transactions will end
            logTransactionAbort(commandName, e, tries);
            // note that transaction will be rolled back in the finally block
            if (e instanceof WriteOnReadError) {
                throw (WriteOnReadError) e;
            } else {
                throw new WriteOnReadError(e);
            }
        } else {
            // read-write transactions will now be restarted as read-write
            tryReadOnly = false;
            logTransactionRestart(commandName, e, tries);
        }
        return tryReadOnly;
    }

    private void logTransactionRestart(String commandName, Throwable cause, int tries) {
        logger.debug("Transaction {} has been restarted {} time(s) because of {}", commandName, tries, cause.getClass()
                .getSimpleName());
    }

    private void logTransactionAbort(String commandName, Throwable cause, int tries) {
        logger.debug("Aborting transaction {} after {} executions because of {}", commandName, tries, cause.getClass()
                .getSimpleName());
    }

    private void logGenericException(String commandName, Throwable cause, int tries) {
        logger.debug("Transaction {} after {} executions throws {}", commandName, tries, cause.getClass().getSimpleName());
    }

}
