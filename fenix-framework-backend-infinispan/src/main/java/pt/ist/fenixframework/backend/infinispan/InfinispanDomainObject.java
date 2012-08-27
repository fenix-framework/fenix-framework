package pt.ist.fenixframework.backend.infinispan;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.FenixCache;

public class InfinispanDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = Logger.getLogger(InfinispanDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private String oid;

    @Override
    protected void restoreOid(Object oid) {
        // assert (oid != null);
        // this.oid = (Long)oid;
    }

    @Override
    protected void ensureOid() {
        // // find successive ids until one is available
        // while (true) {
        //     this.oid = DomainClassInfo.getNextOidFor(this.getClass());
        //     Object cached = FenixCache.getCache().cache(this);
        //     if (cached == this) {
        //         // break the loop once we got this instance cached
        //         return;
        //     }
        // }
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

    public static <T extends DomainObject> T fromOid(long oid) {
        // return (T) FenixCache.getCache().lookup(oid);
        throw new UnsupportedOperationException("not yet implemented");
    }
}

