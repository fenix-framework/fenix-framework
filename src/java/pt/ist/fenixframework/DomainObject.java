package pt.ist.fenixframework;

public interface DomainObject {
    public Integer getIdInternal();
    public long getOID();
    public long getOid();
    public boolean isDeleted();
}
