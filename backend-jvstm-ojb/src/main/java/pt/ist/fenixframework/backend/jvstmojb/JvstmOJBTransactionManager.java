package pt.ist.fenixframework.backend.jvstmojb;

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
import pt.ist.fenixframework.backend.jvstmojb.pstm.AbstractDomainObject.UnableToDetermineIdException;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TopLevelTransaction;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TransactionSupport;
import pt.ist.fenixframework.core.AbstractTransactionManager;
import pt.ist.fenixframework.core.WriteOnReadError;

public class JvstmOJBTransactionManager extends AbstractTransactionManager {

    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBTransactionManager.class);

    /*
     * JvstmOJBTransactions are stored in a thread local, because JVSTM's are also.
     */
    private final ThreadLocal<JvstmOJBTransaction> transactions = new ThreadLocal<JvstmOJBTransaction>();

    @Override
    public void begin(boolean readOnly) throws NotSupportedException {
        if (transactions.get() != null) {
            throw new NotSupportedException("Nesting is not yet supported in JVSTM based backends");
        }

        logger.trace("Begin Transaction. Read Only: {}", readOnly);

        TopLevelTransaction underlying = (TopLevelTransaction) Transaction.begin(readOnly);

        transactions.set(new JvstmOJBTransaction(underlying));
    }

    @Override
    public JvstmOJBTransaction getTransaction() {
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
            transactions.remove();
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
        if (!(tobj instanceof JvstmOJBTransaction)) {
            throw new InvalidTransactionException("Expected JvstmOJBTransaction, got " + tobj);
        }

        if (transactions.get() != null) {
            throw new IllegalStateException("Already associated with a transaction!");
        }

        JvstmOJBTransaction tx = (JvstmOJBTransaction) tobj;

        Transaction.resume(tx.getUnderlyingTransaction());
        transactions.set(tx);
    }

    @Override
    public JvstmOJBTransaction suspend() {
        JvstmOJBTransaction current = transactions.get();

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
            throw new Error("Exception ocurred while running transaction", e);
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
            if (getTransaction() != null) {
                commit();
            } else {
                logger.trace("Aborting read-only transaction due to an exception!");
            }
        }
    }

    // Service Handling - Write Transactions

    private static final Map<String, String> knownWriteServices = new ConcurrentHashMap<String, String>();

    private <T> T handleWriteCommand(Callable<T> command, boolean speculativeReadOnly) throws Exception {

        final String commandName = command.getClass().getName();

        logger.trace("Handling service {}", commandName);

        if (getTransaction() != null && !getTransaction().isReadOnly()) {
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
                        rollback();
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

        throw new RuntimeException("This could should never be reached!");
    }

    private void checkpoint() {
        logger.trace("Checkpointing Transaction");
        JvstmOJBTransaction transaction = getTransaction();
        for (CommitListener listener : listeners) {
            listener.beforeCommit(transaction);
        }
        Transaction.checkpoint();
        for (CommitListener listener : listeners) {
            listener.afterCommit(transaction);
        }
        TransactionSupport.currentFenixTransaction().setReadOnly();
        logger.trace("Transaction is now read-only");
    }

    private void logTransactionRestart(String service, Throwable cause, int tries) {
        logger.warn("Service {} has been restarted {} times because of {}", service, tries, cause.getClass().getSimpleName());
    }

}
