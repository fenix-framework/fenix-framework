package pt.ist.fenixframework.backend.jvstm;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.backend.jvstm.pstm.GenericTopLevelTransaction;
import pt.ist.fenixframework.core.AbstractTransactionManager;
import pt.ist.fenixframework.core.WriteOnReadError;

public class JVSTMTransactionManager extends AbstractTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(JVSTMTransactionManager.class);

    /*
     * JVSTMTransactions are stored in a thread local, because JVSTM's are also.
     */
    private final ThreadLocal<JVSTMTransaction> transactions = new ThreadLocal<JVSTMTransaction>();

    @Override
    public void begin(boolean readOnly) throws NotSupportedException {
        if (transactions.get() != null) {
            throw new NotSupportedException("Nesting is not yet supported in JVSTM based backends");
        }

        logger.trace("Begin Transaction. Read Only: {}", readOnly);

        GenericTopLevelTransaction underlying = (GenericTopLevelTransaction) Transaction.begin(readOnly);

        transactions.set(new JVSTMTransaction(underlying));
    }

    @Override
    public JVSTMTransaction getTransaction() {
        return transactions.get();
    }

    @Override
    public void backendCommit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
        try {
            logger.trace("Committing Transaction");

            // Note that no null check is needed, because a
            // check has been made in the super-class.
            transactions.get().commit();
        } finally {
            transactions.remove();  // smf: if an exception occurs during commit, should we still do this??  Won't the transaction be neeed for rollback?
        }
    }

    @Override
    public void backendRollback() throws SystemException {
        try {
            logger.trace("Rolling Back Transaction");

            // Note that no null check is needed, because a
            // check has been made in the super-class.
            transactions.get().rollback();
        } finally {
            transactions.remove();
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

        Transaction.resume(tx.getUnderlyingTransaction());
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
    public <T> T withTransaction(CallableWithoutException<T> command) {
        try {
            return handleWriteCommand(command, false);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception ocurred while running transaction", e);
        }
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
        return handleWriteCommand(command, false);
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
        boolean readOnly = atomic != null && atomic.readOnly();
        boolean speculativeReadOnly = atomic != null && atomic.speculativeReadOnly();

        if (readOnly) {
            return handleReadCommand(command);
        } else {
            return handleWriteCommand(command, speculativeReadOnly);
        }
    }

    private <T> T handleReadCommand(Callable<T> command) throws Exception {
        if (getTransaction() != null) {
            // Piggy-back on the currently running transaction, 
            // be it read-only or not
            return command.call();
        }

        begin(true);
        try {
            return command.call();
        } finally {
            commit();
        }
    }

    // Service Handling - Write Transactions

    private static final Map<String, String> knownWriteServices = new ConcurrentHashMap<String, String>();

    private <T> T handleWriteCommand(Callable<T> command, boolean speculativeReadOnly) throws Exception {

        final String commandName = command.getClass().getName();

        logger.trace("Handling service {}", commandName);

        if (getTransaction() != null && getTransaction().txAllowsWrite()) {
            // Piggy-back this write transaction in the currently running
            // write transaction
            logger.trace("Inside write transaction. Flattenning...");
            return command.call();
        }

        boolean promotedTransaction = false;

        if (getTransaction() != null) {
            // Commit currently running read-only transaction.
            // Due to JVSTM, this is guaranteed to never fail.
            commit();

            // If a read transaction was already running, checkpoint
            // the current transaction instead of committing it.
            promotedTransaction = true;
        }

        boolean readOnly = speculativeReadOnly ? !knownWriteServices.containsKey(commandName) : false;

        boolean keepGoing = true;
        int tries = 0;

        try {
            while (keepGoing) {
                tries++;
                try {
                    try {
                        begin(readOnly);
                        T result = command.call();
                        if (promotedTransaction) {
                            if (!readOnly) {
                                // Do nothing if the current transaction did not write anything.
                                checkpoint();
                            }
                        } else {
                            commit();
                        }
                        keepGoing = false;
                        return result;
                    } finally {
                        if (keepGoing) {
                            logger.trace("Transaction failed to commit.");
                            if (getTransaction() != null) {
                                rollback();
                            }
                        }
                    }
                } catch (RollbackException e) {
                    if (tries > 3) {
                        logTransactionRestart(commandName, e, tries);
                    }
                } catch (UnableToDetermineIdException e) {
                    if (tries > 3) {
                        logTransactionRestart(commandName, e, tries);
                    }
                } catch (WriteOnReadError e) {
                    logger.trace("Restarting transaction due to WriteOnReadError");
                    knownWriteServices.put(commandName, commandName);
                    readOnly = false;
                    if (tries > 3) {
                        logTransactionRestart(commandName, e, tries);
                    }
                }
            }
        } finally {
            if (promotedTransaction && getTransaction() == null) {
                // We were inside a transaction when we entered, but now we are not!
                begin(true);
            }
        }

        throw new RuntimeException("This could should never be reached!");
    }

    private void checkpoint() {
        logger.trace("Checkpointing Transaction");
        JVSTMTransaction transaction = getTransaction();
        for (CommitListener listener : listeners) {
            listener.beforeCommit(transaction);
        }
        Transaction.checkpoint();
        for (CommitListener listener : listeners) {
            listener.afterCommit(transaction);
        }
        transaction.setReadOnly();
        logger.trace("Transaction is now read-only");
    }

    private void logTransactionRestart(String service, Throwable cause, int tries) {
        logger.warn("Service {} has been restarted {} times because of {}", service, tries, cause.getClass().getSimpleName());
    }

}
