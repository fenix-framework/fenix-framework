package pt.ist.fenixframework.pstm;

import pt.ist.fenixframework.DomainObject;

import org.apache.ojb.broker.PersistenceBroker;

public interface FenixTransaction {
    public void setReadOnly();
    public DBChanges getDBChanges();
    public PersistenceBroker getOJBBroker();
    public DomainObject readDomainObject(String classname, int oid);
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr);
    public boolean isBoxValueLoaded(VBox vbox);
    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2);
    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2);
}
