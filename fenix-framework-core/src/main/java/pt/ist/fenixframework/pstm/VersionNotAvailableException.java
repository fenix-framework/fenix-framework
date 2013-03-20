package pt.ist.fenixframework.pstm;

public class VersionNotAvailableException extends RuntimeException {

    public VersionNotAvailableException() {
    }

    public VersionNotAvailableException(final String message) {
        super(message);
    }

}
