package pt.ist.fenixframework.backend.jvstmojb.pstm;

public class VersionNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = -3876251268720915840L;

    VersionNotAvailableException(String attribute, Object object) {
        super("Couldn't reload attribute '" + attribute + "' of " + object + ". Check if the object exists in the database!");
    }

    VersionNotAvailableException(String attribute, Object object, Throwable e) {
        super("Couldn't reload attribute '" + attribute + "' of " + object + " due to an exception", e);
    }

}
