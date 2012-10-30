package pt.ist.fenixframework.dml;

/**
 * Thrown when the DmlCompiler encounters some problem.
 */
public class DmlCompilerException extends Exception {
    public DmlCompilerException(String message) {
	super(message);
    }

    public DmlCompilerException(String message, Throwable cause) {
	super(message, cause);
    }

    public DmlCompilerException(Throwable cause) {
	super(cause);
    }
}
