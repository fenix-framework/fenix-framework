package pt.ist.fenixframework.core.dml.runtime;

import java.util.LinkedList;

public class DirectRelation<C1,C2> implements Relation<C1,C2> {
    private Relation<C2,C1> inverse = new InverseRelation<C2,C1>(this);
    
    private Role<C1,C2> firstRole;

    private LinkedList<RelationListener<C1,C2>> listeners = null;

    public DirectRelation(Role<C1,C2> firstRole) {
        this.firstRole = firstRole;
    }

    public void add(C1 o1, C2 o2) {
        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.beforeAdd(this, o1, o2);
            }
        }

        firstRole.add(o1, o2, this);
        firstRole.getInverseRole().add(o2, o1, inverse);

        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.afterAdd(this, o1, o2);
            }
        }
    }

    public void remove(C1 o1, C2 o2) {
        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.beforeRemove(this, o1, o2);
            }
        }

        firstRole.remove(o1, o2);
        firstRole.getInverseRole().remove(o2, o1);

        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.afterRemove(this, o1, o2);
            }
        }
    }

    public Relation<C2,C1> getInverseRelation() {
        return inverse;
    }


    protected LinkedList<RelationListener<C1,C2>> getListeners() {
        if (listeners == null) {
            listeners = new LinkedList<RelationListener<C1,C2>>();
        }
        return listeners;
    }

    public void addListener(RelationListener<C1,C2> listener) {
        getListeners().add(listener);
    }

    public void removeListener(RelationListener<C1,C2> listener) {
        getListeners().remove(listener);
    }
}
