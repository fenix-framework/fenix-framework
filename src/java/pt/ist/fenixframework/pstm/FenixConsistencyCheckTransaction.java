package pt.ist.fenixframework.pstm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jvstm.ReadTransaction;
import jvstm.Transaction;
import jvstm.cps.ConsistencyCheckTransaction;
import jvstm.cps.Depended;
import pt.ist.fenixframework.DomainObject;

import org.apache.ojb.broker.PersistenceBroker;

public class FenixConsistencyCheckTransaction extends ReadTransaction 
    implements ConsistencyCheckTransaction,FenixTransaction {

    private FenixTransaction parent;
    private Object checkedObj;

    public FenixConsistencyCheckTransaction(FenixTransaction parent, Object checkedObj) {
        super((Transaction)parent);
        this.parent = parent;
        this.checkedObj = checkedObj;
    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        throw new Error("Nested transactions not supported yet...");
    }

    @Override
    public <T> T getBoxValue(jvstm.VBox<T> vbox) {
        throw new Error("In a FenixConsistencyCheckTransaction we must call the three-arg getBoxValue method");
    }

    private static final Set<Depended> EMPTY_SET = Collections.unmodifiableSet(new HashSet<Depended>());

    public Set<Depended> getDepended() {
        return EMPTY_SET;
    }

    public void setReadOnly() {
        throw new Error("It doesn't make sense to call setReadOnly for a FenixConsistencyCheckTransaction");
    }

    public DBChanges getDBChanges() {
        throw new Error("A FenixConsistencyCheckTransaction is read-only");
    }

    public PersistenceBroker getOJBBroker() {
        return parent.getOJBBroker();
    }

    public DomainObject readDomainObject(String classname, int oid) {
        return parent.readDomainObject(classname, oid);
    }

    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr) {
        if (obj != checkedObj) {
            throw new Error("We currently do not support consistency-predicates that access other objects");
        }

        // ask the parent transaction (a RW tx) for the value of the box
        return parent.getBoxValue(vbox, obj, attr);
    }

    public boolean isBoxValueLoaded(VBox vbox) {
        throw new Error("It doesn't make sense to call isBoxValueLoaded for a FenixConsistencyCheckTransaction");
    }

    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2) {
        throw new Error("It doesn't make sense to call logRelationAdd for a FenixConsistencyCheckTransaction");
    }

    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2) {
        throw new Error("It doesn't make sense to call logRelationRemove for a FenixConsistencyCheckTransaction");
    }
}
