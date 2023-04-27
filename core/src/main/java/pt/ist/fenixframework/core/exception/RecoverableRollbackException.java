package pt.ist.fenixframework.core.exception;

import pt.ist.fenixframework.core.TransactionError;

/**
 * This exception is used to indicate that a exception occurred while
 * committing a {@link Transaction}, from which the Framework knows
 * how to recover.
 * 
 * This is typically thrown in response to a {@link TransactionError},
 * and is recovered by restarting the transaction.
 * 
 * @author João Pedro Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
public class RecoverableRollbackException extends FenixRollbackException {

    private static final long serialVersionUID = -845641341892984423L;

    public RecoverableRollbackException(Throwable cause) {
        super(cause);
    }

    public RecoverableRollbackException(Throwable cause, String message) {
        super(cause, message);
    }

}
