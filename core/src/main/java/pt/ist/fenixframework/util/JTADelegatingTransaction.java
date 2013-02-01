package pt.ist.fenixframework.util;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;

import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.txintrospector.TxStats;

public class JTADelegatingTransaction implements Transaction {

    private final javax.transaction.Transaction delegateTx;

    private final TxStats txIntrospector = TxStats.newInstance();

    public JTADelegatingTransaction(javax.transaction.Transaction delegateTx) {
	super();
	this.delegateTx = delegateTx;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
	    SystemException {
	delegateTx.commit();
    }

    @Override
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
	return delegateTx.delistResource(xaRes, flag);
    }

    @Override
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
	return delegateTx.enlistResource(xaRes);
    }

    @Override
    public int getStatus() throws SystemException {
	return delegateTx.getStatus();
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
	delegateTx.registerSynchronization(sync);
    }

    @Override
    public void rollback() throws IllegalStateException, SystemException {
	delegateTx.rollback();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
	delegateTx.setRollbackOnly();
    }

    @Override
    public TxStats getTxIntrospector() {
	return txIntrospector;
    }

}
