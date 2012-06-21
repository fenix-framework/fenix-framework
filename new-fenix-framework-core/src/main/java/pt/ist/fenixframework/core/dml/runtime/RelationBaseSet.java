package pt.ist.fenixframework.core.dml.runtime;

public interface RelationBaseSet<E> {
    public void justAdd(E elem);
    public void justRemove(E elem);
}
