package pt.ist.fenixframework.core.dml.runtime;

import jvstm.VBox;

/**
 * This class handles one side of a many-to-something relation: the
 * side that keeps the collection corresponding to the multiplicity
 * many.
 */

public abstract class RoleMany<C1,C2> implements Role<C1,C2> {
    public void add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if ((o1 != null) && (o2 != null)) {
            getSet(o1).justAdd(o2);
        }
    }

    public void remove(C1 o1, C2 o2) {
        if ((o1 != null) && (o2 != null)) {
            getSet(o1).justRemove(o2);
        }
    }

    public abstract RelationBaseSet<C2> getSet(C1 o1);
}
