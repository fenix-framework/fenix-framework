package pt.ist.fenixframework.backend.fenixjvstm;

import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class FenixJvstmDomainObject extends AbstractDomainObjectAdapter {

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private long oid;

    // We need to have the default constructor, because we've added the
    // allocate-instance constructor
    protected FenixJvstmDomainObject() {
	super();
    }

    protected FenixJvstmDomainObject(DomainObjectAllocator.OID oid) {
	this.oid = (Long) oid.oid;
    }

    @Override
    protected void ensureOid() {
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
