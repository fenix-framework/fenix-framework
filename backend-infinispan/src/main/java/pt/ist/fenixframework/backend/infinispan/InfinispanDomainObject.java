package pt.ist.fenixframework.backend.infinispan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.OID;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.IdentityMap;
import eu.cloudtm.LocalityHints;

public class InfinispanDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private OID oid;

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected InfinispanDomainObject() {
        this((LocalityHints) null);
    }

    protected InfinispanDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (OID) oid.oid;
    }

    public InfinispanDomainObject(LocalityHints hints) {
        super(hints);
    }

    @Override
    protected void ensureOid(LocalityHints hints) {
        Class objClass = this.getClass();
        IdentityMap idMap = InfinispanBackEnd.getInstance().getIdentityMap();

        while (true) {
            // assign new OID
            this.oid = OID.makeNew(objClass, hints);
            // cache this instance
            Object shouldBeSame = idMap.cache(this);
            if (shouldBeSame == this) {
                return;
            } else {
                if (logger.isWarnEnabled()) {
                    logger.warn("Another object was already cached with the same key as this new object: " + oid);
                }
            }
        }
    }

    @Override
    public OID getOid() {
        return this.oid;
    }

    @Override
    public final String getExternalId() {
        return oid.toExternalId();
    }

    @Override
    public LocalityHints getLocalityHints() {
        return getOid().getLocalityHints();
    }

}
