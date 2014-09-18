package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

public interface Relation<C1 extends DomainObject, C2 extends DomainObject> {
    public boolean add(C1 o1, C2 o2);

    public boolean remove(C1 o1, C2 o2);

    public Relation<C2, C1> getInverseRelation();

    public String getName();

}
