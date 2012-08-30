package pt.ist.fenixframework.backend.infinispan;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.IdentityMap;

public class InfinispanDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = Logger.getLogger(InfinispanDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private String oid;

    @Override
    protected void restoreOid(Object oid) {
        assert (oid != null);
        this.oid = (String)oid;
    }

    @Override
    protected void ensureOid() {
        this.oid = UUID.randomUUID().toString();
    }

    // dealing with domain object identifiers

    @Override
    public String getOid() {
	return oid;
    }

    @Override
    public final String getExternalId() {
	return oid;
    }

    public static <T extends DomainObject> T fromOid(String oid) {
        // FenixCache cache = FenixFramework.getBackEnd().getCache();

        // return (T) FenixCache.getCache().lookup(oid);
        throw new UnsupportedOperationException("not yet implemented");
    }
}

