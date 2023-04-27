package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

public class InverseRelation<C1 extends DomainObject, C2 extends DomainObject> implements Relation<C1, C2> {
    private final Relation<C2, C1> inverseRelation;
    private final String name;

    public InverseRelation(Relation<C2, C1> inverseRelation, String name) {
        this.inverseRelation = inverseRelation;
        this.name = name;
    }

    @Override
    public boolean add(C1 o1, C2 o2) {
        return inverseRelation.add(o2, o1);
    }

    @Override
    public boolean remove(C1 o1, C2 o2) {
        return inverseRelation.remove(o2, o1);
    }

    @Override
    public Relation<C2, C1> getInverseRelation() {
        return inverseRelation;
    }

    @Override
    public String getName() {
        return name;
    }

}
