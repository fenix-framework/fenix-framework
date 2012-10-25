package pt.ist.fenixframework.backend.mem;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class MemDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MemDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private long oid;

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected MemDomainObject() {
        super();
    }

    protected MemDomainObject(DomainObjectAllocator.OID oid) {
        this.oid = (Long)oid.oid;
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

