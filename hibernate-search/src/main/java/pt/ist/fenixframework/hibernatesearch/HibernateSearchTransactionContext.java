package pt.ist.fenixframework.hibernatesearch;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.hibernate.search.backend.TransactionContext;

import pt.ist.fenixframework.Transaction;

public class HibernateSearchTransactionContext implements TransactionContext {

    private final Transaction transaction;

    HibernateSearchTransactionContext(Transaction transaction) {
	this.transaction = transaction;
    }

    // TransactionContext implementation

    @Override
    public Object getTransactionIdentifier() {
	return this;
    }

    @Override
    public boolean isTransactionInProgress() {
	try {
	    return transaction.getStatus() == Status.STATUS_ACTIVE;
	} catch (SystemException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
	try {
	    transaction.registerSynchronization(synchronization);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

}
