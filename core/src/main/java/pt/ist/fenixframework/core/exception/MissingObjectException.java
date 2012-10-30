package pt.ist.fenixframework.core.exception;

/**
 * Thrown to indicate that a requested object could not be found.  The message should provide the
 * identifier of the object that was requested.
 */
public class MissingObjectException extends RuntimeException {
    public MissingObjectException(String objId, Throwable cause) {
        super (objId, cause);
    }
}
