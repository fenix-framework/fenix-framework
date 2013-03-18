package pt.ist.fenixframework.backend.jvstmmem;

import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;
import eu.cloudtm.LocalityHints;

public class JVSTMMemDomainObject extends AbstractDomainObjectAdapter {

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private long oid;

    // We need to have the default constructor, because we've added the
    // allocate-instance constructor
    protected JVSTMMemDomainObject() {
        this((LocalityHints) null);
    }

    protected JVSTMMemDomainObject(DomainObjectAllocator.OID oid) {
        this.oid = (Long) oid.oid;
    }

    public JVSTMMemDomainObject(LocalityHints hints) {
        super(hints);
    }

    @Override
    protected void ensureOid(LocalityHints hints) {
        // find successive ids until one is available
        while (true) {
            this.oid = DomainClassInfo.getNextOidFor(this.getClass());
            Object cached = SharedIdentityMap.getCache().cache(this);
            if (cached == this) {
                // break the loop once we got this instance cached
                return;
            }
        }
    }

    // dealing with domain object identifiers

    @Override
    public Long getOid() {
        return oid;
    }

    @Override
    public final String getExternalId() {
        return String.valueOf(getOid());
    }
}
