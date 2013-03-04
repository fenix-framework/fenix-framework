package pt.ist.fenixframework.backend.infinispan;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import pt.ist.fenixframework.DomainObject;
// import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.IdentityMap;

public class InfinispanDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private OID oid;

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected InfinispanDomainObject() {
        super();
    }

    protected InfinispanDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (OID) oid.oid;
    }

    @Override
    protected void ensureOid() {
        Class objClass = this.getClass();
        IdentityMap idMap = InfinispanBackEnd.getInstance().getIdentityMap();

        while (true) {
            // assign new OID
            String uuid = UUID.randomUUID().toString();
            this.oid = new OID(objClass, uuid);
            // cache this instance
            Object shouldBeSame = idMap.cache(this);
            if (shouldBeSame == this) {
                return;
            } else {
                logger.warn("Another object was already cached with the same key as this new object: " + oid);
            }
        }
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
