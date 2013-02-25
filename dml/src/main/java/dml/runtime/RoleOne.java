package dml.runtime;

import jvstm.VBox;

/**
 * This class handles one side of a one-to-something relation: the
 * side that keeps the single reference corresponding to the
 * multiplicity one.
 */

public abstract class RoleOne<C1,C2> implements Role<C1,C2> {
    @Override
    public void add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if (o1 != null) {
            VBox<C2> o1Box = getBox(o1);
            C2 old2 = o1Box.get();
            if (o2 != old2) {
                relation.remove(o1, old2);
                o1Box.put(o2);
            }
        }
    }

    @Override
    public void remove(C1 o1, C2 o2) {
        if (o1 != null) {
            getBox(o1).put(null);
        }
    }

    public abstract VBox<C2> getBox(C1 o1);
}
