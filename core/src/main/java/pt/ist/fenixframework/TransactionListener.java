package pt.ist.fenixframework;

import jakarta.transaction.Transaction;

public interface TransactionListener {
    public void notifyBeforeBegin();

    public void notifyAfterBegin(Transaction transaction);
}
