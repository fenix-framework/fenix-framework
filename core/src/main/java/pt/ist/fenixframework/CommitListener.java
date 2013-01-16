package pt.ist.fenixframework;

/**
 * Represents a listener that will be notified whenever a {@link Transaction} is
 * committed. This interface is meant to be implemented by users of the
 * Framework.
 * 
 * Note that the implementation of this interface MUST be thread-safe, since
 * there may be several Transactions being committed at once, causing the
 * methods in the listener to be called concurrently.
 * 
 */
public interface CommitListener {

    /**
     * This method is called before any {@link Transaction} is committed. Any
     * unchecked exception throw by this method is going to cause the
     * transaction to be rolled back instead of committed.
     * 
     * @param transaction
     *            The transaction about to be committed.
     */
    public void beforeCommit(Transaction transaction);

    /**
     * This method is called after any {@link Transaction} is committed. The
     * outcome of the transaction can be determined by calling
     * {@code Transaction.getStatus()}
     * 
     * @param transaction
     *            The transaction that was committed/rolled-back.
     */
    public void afterCommit(Transaction transaction);

}