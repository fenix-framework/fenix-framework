package pt.ist.fenixframework.core.dml.runtime;

public interface Relation<C1,C2> {
    public void add(C1 o1, C2 o2);
    public void remove(C1 o1, C2 o2);
    public Relation<C2,C1> getInverseRelation();
    // Remove the following from the Relation interface because the 
    // static initialization order of classes prevent us from adding
    // listeners to non-direct relations
    //public void addListener(RelationListener<C1,C2> listener);
    //public void removeListener(RelationListener<C1,C2> listener);
}
