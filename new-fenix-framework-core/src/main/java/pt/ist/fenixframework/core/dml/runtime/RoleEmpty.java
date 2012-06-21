package pt.ist.fenixframework.core.dml.runtime;

/**
 * This class handles one side of a relation which is not navigable in
 * the other direction.
 */

public class RoleEmpty<C1,C2> implements Role<C1,C2> {
    private Role<C2,C1> inverseRole;

    public RoleEmpty(Role<C2,C1> inverseRole) {
        this.inverseRole = inverseRole;
    }

    public void add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        // do nothing
    }
    
    public void remove(C1 o1, C2 o2) {
        // do nothing
    }

    public Role<C2,C1> getInverseRole() {
        return inverseRole;
    }
}
