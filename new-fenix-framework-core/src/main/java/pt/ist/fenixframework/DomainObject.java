package pt.ist.fenixframework;

import java.io.Serializable;

public interface DomainObject {
    public Serializable getExternalId();
    // public DomainObject fromExternalId(Serializable oid);
}
