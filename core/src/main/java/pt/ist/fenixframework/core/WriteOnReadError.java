package pt.ist.fenixframework.core;

/**
 * An instance of <code>WriteOnReadError</code> is thrown whenever a write attempt is made within a
 * read-only transaction.
 *
 * @see CommitError
 */
public class WriteOnReadError extends CommitError {
    private static final long serialVersionUID = 1L;
    protected WriteOnReadError() { super(); }
}
