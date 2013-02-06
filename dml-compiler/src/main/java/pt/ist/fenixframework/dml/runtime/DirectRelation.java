package pt.ist.fenixframework.dml.runtime;

import java.util.LinkedList;

import pt.ist.fenixframework.DomainObject;

public class DirectRelation<C1 extends DomainObject,C2 extends DomainObject> implements Relation<C1,C2> {
    private Relation<C2,C1> inverse;
    private Role<C1,C2> firstRole;
    private final String name;

    private LinkedList<RelationListener<C1,C2>> listeners = null;

    public DirectRelation(Role<C1,C2> firstRole, String name) {
        this.firstRole = firstRole;
        this.name = name;
        inverse = new InverseRelation<C2,C1>(this, name);
    }

    public DirectRelation(Role<C1,C2> firstRole, String name, RelationListener<C1,C2> ... listeners) {
        this(firstRole, name);
        for (RelationListener<C1,C2> listener : listeners) {
            addListener(listener);
        }
    }

    public boolean add(C1 o1, C2 o2) {
        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.beforeAdd(this, o1, o2);
            }
        }

        boolean added = firstRole.add(o1, o2, this);
        if (added) {
            firstRole.getInverseRole().add(o2, o1, inverse);

            if (listeners != null) {
                for (RelationListener<C1,C2> l : listeners) {
                    l.afterAdd(this, o1, o2);
                }
            }
        }
        return added;
    }

    public boolean remove(C1 o1, C2 o2) {
        if (listeners != null) {
            for (RelationListener<C1,C2> l : listeners) {
                l.beforeRemove(this, o1, o2);
            }
        }

        boolean removed = firstRole.remove(o1, o2);
        if (removed) {
            firstRole.getInverseRole().remove(o2, o1);

            if (listeners != null) {
                for (RelationListener<C1,C2> l : listeners) {
                    l.afterRemove(this, o1, o2);
                }
            }
        }
        return removed;
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

    public String getName() {
        return name;
    }
}
