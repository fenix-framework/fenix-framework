package pt.ist.fenixframework.backend.jvstm.pstm;

public interface JvstmInFenixTransaction {

    public void setReadOnly();

    public boolean txAllowsWrite();

//    public void logRelationAdd(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);
//
//    public void logRelationRemove(String relationName, AbstractDomainObject o1, AbstractDomainObject o2);

    // these should be inherited from a Transaction inferface in JVSTM (if only it existed)

    public <T> T getBoxValue(VBox<T> vbox);

    public boolean isBoxValueLoaded(VBox vbox);

}
