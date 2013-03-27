package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jvstm.PerTxBox;
import jvstm.ReadTransaction;
import jvstm.Transaction;
import jvstm.cps.ConsistencyCheckTransaction;
import jvstm.cps.Depended;

import org.apache.ojb.broker.PersistenceBroker;

import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;

public class FenixConsistencyCheckTransaction extends ReadTransaction implements ConsistencyCheckTransaction, FenixTransaction {

    protected HashSet<VBox> boxesRead = new HashSet<VBox>();

    private final FenixTransaction parent;
    private final Object checkedObj;

    public FenixConsistencyCheckTransaction(FenixTransaction parent, Object checkedObj) {
        super((Transaction) parent);
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

    @Override
    public Set<Depended> getDepended() {
        if (!JvstmOJBConfig.canCreateDomainMetaObjects()) {
            return EMPTY_SET;
        }
        Set<Depended> dependedSet = new HashSet<Depended>(boxesRead.size());

        for (VBox box : boxesRead) {
            Depended depended = getDependedForBox(box);
            dependedSet.add(depended);
        }

        return dependedSet;
    }

    protected Depended getDependedForBox(VBox box) {
        AbstractDomainObject domainObject = (AbstractDomainObject) box.getOwnerObject();
        DomainMetaObject metaObject = domainObject.getDomainMetaObject();
        return metaObject;
    }

    @Override
    public void setReadOnly() {
        throw new Error("It doesn't make sense to call setReadOnly for a FenixConsistencyCheckTransaction");
    }

    @Override
    public DBChanges getDBChanges() {
        throw new Error("A FenixConsistencyCheckTransaction is read-only");
    }

    @Override
    public PersistenceBroker getOJBBroker() {
        return parent.getOJBBroker();
    }

    @Override
    public AbstractDomainObject readDomainObject(String classname, int oid) {
        return parent.readDomainObject(classname, oid);
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr) {
        if (obj != checkedObj) {
            if (!JvstmOJBConfig.canCreateDomainMetaObjects()) {
                throw new Error("Consistency predicates are not allowed to access other objects, "
                        + "unless the FenixFramework is configured to create DomainMetaObjects. "
                        + "See: JvstmOJBConfig.canCreateDomainMetaObjects");
            }
            if (obj.getClass().isAnnotationPresent(NoDomainMetaObjects.class)) {
                throw new Error("Consistency predicates are not allowed to access objects "
                        + "of a class annotated with @NoDomainMetaObjects");
            }
        }

        boxesRead.add(vbox);

        // ask the parent transaction (a RW tx) for the value of the box
        return parent.getBoxValue(vbox, obj, attr);
    }

    @Override
    public <T> void setBoxValueInParent(VBox<T> vbox, T value) {
        parent.setBoxValueInParent(vbox, value);
    }

    @Override
    public <T> T getPerTxValue(PerTxBox<T> box, T initial) {
        return parent.getPerTxValue(box, initial);
    }

    @Override
    public <T> void setPerTxValue(PerTxBox<T> box, T value) {
        parent.setPerTxValue(box, value);
    }

    @Override
    public boolean isBoxValueLoaded(VBox vbox) {
        throw new Error("It doesn't make sense to call isBoxValueLoaded for a FenixConsistencyCheckTransaction");
    }

    @Override
    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2) {
        throw new Error("It doesn't make sense to call logRelationAdd for a FenixConsistencyCheckTransaction");
    }

    @Override
    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2) {
        throw new Error("It doesn't make sense to call logRelationRemove for a FenixConsistencyCheckTransaction");
    }
}
