package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

/**
 * This class handles one side of a relation which is not navigable in
 * the other direction.
 */

public class RoleEmpty<C1 extends DomainObject,C2 extends DomainObject> implements Role<C1,C2> {
    private Role<C2,C1> inverseRole;

    public RoleEmpty(Role<C2,C1> inverseRole) {
        this.inverseRole = inverseRole;
    }

    public boolean add(C1 o1, C2 o2, Relation<C1,C2> relation) {
	return true;
    }
    
    public boolean remove(C1 o1, C2 o2) {
	return true;
    }

    public Role<C2,C1> getInverseRole() {
        return inverseRole;
    }
}
