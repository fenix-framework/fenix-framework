package pt.ist.fenixframework.backend.jvstm.repository;

public class PersistenceException extends RuntimeException {
    public PersistenceException() {
    }

    public PersistenceException(Throwable e) {
        super(e);
    }

    public PersistenceException(String msg, Throwable e) {
        super(msg, e);
    }

    public PersistenceException(String msg) {
        super(msg);
    }
}
