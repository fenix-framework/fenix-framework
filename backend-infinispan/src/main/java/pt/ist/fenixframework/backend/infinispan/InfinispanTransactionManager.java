package pt.ist.fenixframework.backend.infinispan;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.infinispan.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.util.TxMap;

public class InfinispanTransactionManager implements TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanTransactionManager.class);

    private static javax.transaction.TransactionManager delegateTxManager;

    private final ConcurrentLinkedQueue<CommitListener> listeners = new ConcurrentLinkedQueue<CommitListener>();

    void setDelegateTxManager(javax.transaction.TransactionManager delegate) {
	delegateTxManager = delegate;
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
	begin(false);
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
	if (readOnly) {
	    logger.warn("InfinispanBackEnd does not enforce read-only transactions. Starting as normal transaction");
	}
	logger.trace("Begin transaction");
	delegateTxManager.begin();
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
	logger.trace("Commit transaction");

	pt.ist.fenixframework.Transaction tx = getTransaction();

	for (CommitListener listener : listeners) {
	    listener.beforeCommit(tx);
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
	try {
	    Transaction tx = delegateTxManager.getTransaction();
	    return TxMap.getTx(tx);
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public void rollback() throws SystemException {
	logger.trace("Rollback transaction");
	delegateTxManager.rollback();
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	try {
	    return withTransaction(command, null);
	} catch (Exception e) {
	    throw new RuntimeException(e);
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
	T result = null;
	boolean txFinished = false;
	while (!txFinished) {
	    try {
		boolean inTopLevelTransaction = false;
		// the purpose of this test is to enable reuse of the existing
		// transaction
		if (getTransaction() == null) {
		    logger.trace("No previous transaction.  Will begin a new one.");
		    begin();
		    inTopLevelTransaction = true;
		} else {
		    logger.trace("Already inside a transaction. Not nesting.");
		}
		// do some work
		result = command.call();
		if (inTopLevelTransaction) {
		    logger.trace("Will commit a top-level transaction.");
		    commit();
		} else {
		    logger.trace("Leaving an inner transaction.");
		}
		txFinished = true;
		return result;
	    } catch (CacheException ce) {
		// If the execution fails
		logException(ce);
	    } catch (RollbackException re) {
		// If the transaction was marked for rollback only, the
		// transaction is rolled back and this exception is thrown.
		logException(re);
	    } catch (HeuristicMixedException hme) {
		// If a heuristic decision was made and some some parts of the
		// transaction have been committed while other parts have been
		// rolled back.
		// Pedro -- most of the time, happens when some nodes fails...
		logException(hme);
	    } catch (HeuristicRollbackException hre) {
		// If a heuristic decision to roll back the transaction was made
		logException(hre);
	    } catch (Exception e) { // any other exception out
		logger.debug("Exception within transaction", e);
		throw e;
	    } finally {
		if (!txFinished) {
		    try {
			rollback();
		    } catch (IllegalStateException ise) {
			// If the transaction is in a state where it cannot be
			// rolled back.
			// Pedro -- happen when the commit fails. When commit
			// fails, it invokes the rollback().
			// so rollback() will be invoked again, but the
			// transaction no longer exists
			// Pedro -- just ignore it
		    } catch (Exception ex) {
			logger.error("Exception while aborting transaction");
			ex.printStackTrace();
		    }
		}
	    }
	    // Pedro had this wait here. Why?
	    // waitingBeforeRetry();

	    logger.debug("Retrying transaction: " + command);
	}
	// never reached
	throw new RuntimeException("code never reached");
    }

    // private static final Random rand = new Random();
    // private void waitingBeforeRetry() {
    // try {
    // //smf: why so long?!
    // Thread.sleep(rand.nextInt(10000));
    // } catch(InterruptedException ie) {
    // //do nothing
    // }
    // }

    private void logException(Exception e) {
	logger.info("Exception caught in transaction: " + e.getLocalizedMessage());
	logger.trace("Exception caught in transaction:", e);
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

}
