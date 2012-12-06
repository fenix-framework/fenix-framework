package pt.ist.fenixframework.pstm;

import org.apache.ojb.broker.PersistenceBroker;

public interface FenixTransaction {
    public void setReadOnly();
    public DBChanges getDBChanges();
    public PersistenceBroker getOJBBroker();
    public AbstractDomainObject readDomainObject(String classname, int oid);
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr);
    public boolean isBoxValueLoaded(VBox vbox);
    public void logRelationAdd(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);
    public void logRelationRemove(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);
}
