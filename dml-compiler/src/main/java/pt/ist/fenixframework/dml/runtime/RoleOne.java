package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

/**
 * This class handles one side of a one-to-something relation: the
 * side that keeps the single reference corresponding to the
 * multiplicity one.
 */

public abstract class RoleOne<C1 extends DomainObject,C2 extends DomainObject> implements Role<C1,C2> {
    public boolean add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if (o1 != null) {
            C2 old2 = getValue(o1);
            if (o2 != old2) {
                relation.remove(o1, old2);
                setValue(o1, o2);
            }
        }
        return true;
    }

    public boolean remove(C1 o1, C2 o2) {
        if (o1 != null) {
            setValue(o1, null);
        }
        return true;
    }

    public abstract C2 getValue(C1 o1);
    public abstract void setValue(C1 o1, C2 o2);
}
