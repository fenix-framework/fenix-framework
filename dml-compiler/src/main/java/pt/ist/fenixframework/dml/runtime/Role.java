package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

public interface Role<C1 extends DomainObject,C2 extends DomainObject> {
    public boolean add(C1 o1, C2 o2, Relation<C1,C2> rel);
    public boolean remove(C1 o1, C2 o2);
    public Role<C2,C1> getInverseRole();
}
