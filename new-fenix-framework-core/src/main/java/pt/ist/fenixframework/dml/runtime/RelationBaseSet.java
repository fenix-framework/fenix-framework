package pt.ist.fenixframework.dml.runtime;

public interface RelationBaseSet<E> {
    public void justAdd(E elem);
    public void justRemove(E elem);
}
