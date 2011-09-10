package pt.ist.fenixframework;

public class FFDomainException extends Error {
    
    public FFDomainException(String message, Throwable cause) {
	super(message, cause);
    }

    public FFDomainException(String message) {
	super(message);
    }

    public FFDomainException(Throwable cause) {
	super(cause);
    }
}
