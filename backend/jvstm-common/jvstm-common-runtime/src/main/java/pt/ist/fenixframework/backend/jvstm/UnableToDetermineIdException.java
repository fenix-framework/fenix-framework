package pt.ist.fenixframework.backend.jvstm;

public class UnableToDetermineIdException extends RuntimeException {
    private static final long serialVersionUID = 3085208774911959073L;

    public UnableToDetermineIdException(Throwable cause) {
        super("Unable to determine id Exception", cause);
    }
}
