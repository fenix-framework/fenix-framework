package dml.runtime;

public class RelationAdapter<C1,C2> implements RelationListener<C1,C2> {
    @Override
    public void beforeAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
        beforeAdd(o1, o2);
    }

    @Override
    public void afterAdd(Relation<C1,C2> rel, C1 o1, C2 o2) {
        afterAdd(o1, o2);
    }

    @Override
    public void beforeRemove(Relation<C1,C2> rel, C1 o1, C2 o2) {
        beforeRemove(o1, o2);
    }

    @Override
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
