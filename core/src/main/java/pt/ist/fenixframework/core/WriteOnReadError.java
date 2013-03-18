package pt.ist.fenixframework.core;

/**
 * An instance of <code>WriteOnReadError</code> is thrown whenever a write attempt is made within a
 * read-only transaction.
 * 
 * @see TransactionError
 */
public class WriteOnReadError extends TransactionError {
    private static final long serialVersionUID = 1L;

    public WriteOnReadError() {
        super();
    }
}
