package pt.ist.fenixframework.backend.ogm;

import java.util.concurrent.Callable;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.TransactionManager;

public class OgmBootingTransactionManager extends TransactionManager {

    private static final Transaction BOOTING_TRANSACTION = new Transaction() {
        @Override
        public void commit() {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public boolean delistResource(XAResource arg0, int arg1) {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public boolean enlistResource(XAResource arg0) {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public int getStatus() {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public void registerSynchronization(Synchronization arg0) {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public void rollback() {
            throw new UnsupportedOperationException("Booting OGM");
        }

        @Override
        public void setRollbackOnly() {
            throw new UnsupportedOperationException("Booting OGM");
        }
    };

    @Override
    protected void backendBegin(boolean readOnly) {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    protected void backendCommit() {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    protected Transaction backendGetTransaction() {
        return BOOTING_TRANSACTION;
    }

    @Override
    protected void backendRollback() {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    protected <T> T backendWithTransaction(CallableWithoutException<T> command) {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    protected <T> T backendWithTransaction(Callable<T> command) {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    protected <T> T backendWithTransaction(Callable<T> command, Atomic atomic) {
        throw new UnsupportedOperationException("Booting OGM");
    }

}

