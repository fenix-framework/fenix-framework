package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

public class RelationAdapter<C1 extends DomainObject,C2 extends DomainObject> implements RelationListener<C1,C2> {
    public void beforeAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
        beforeAdd(o1, o2);
    }

    public void afterAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
        afterAdd(o1, o2);
    }

    public void beforeRemove(Relation<C1,C2> rel, C1 o1, C2 o2) {
        beforeRemove(o1, o2);
    }

    public void afterRemove(Relation<C1,C2> rel, C1 o1, C2 o2) {
        afterRemove(o1, o2);
    }

    // easier to use methods

    public void beforeAdd(C1 o1, C2 o2) {
        // do nothing
    }

    public void afterAdd(C1 o1, C2 o2) {
        // do nothing
    }

    public void beforeRemove(C1 o1, C2 o2) {
        // do nothing
    }

    public void afterRemove(C1 o1, C2 o2) {
        // do nothing
    }
}
