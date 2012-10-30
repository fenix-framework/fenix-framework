package pt.ist.fenixframework.core;

/**
 * An instance of <code>CommitError</code> is thrown whenever it occurs a situation that prevents a
 * transaction from committing.
 *
 * Note that this <code>Error</code> may be thrown even before the commit operation is invoked.
 *
 * An application should never catch instances of this class, as the purpose of throwing an instance
 * of this class is to make a non-local exit from the currently running transaction, and to deal
 * with the situation at an infrastrutural level.  This is done by the Fenix Framework runtime and
 * should not be masked by the application code in anyway.
 *
 * The class <code>CommitError</code> is specifically a subclass of <code>Error</code> rather than
 * <code>Exception</code>, even though it is a "normal occurrence", because many applications catch
 * all occurrences of <code>Exception</code> and then discard the exception.
 *
 */
public class CommitError extends Error {
    private static final long serialVersionUID = 1L;
    protected CommitError() { super(); }
}
