package pt.ist.fenixframework.backend.jvstmojb.pstm;

import pt.ist.fenixframework.dml.runtime.DirectRelation;
import pt.ist.fenixframework.dml.runtime.Role;

public class LoggingRelation<C1 extends AbstractDomainObject, C2 extends AbstractDomainObject> extends DirectRelation<C1, C2> {
    private String relationName;

    public LoggingRelation(Role<C1, C2> firstRole, String name) {
        super(firstRole, name);
        this.relationName = name;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    @Override
    public boolean add(C1 o1, C2 o2) {
        boolean added = super.add(o1, o2);
        if ((o1 != null) && (o2 != null)) {
            TransactionSupport.currentFenixTransaction().logRelationAdd(relationName, o1, o2);
        }
        return added;
    }

    @Override
    public boolean remove(C1 o1, C2 o2) {
        boolean removed = super.remove(o1, o2);
        if ((o1 != null) && (o2 != null)) {
            TransactionSupport.currentFenixTransaction().logRelationRemove(relationName, o1, o2);
        }
        return removed;
    }
}
