package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.core.CommitError;
import pt.ist.fenixframework.core.WriteOnReadError;

public class /*enum*/ DefaultAtomicContext implements AtomicContext {

    // FLATTEN_READONLY(true, true),
    // FLATTEN_READWRITE(true, false),
    // READ_ONLY(false, true),
    // READ_WRITE(false, false);

    private static final Logger logger = LoggerFactory.getLogger(DefaultAtomicContext.class);

    private final Atomic atomic;

    DefaultAtomicContext(Atomic atomic)   {
        this.atomic = atomic;
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
        
        return tm.withTransaction(method, this.atomic);
    }
}
