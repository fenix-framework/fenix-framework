package pt.ist.fenixframework;

public interface DomainObject {
    public Integer getIdInternal();
    public String getExternalId();
    public long getOID();
    public long getOid();
    public boolean isDeleted();
}
