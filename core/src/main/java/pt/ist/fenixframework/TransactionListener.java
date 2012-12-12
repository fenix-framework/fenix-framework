package pt.ist.fenixframework;

import javax.transaction.Transaction;

public interface TransactionListener {
	public void notifyBeforeBegin();
	public void notifyAfterBegin(Transaction transaction);
}
