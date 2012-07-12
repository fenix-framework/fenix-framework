package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.core.exception.CommitError;
import pt.ist.fenixframework.core.exception.WriteOnReadError;

public enum DefaultAtomicContext implements AtomicContext {

    FLATTEN_READONLY(true, true),
    FLATTEN_READWRITE(true, false),
    READ_ONLY(false, true),
    READ_WRITE(false, false);

    private final boolean flattenTx;
    private final boolean tryReadOnly;

    private DefaultAtomicContext(boolean flatten, boolean speculativeReadOnly)   {
        flattenTx = flatten;
        tryReadOnly = speculativeReadOnly;
    }

    @Override
    public final <V> V doTransactionally(Callable<V> method) throws Exception {
        TransactionManager tm = FenixFramework.getTransactionManager();

        boolean inTransaction = (tm.getTransaction() != null);
        if (flattenTx && inTransaction) {
            return method.call();
        }

        boolean readOnly = tryReadOnly;
        while (true) {
            tm.begin(readOnly);
            boolean txFinished = false;
            try {
                V result = method.call();
                tm.commit();
                txFinished = true;
                return result;
            } catch (WriteOnReadError wore) {
                tm.rollback();
                txFinished = true;
                readOnly = false;
            } catch (CommitError ce) {
                tm.rollback();
                txFinished = true;
            } finally {
                if (!txFinished) {
                    tm.rollback();
                }
            }
        }
    }

}
