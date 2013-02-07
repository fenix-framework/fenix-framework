package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

public class InverseRelation<C1 extends DomainObject,C2 extends DomainObject> implements Relation<C1,C2> {
    private Relation<C2,C1> inverseRelation;
    private final String name;
    //private LinkedList<InverseListener<C2,C1>> inverseListeners = null;

    public InverseRelation(Relation<C2,C1> inverseRelation, String name) {
        this.inverseRelation = inverseRelation;
        this.name = name;
    }

    public boolean add(C1 o1, C2 o2) {
        return inverseRelation.add(o2, o1);
    }

    public boolean remove(C1 o1, C2 o2) {
        return inverseRelation.remove(o2, o1);
    }

    public Relation<C2,C1> getInverseRelation() {
        return inverseRelation;
    }

    public String getName() {
        return name;
    }

    /*
     * The following methods are commented out because, currently, we
     * can only add listeners to DirectRelations (a consequence of the
     * initialization order of classes)

    protected LinkedList<InverseListener<C2,C1>> getListeners() {
        if (inverseListeners == null) {
            inverseListeners = new LinkedList<InverseListener<C2,C1>>();
        }
        return inverseListeners;
    }

    public void addListener(RelationListener<C1,C2> listener) {
        InverseListener<C2,C1> invListener = new InverseListener<C2,C1>(listener);
        inverseRelation.addListener(invListener);
        getListeners().add(invListener);
    }

    public void removeListener(RelationListener<C1,C2> listener) {
        ListIterator<InverseListener<C2,C1>> iter = getListeners().listIterator();
        while (iter.hasNext()) {
            if (iter.next().inverseListener.equals(listener)) {
                iter.remove();
            }
        }
    }

    static class InverseListener<C1,C2> implements RelationListener<C1,C2> {
        RelationListener<C2,C1> inverseListener;

        InverseListener(RelationListener<C2,C1> inverseListener) {
            this.inverseListener = inverseListener;
        }

        public void beforeAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
            inverseListener.beforeAdd(rel.getInverseRelation(), o2, o1);
        }
        
        public void afterAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
            inverseListener.afterAdd(rel.getInverseRelation(), o2, o1);
        }

        public void beforeRemove(Relation<C1,C2> rel, C1 o1, C2 o2) {
            inverseListener.beforeRemove(rel.getInverseRelation(), o2, o1);
        }
        
        public void afterRemove(Relation<C1,C2> rel, C1 o1, C2 o2) {
            inverseListener.afterRemove(rel.getInverseRelation(), o2, o1);
        }
    }
    */
}
