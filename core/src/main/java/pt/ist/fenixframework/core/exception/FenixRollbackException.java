package pt.ist.fenixframework.core.exception;

import javax.transaction.RollbackException;

/**
 * This subclass of {@link RollbackException} only exists because
 * it is the only way to provide the exception that caused the
 * transaction to be rolled-back, so it can be properly
 * thrown inside 'withTransaction' methods.
 * 
 * The main goal of this exception is to wrap-programmer thrown
 * exceptions, so that managed environments can abort execution
 * and throw the correct exception to the programmer.
 * 
 * @author João Pedro Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
public class FenixRollbackException extends RollbackException {

    private static final long serialVersionUID = 5839930506950371469L;

    private final Throwable cause;

    public FenixRollbackException(Throwable cause) {
        this.cause = cause;
    }

    public FenixRollbackException(Throwable cause, String message) {
        super(message);
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

}
