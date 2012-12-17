package pt.ist.fenixframework.hibernatesearch;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.hibernate.search.backend.spi.WorkType;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

/**
 * This class acts as a bridge between the current fenix framework transactional backend, and hibernate search.
 *
 * Before a transaction commits, this class uses the TxIntrospector interface to obtain a list of new and changed
 * objects, and feeds it to hibernate search for indexing.
 *
 * After a transaction commits or aborts, this class also signals the result to hibernate-search, so that the
 * index additions/changes for the transaction are persisted or dropped.
 */
class CommitIndexer implements Synchronization, org.hibernate.search.backend.TransactionContext {

    private Synchronization hibernateSearchSynchronization;

    // Synchronization implementation

    @Override
    public void beforeCompletion() {
        TxIntrospector introspector = TxIntrospector.getTxIntrospector();

        HibernateSearchSupport.updateIndex(this, introspector.getNewObjects(), WorkType.ADD);
        HibernateSearchSupport.updateIndex(this, introspector.getModifiedObjects(), WorkType.UPDATE);

        if (hibernateSearchSynchronization != null) {
            // The synchronization object can be null whenever no work has been scheduled, because no
            // new or modified objects of @Indexed classes were found
            hibernateSearchSynchronization.beforeCompletion();
        }
    }

    @Override
    public void afterCompletion(int status) {
        if (hibernateSearchSynchronization != null) {
            hibernateSearchSynchronization.afterCompletion(status);
        }
    }

    // TransactionContext implementation

    @Override
    public Object getTransactionIdentifier() {
        return this;
    }

    @Override
    public boolean isTransactionInProgress() {
        try {
            return FenixFramework.getTransactionManager().getTransaction().getStatus() == Status.STATUS_ACTIVE;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        if (hibernateSearchSynchronization != null) {
            throw new RuntimeException("Unexpected double call to registerSynchronization");
        }
        hibernateSearchSynchronization = synchronization;
    }
}
