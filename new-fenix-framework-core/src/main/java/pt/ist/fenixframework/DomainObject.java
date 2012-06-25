package pt.ist.fenixframework;

public interface DomainObject {
    public Object getExternalId();
    public DomainObject fromExternalId(Object oid);
}
