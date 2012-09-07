package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionalCommand;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.core.CommitError;
import pt.ist.fenixframework.core.WriteOnReadError;

public enum DefaultAtomicContext implements AtomicContext {

    FLATTEN_READONLY(true, true),
    FLATTEN_READWRITE(true, false),
    READ_ONLY(false, true),
    READ_WRITE(false, false);

    private static final Logger logger = Logger.getLogger(DefaultAtomicContext.class);

    private final boolean flattenTx;
    private final boolean tryReadOnly;

    private DefaultAtomicContext(boolean flatten, boolean speculativeReadOnly)   {
        flattenTx = flatten;
        tryReadOnly = speculativeReadOnly;
    }

    // @Override
    // public final <V> V doTransactionally(Callable<V> method) throws Exception {
    //     if (logger.isDebugEnabled()) {
    //         logger.debug("Handling @Atomic call from " + Thread.currentThread().getStackTrace()[2]);
    //     }
    //     TransactionManager tm = FenixFramework.getTransactionManager();

    //     boolean inTransaction = (tm.getTransaction() != null);
    //     if (flattenTx && inTransaction) {
    //         return method.call();
    //     }

    //     boolean readOnly = tryReadOnly;
    //     while (true) {
    //         tm.begin(readOnly);
    //         boolean txFinished = false;
    //         try {
    //             V result = method.call();
    //             tm.commit();
    //             txFinished = true;
    //             return result;
    //         } catch (WriteOnReadError wore) {
    //             tm.rollback();
    //             txFinished = true;
    //             readOnly = false;
    //         } catch (CommitError ce) {
    //             tm.rollback();
    //             txFinished = true;
    //         } finally {
    //             if (!txFinished) {
    //                 tm.rollback();
    //             }
    //         }
    //     }
    // }

    @Override
    public final <V> V doTransactionally(final Callable<V> method) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling @Atomic call from " + Thread.currentThread().getStackTrace()[2]);
        }
        TransactionManager tm = FenixFramework.getTransactionManager();
        
        return tm.withTransaction(new TransactionalCommand<V>() {
                public V doIt() throws Exception {
                    return method.call();
                }
            });
    }

}
