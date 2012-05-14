package pt.ist.fenixframework.services;

public class ServiceManagerException extends Exception {
    private static final long serialVersionUID = -8465388874995985228L;

    public ServiceManagerException(String message) {
	super(message);
    }

    public ServiceManagerException(String message, Throwable cause) {
	super(message, cause);
    }
}
