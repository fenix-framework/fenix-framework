package pt.ist.fenixframework.backend.jvstmojb.repository;

public class PartialFormatException extends IllegalArgumentException {

    public PartialFormatException(String s) {
        super(s);
    }

    public PartialFormatException(String s, Throwable cause) {
        super(s, cause);
    }
}
