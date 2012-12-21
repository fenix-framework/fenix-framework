package pt.ist.fenixframework.backend.ogm;

import java.util.concurrent.Callable;

import javax.transaction.NotSupportedException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.CallableWithoutException;
import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class OgmBootingTransactionManager implements TransactionManager {

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

	@Override
	public TxIntrospector getTxIntrospector() {
	    return null;
	}
    };

    @Override
    public void begin() {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public int getStatus() {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public void resume(javax.transaction.Transaction tobj) {
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

    @Override
    public void setTransactionTimeout(int seconds) {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public Transaction suspend() {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public Transaction getTransaction() {
	return BOOTING_TRANSACTION;
    }

    @Override
    public <T> T withTransaction(CallableWithoutException<T> command) {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public <T> T withTransaction(Callable<T> command) throws Exception {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public <T> T withTransaction(Callable<T> command, Atomic atomic) throws Exception {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public void begin(boolean readOnly) throws NotSupportedException, SystemException {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public void addCommitListener(CommitListener listener) {
	throw new UnsupportedOperationException("Booting OGM");
    }

    @Override
    public void removeCommitListener(CommitListener listener) {
	throw new UnsupportedOperationException("Booting OGM");
    }

}

