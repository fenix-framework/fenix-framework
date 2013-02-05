package pt.ist.fenixframework.dml.runtime;

public interface RelationBaseSet<E> {
    public boolean justAdd(E elem);
    public boolean justRemove(E elem);
}
