package pt.ist.fenixframework.backend.infinispan;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;

import org.apache.log4j.Logger;

import org.infinispan.CacheException;

import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;

public class InfinispanTransactionManager implements TransactionManager {
    private static final Logger logger = Logger.getLogger(InfinispanTransactionManager.class);

    public void begin() {}
    public void begin(boolean readOnly) {}
    public void commit() {}
    public Transaction getTransaction() { return null; }
    public void rollback() {}

    public <T> T withTransaction(TransactionalCommand<T> command) {

        //smf: ISTO PROVAVELMENTE TEM DE IR (TAMBEM?) PARA UM ATOMIC PROCESSOR!!!!!!

        T result = null;
        boolean txFinished = false;
        while (!txFinished) {
            //smf: IdentityMap localIdMap = null;
            try {
                boolean inTopLevelTransaction = false;
                // the purpose of this test is to enable reuse of the existing transaction
                if (getTransaction() == null) {
                    logger.trace("No previous transaction.  Beginning a new one.");
                    begin();
                    inTopLevelTransaction = true;
                }
                //smf: localIdMap = new LocalIdentityMap();
                //smf: perTxIdMap.set(localIdMap);
                // do some work
                result = command.doIt();
                if (inTopLevelTransaction) {
                    commit();
                }
                txFinished = true;
                return result;
            } catch (CacheException ce) {
                //If the execution fails
                logException(ce);
            // } catch(RollbackException re) {
            //     //If the transaction was marked for rollback only, the transaction is rolled back and this exception is thrown.
            //     logException(re);
            // } catch(HeuristicMixedException hme) {
            //     //If a heuristic decision was made and some some parts of the transaction have been committed while other parts have been rolled back.
            //     //Pedro -- most of the time, happens when some nodes fails...
            //     logException(hme);
            // } catch(HeuristicRollbackException hre) {
            //     //If a heuristic decision to roll back the transaction was made
            //     logException(hre);
            } catch (Exception e) { // any other exception 	 out
                logger.debug("Exception within transaction", e);
                throw new RuntimeException(e);
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
                        logger.error("Exception while aborting transaction");
                        ex.printStackTrace();
                    }
                }
                //smf: perTxIdMap.set(null);
            }
            // Pedro had this wait here.  Why?
            // waitingBeforeRetry();

            logger.debug("Retrying transaction: " + command);
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
        logger.info("Exception caught in transaction: " + e.getLocalizedMessage());
        logger.trace("Exception caught in transaction:", e);
    }

}

