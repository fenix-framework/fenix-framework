package pt.ist.fenixframework.hibernatesearch;

import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.backend.spi.WorkType;

import pt.ist.fenixframework.CommitListener;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

/**
 * This class acts as a bridge between the current fenix framework transactional
 * backend, and hibernate search.
 * 
 * Before a transaction commits, this class uses the TxIntrospector interface to
 * obtain a list of new and changed objects, and feeds it to hibernate search
 * for indexing.
 * 
 * After a transaction commits or aborts, this class also signals the result to
 * hibernate-search, so that the index additions/changes for the transaction are
 * persisted or dropped.
 */
class CommitIndexer implements CommitListener {

    @Override
    public void beforeCommit(Transaction transaction) {

	TransactionContext context = new HibernateSearchTransactionContext(transaction);

	TxIntrospector introspector = transaction.getTxIntrospector();

	HibernateSearchSupport.updateIndex(context, introspector.getNewObjects(), WorkType.ADD);
	HibernateSearchSupport.updateIndex(context, introspector.getModifiedObjects(), WorkType.UPDATE);

    }

    @Override
    public void afterCommit(Transaction transaction) {

    }
}
