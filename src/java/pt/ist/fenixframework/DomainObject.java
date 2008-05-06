package pt.ist.fenixframework;

public interface DomainObject {
    public Integer getIdInternal();
    public void setIdInternal(Integer idInternal);
    public jvstm.VBoxBody addNewVersion(String attrName, int txNumber);
    public void readFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException;
    public boolean isDeleted();
}
