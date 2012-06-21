package pt.ist.fenixframework.core.dml.runtime;

public interface Role<C1,C2> {
    public void add(C1 o1, C2 o2, Relation<C1,C2> rel);
    public void remove(C1 o1, C2 o2);
    public Role<C2,C1> getInverseRole();
}
