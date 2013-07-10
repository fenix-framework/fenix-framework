package pt.ist.fenixframework.backend.infinispan;

import org.infinispan.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.util.TxMap;

import javax.transaction.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InfinispanTransactionManager implements TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanTransactionManager.class);
    private static javax.transaction.TransactionManager delegateTxManager;
    private static final RollBackOnlyException ROLL_BACK_ONLY_EXCEPTION = new RollBackOnlyException();
    private final ConcurrentLinkedQueue<CommitListener> listeners = new ConcurrentLinkedQueue<CommitListener>();

    @Override
    public void begin() throws NotSupportedException, SystemException {
        begin(false);
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
        if (readOnly) {
            logger.warn("InfinispanBackEnd does not enforce read-only transactions. Starting as normal transaction");
        }
        delegateTxManager.begin();
        if (logger.isTraceEnabled()) {
            logger.trace("Begin transaction: " + getTransaction());
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
        pt.ist.fenixframework.Transaction tx = getTransaction();
        if (logger.isTraceEnabled()) {
            logger.trace("Commit transaction: " + tx);
        }

        try {
            for (CommitListener listener : listeners) {
                listener.beforeCommit(tx);
            }
        } catch (RuntimeException e) {
            /**
             * As specified in CommitListener.beforeCommit(), any unchecked
             * exception will cause the transaction to be rolled back.
             */
            logger.warn("RuntimeException received. Rollback transaction");
            rollback();
            throw new RollbackException(e.getMessage());
        }
        try {
            delegateTxManager.commit();
        } finally {
            for (CommitListener listener : listeners) {
                listener.afterCommit(tx);
            }
        }
    }

    @Override
    public pt.ist.fenixframework.Transaction getTransaction() {
        Transaction tx;
        try {
            tx = delegateTxManager.getTransaction();
        } catch (SystemException e) {
            return null;
        }

        return (tx == null) ? null : TxMap.getTx(tx);
    }

    @Override
    public void rollback() throws SystemException {
        if (logger.isTraceEnabled()) {
            logger.trace("Rollback transaction: " + getTransaction());
        }
        delegateTxManager.rollback();
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
        try {
            return withTransaction(command, null);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
        return withTransaction(command, null);
    }

    /**
     * For now, it ignores the value of the atomic parameter.
     */
    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
        while (true) {
            boolean started = tryBegin();
            boolean success = false;
            boolean canRetry = false;
            boolean finished;
            T result = null;
            try {
                // do some work
                result = command.call();
                success = true;
            } catch (CacheException e) {
                logger.error("Error executing transaction " + getTransaction(), e);
                canRetry = true;
            } catch (RollBackOnlyException e) {
                logger.error("Rollback only exception caught! An inner transaction wants to rollback: " +
                        getTransaction());
                canRetry = true;
            } catch (RuntimeException e) {
                logger.error("Unexpected error executing transaction " + getTransaction(), e);
                throw e;
            } catch (Exception e) {
                logger.error("Application error executing transaction " + getTransaction(), e);
                throw e;
            } catch (Throwable t) {
                logger.error("Unexpected error executing transaction " + getTransaction(), t);
                throw new RuntimeException(t);
            } finally {
                finished = tryCommit(started, success, canRetry);
            }
            if (finished) {
                return result;
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Retrying transaction: " + command);
            }
        }
    }

    @Override
    public int getStatus() throws SystemException {
        return delegateTxManager.getStatus();
    }

    @Override
    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException {
        delegateTxManager.resume(tx);
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegateTxManager.setRollbackOnly();
    }

    @Override
    public void setTransactionTimeout(int timeout) throws SystemException {
        delegateTxManager.setTransactionTimeout(timeout);
    }

    @Override
    public Transaction suspend() throws SystemException {
        return delegateTxManager.suspend();
    }

    /**
     * @see pt.ist.fenixframework.TransactionManager#addCommitListener(pt.ist.fenixframework.CommitListener)
     */
    @Override
    public void addCommitListener(CommitListener listener) {
        listeners.add(listener);
    }

    /**
     * @see pt.ist.fenixframework.TransactionManager#removeCommitListener(pt.ist.fenixframework.CommitListener)
     */
    @Override
    public void removeCommitListener(CommitListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return {@code true} if the transaction is finished, {@code false} otherwise.
     */
    private boolean tryCommit(boolean started, boolean success, boolean canRetry) {
        if (!started) {
            if (logger.isTraceEnabled()) {
                logger.trace("Finishing inner transaction. Success? " + success);
            }
            //nested transaction. Don't even try to commit neither retrying if aborted. However, we nee to notify
            //the top-level transaction if we abort.
            if (!success && canRetry) {
                throw ROLL_BACK_ONLY_EXCEPTION;
            }
            return true;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Finishing top-level transaction. Success? " + success);
        }

        try {
            if (success) {
                commit();
            } else {
                rollback();
            }
            return success;
        } catch (RuntimeException e) {
            logger.error("Unexpected error finishing transaction", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error finishing transaction", e);
        } catch (Throwable t) {
            logger.error("Unexpected error finishing transaction", t);
            throw new RuntimeException(t);
        }

        return false;
    }

    /**
     * @return {@code true} if the begin has succeed, {@code false} otherwise.
     */
    private boolean tryBegin() {
        if (getTransaction() == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No previous transaction. Will begin a new one.");
            }
            try {
                begin();
            } catch (RuntimeException e) {
                logger.error("Error beginning transaction", e);
                throw e;
            } catch (Exception e) {
                logger.error("Error beginning transaction", e);
                throw new RuntimeException(e);
            } catch (Throwable t) {
                logger.error("Unexpected error beginning transaction", t);
                throw new RuntimeException(t);
            }
            return true;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Already inside a transaction. Not nesting.");
            }
        }
        return false;
    }

    void setDelegateTxManager(javax.transaction.TransactionManager delegate) {
        delegateTxManager = delegate;
    }

    private static class RollBackOnlyException extends RuntimeException {

    }

}
