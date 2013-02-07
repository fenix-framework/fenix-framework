package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

/**
 * This class handles one side of a many-to-something relation: the
 * side that keeps the collection corresponding to the multiplicity
 * many.
 */

public abstract class RoleMany<C1 extends DomainObject,C2 extends DomainObject> implements Role<C1,C2> {
    public boolean add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if ((o1 != null) && (o2 != null)) {
            return getSet(o1).justAdd(o2);
        }
        return false;
    }

    public boolean remove(C1 o1, C2 o2) {
        if ((o1 != null) && (o2 != null)) {
            return getSet(o1).justRemove(o2);
        }
        return false;
    }

    public abstract RelationBaseSet<C2> getSet(C1 o1);
}
