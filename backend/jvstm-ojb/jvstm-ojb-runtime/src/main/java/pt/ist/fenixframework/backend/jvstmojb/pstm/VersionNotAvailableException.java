package pt.ist.fenixframework.backend.jvstmojb.pstm;

public class VersionNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = -3876251268720915840L;

    public VersionNotAvailableException(String message) {
        super(message);
    }

}
