package pt.ist.fenixframework.pstm.dml;

import dml.runtime.Role;
import dml.runtime.Relation;

/**
 * This class is similar to the dml.runtime.RoleOne except that it
 * does not deal directly with vboxes, as the slot for the relation
 * may be part of a larger structure, itself contained in a box, as
 * per the one-box-per-object model.
 */

public abstract class RoleOne<C1,C2> implements Role<C1,C2> {

    public void add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if (o1 != null) {
            C2 old2 = getValue(o1);
            if (o2 != old2) {
                relation.remove(o1, old2);
                setValue(o1, o2);
            }
        }
    }

    public void remove(C1 o1, C2 o2) {
        if (o1 != null) {
            setValue(o1, null);
        }
    }

    public abstract C2 getValue(C1 o1);
    public abstract void setValue(C1 o1, C2 o2);
}
