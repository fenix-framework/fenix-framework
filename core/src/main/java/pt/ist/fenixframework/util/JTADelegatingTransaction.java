package pt.ist.fenixframework.util;

import java.lang.ref.WeakReference;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.FenixAbstractTransaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;
import pt.ist.fenixframework.txintrospector.TxStats;

public class JTADelegatingTransaction extends FenixAbstractTransaction {

    private final WeakReference<jakarta.transaction.Transaction> delegateTxRef;

    public JTADelegatingTransaction(jakarta.transaction.Transaction delegateTx) {
        super();
        this.delegateTxRef = new WeakReference<jakarta.transaction.Transaction>(delegateTx);
    }

    private jakarta.transaction.Transaction getDelegateTx() {
        jakarta.transaction.Transaction delegateTx = delegateTxRef.get();
        if (delegateTx == null) {
            throw new IllegalStateException("Delegate transaction no longer exists");
        }
        return delegateTx;
    }

    @Override
    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, SystemException {
        getDelegateTx().commit();
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        return getDelegateTx().delistResource(xaRes, flag);
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        return getDelegateTx().enlistResource(xaRes);
    }

    @Override
    public int getStatus() throws SystemException {
        return getDelegateTx().getStatus();
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        getDelegateTx().registerSynchronization(sync);
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
        getDelegateTx().rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        getDelegateTx().setRollbackOnly();
    }

    private final TxIntrospector introspector = TxStats.newInstance();

    @Override
    public TxIntrospector getTxIntrospector() {
        return introspector;
    }

}
