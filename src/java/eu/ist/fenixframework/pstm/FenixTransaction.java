package eu.ist.fenixframework.pstm;

import eu.ist.fenixframework.DomainObject;

import org.apache.ojb.broker.PersistenceBroker;

public interface FenixTransaction {
    public void setReadOnly();
    public DBChanges getDBChanges();
    public PersistenceBroker getOJBBroker();
    public DomainObject getDomainObject(String classname, int oid);
    public DomainObject readDomainObject(String classname, int oid);
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr);
    public boolean isBoxValueLoaded(VBox vbox);
}
