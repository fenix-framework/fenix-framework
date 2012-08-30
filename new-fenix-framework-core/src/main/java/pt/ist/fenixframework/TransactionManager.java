package pt.ist.fenixframework;

public interface TransactionManager {
    /* Create a new transaction and associate it with the current thread. */
    public void begin();

    /* Complete the transaction associated with the current thread. When this method completes, the
     * thread is no longer associated with a transaction. */
    public void commit();

    // public int getStatus();
    public Transaction getTransaction();
    // public void resume(Transaction tobj);
    public void rollback();
    // public void setRollbackOnly();
    // public void setTransactionTimeout(int seconds);
    // public Transaction suspend();

    /**
     * Transactionally execute a command, possibly returning a result.
     */
    public <T> T withTransaction(TransactionalCommand<T> command);


    // non-JTA API
    public void begin(boolean readOnly);
    
}
