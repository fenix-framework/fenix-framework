package pt.ist.fenixframework.pstm;

import dml.runtime.DirectRelation;
import dml.runtime.Role;

import pt.ist.fenixframework.DomainObject;

public class LoggingRelation<C1 extends DomainObject,C2 extends DomainObject> extends DirectRelation<C1,C2> {
    private String relationName;

    public LoggingRelation(Role<C1,C2> firstRole) {
        super(firstRole);
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public void add(C1 o1, C2 o2) {
        super.add(o1, o2);
        if ((o1 != null) && (o2 != null)) {
            Transaction.currentFenixTransaction().logRelationAdd(relationName, o1, o2);
        }
    }

    public void remove(C1 o1, C2 o2) {
        super.remove(o1, o2);
        if ((o1 != null) && (o2 != null)) {
            Transaction.currentFenixTransaction().logRelationRemove(relationName, o1, o2);
        }
    }
}
