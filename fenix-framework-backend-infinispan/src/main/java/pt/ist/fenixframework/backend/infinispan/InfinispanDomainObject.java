package pt.ist.fenixframework.backend.infinispan;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// import pt.ist.fenixframework.DomainObject;
// import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
// import pt.ist.fenixframework.core.IdentityMap;

public class InfinispanDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = Logger.getLogger(InfinispanDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private OID oid;

    protected InfinispanDomainObject() {
        super();
    }

    protected InfinispanDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (OID)oid.oid;
    }

    @Override
    protected void restoreOid(Comparable oid) {
        throw new UnsupportedOperationException("disabled");
        // assert (oid != null);
        // this.oid = (OID)oid;
    }

    @Override
    protected void ensureOid() {
        Class objClass = this.getClass();
        String uuid = UUID.randomUUID().toString();
        this.oid = new OID(objClass, uuid);
    }

    // dealing with domain object identifiers

    @Override
    public OID getOid() {
	return this.oid;
    }

    @Override
    public final String getExternalId() {
	return oid.toExternalId();
    }

}

