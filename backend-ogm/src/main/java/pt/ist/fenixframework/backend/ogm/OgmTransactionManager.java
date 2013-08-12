package pt.ist.fenixframework.backend.ogm;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.hibernate.ejb.AvailableSettings;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.ogm.datastore.infinispan.impl.InfinispanDatastoreProvider;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.infinispan.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.util.Misc;
import pt.ist.fenixframework.util.TxMap;

public class OgmTransactionManager implements TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(OgmTransactionManager.class);

    private javax.transaction.TransactionManager delegateTxManager;
    EntityManagerFactory emf;

    void setupTxManager(OgmConfig config) {
        if (logger.isTraceEnabled()) {
            Misc.traceClassLoaderHierarchy(logger);
        }
        HashMap properties = new HashMap();
        properties.put(AvailableSettings.INTERCEPTOR, AllocationInterceptor.class.getName());
        properties.put(InfinispanDatastoreProvider.INFINISPAN_CONFIGURATION_RESOURCENAME, config.getIspnConfigFile());

        emf = Persistence.createEntityManagerFactory("fenixframework-persistence-unit", properties);
        if (logger.isDebugEnabled()) {
            logger.debug("Created EntityManagerFactory: " + emf);
        }

        SessionFactoryImplementor sessionFactory =
            (SessionFactoryImplementor)((HibernateEntityManagerFactory)emf).getSessionFactory();
        delegateTxManager = sessionFactory.getServiceRegistry().getService(JtaPlatform.class).
            retrieveTransactionManager();
    }

    private final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<EntityManager>();

    EntityManager getEntityManager() {
        return currentEntityManager.get();
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
	begin(false);
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
        if (readOnly) {
            if (logger.isWarnEnabled()) {
                logger.warn("OgmBackEnd does not enforce read-only transactions. Starting as normal transaction");
            }
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Begin transaction");
        }
        delegateTxManager.begin();

        EntityManager em = null;
        em = emf.createEntityManager();
        currentEntityManager.set(em);
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException,
                                HeuristicRollbackException, SystemException {
        if (logger.isTraceEnabled()) {
            logger.trace("Commit transaction");
        }

	pt.ist.fenixframework.Transaction tx = getTransaction();

	try {
	    for (CommitListener listener : listeners) {
		listener.beforeCommit(tx);
	    }
	} catch (RuntimeException e) {
	    /**
	     * As specified in CommitListener.beforeCommit(), any unchecked
	     * exception will cause the transaction to be rolled back.
	     */
	    rollback();
	    throw new RollbackException(e.getMessage());
	}
	try {
	    EntityManager em = currentEntityManager.get();

	    em.flush();
	    delegateTxManager.commit();
	    em.close();

	    currentEntityManager.set(null);
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
        if (logger.isTraceEnabled()) {
            logger.trace("Rollback transaction");
        }
        delegateTxManager.rollback();

        currentEntityManager.set(null);
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
        try {
        	Atomic atomic = null;
            return withTransaction(command, atomic);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command, String transactionalClassId) {
        return withTransaction(command);
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
    	Atomic atomic = null;
	return withTransaction(command, atomic);
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
                // the purpose of this test is to enable reuse of the existing transaction
                if (getTransaction() == null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("No previous transaction.  Will begin a new one.");
                    }
                    begin();
                    inTopLevelTransaction = true;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Already inside a transaction. Not nesting.");
                    }
                }
                // do some work
                result = command.call();
                if (inTopLevelTransaction) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Will commit a top-level transaction.");
                    }
                    commit();
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Leaving an inner transaction.");
                    }
                }
                txFinished = true;
                return result;
            } catch (CacheException ce) {
                //If the execution fails
                logException(ce);
            } catch(RollbackException re) {
                //If the transaction was marked for rollback only, the transaction is rolled back and this exception is thrown.
                logException(re);
            } catch(HeuristicMixedException hme) {
                //If a heuristic decision was made and some some parts of the transaction have been committed while other parts have been rolled back.
                //Pedro -- most of the time, happens when some nodes fails...
                logException(hme);
            } catch(HeuristicRollbackException hre) {
                //If a heuristic decision to roll back the transaction was made
                logException(hre);
            } catch (Exception e) { // any other exception gets out
                if (logger.isDebugEnabled()) {
                    logger.debug("Exception within transaction", e);
                }
                throw e;
            } finally {
                if (!txFinished) {
                    try {
                        rollback();
                    } catch(IllegalStateException ise) {
                        // If the transaction is in a state where it cannot be rolled back.
                        // Pedro -- happen when the commit fails. When commit fails, it invokes the rollback().
                        //          so rollback() will be invoked again, but the transaction no longer exists
                        // Pedro -- just ignore it
                    } catch (Exception ex) {
                        if (logger.isErrorEnabled()) {
                            logger.error("Exception while aborting transaction");
                        }
                        ex.printStackTrace();
                    }
                }
            }
            // Pedro had this wait here.  Why?
            // waitingBeforeRetry();

            if (logger.isDebugEnabled()) {
                logger.debug("Retrying transaction: " + command);
            }
        }
        // never reached
        throw new RuntimeException("code never reached");
    }


    // private static final Random rand = new Random();
    // private void waitingBeforeRetry() {
    //     try {
    //         //smf: why so long?!
    //         Thread.sleep(rand.nextInt(10000));
    //     } catch(InterruptedException ie) {
    //         //do nothing
    //     }
    // }

    private void logException(Exception e) {
        if (logger.isInfoEnabled()) {
            logger.info("Exception caught in transaction: " + e.getLocalizedMessage());
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

    private final ConcurrentLinkedQueue<CommitListener> listeners = new ConcurrentLinkedQueue<CommitListener>();

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

	@Override
	public <T> T withTransaction(Callable<T> command, String transactionalClassId) throws Exception {
		return withTransaction(command);
	}

}

