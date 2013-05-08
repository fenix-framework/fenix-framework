package pt.ist.fenixframework.util;

import java.lang.ref.WeakReference;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.FenixAbstractTransaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;
import pt.ist.fenixframework.txintrospector.TxStats;

public class JTADelegatingTransaction extends FenixAbstractTransaction {

    private final WeakReference<javax.transaction.Transaction> delegateTxRef;

    public JTADelegatingTransaction(javax.transaction.Transaction delegateTx) {
        super();
        this.delegateTxRef = new WeakReference<javax.transaction.Transaction>(delegateTx);
    }

    private javax.transaction.Transaction getDelegateTx() {
        javax.transaction.Transaction delegateTx = delegateTxRef.get();
        if (delegateTx == null) {
            throw new IllegalStateException("Delegate transaction no longer exists");
        }
        return delegateTx;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
            SystemException {
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
