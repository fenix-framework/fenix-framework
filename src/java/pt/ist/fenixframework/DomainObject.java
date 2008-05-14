package pt.ist.fenixframework;

public interface DomainObject {
    public Integer getIdInternal();
    public void setIdInternal(Integer idInternal);
    public long getOID();
    public boolean isDeleted();
}
