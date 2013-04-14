package pt.ist.fenixframework.backend.jvstmojb.pstm;

import jvstm.PerTxBox;

import org.apache.ojb.broker.PersistenceBroker;

import pt.ist.fenixframework.DomainObject;

public interface FenixTransaction {
    public void setReadOnly();

    public DBChanges getDBChanges();

    public PersistenceBroker getOJBBroker();

    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr);

    public <T> void setBoxValueInParent(VBox<T> box, T value);

    public <T> T getPerTxValue(PerTxBox<T> box, T initial);

    public <T> void setPerTxValue(PerTxBox<T> box, T value);

    public boolean isBoxValueLoaded(VBox vbox);

    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2);

    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2);
}
