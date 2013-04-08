package pt.ist.fenixframework.backend.jvstm.pstm;

public interface JvstmInFenixTransaction {

    public void setReadOnly();

    public boolean txAllowsWrite();

    public <T> T getBoxValue(VBox<T> vbox);

    public boolean isBoxValueLoaded(VBox vbox);

//    public void logRelationAdd(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);
//
//    public void logRelationRemove(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);
}
