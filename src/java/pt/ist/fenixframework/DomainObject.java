package pt.ist.fenixframework;

public interface DomainObject {
    public Integer getIdInternal();
    public void setIdInternal(Integer idInternal);
    public long getOID();
    public long getOid();
    public boolean isDeleted();
}
