package pt.ist.fenixframework;

public interface TransactionalCommand<T> {
    public T doIt() throws Exception;
}
