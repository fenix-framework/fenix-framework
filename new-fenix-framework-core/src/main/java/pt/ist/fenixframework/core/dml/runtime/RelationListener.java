package pt.ist.fenixframework.core.dml.runtime;

public interface RelationListener<C1,C2> {
    public void beforeAdd(Relation<C1,C2> rel, C1 o1, C2 o2);
    public void afterAdd(Relation<C1,C2> rel, C1 o1, C2 o2);
    public void beforeRemove(Relation<C1,C2> rel, C1 o1, C2 o2);
    public void afterRemove(Relation<C1,C2> rel, C1 o1, C2 o2);
}
