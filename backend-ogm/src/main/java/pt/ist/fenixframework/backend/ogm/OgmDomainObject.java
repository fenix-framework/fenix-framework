package pt.ist.fenixframework.backend.ogm;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import eu.cloudtm.LocalityHints;
// import pt.ist.fenixframework.DomainObject;
// import pt.ist.fenixframework.FenixFramework;

// @Tuplizer(impl = DynamicEntityTuplizer.class)
public abstract class OgmDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(OgmDomainObject.class);

    private String hibernate$primaryKey;  // assigned by the fenix framework.
    private transient OgmOID oid;       // used by FF.  Includes hibernate$primaryKey info.

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected OgmDomainObject() {
        this((LocalityHints) null);
    }

    protected OgmDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (OgmOID) oid.oid;
        this.hibernate$primaryKey = this.oid.getPrimaryKey();
    }

    public OgmDomainObject(LocalityHints hints) {
        super(hints);
    }

    @Override
    protected void ensureOid(LocalityHints hints) {
        if (this.getClass().equals(DomainRoot.class)) {
            hibernate$primaryKey = OgmOID.ROOT_PK;
        } else {
            hibernate$primaryKey = UUID.randomUUID().toString();
        }

        this.oid = new OgmOID(this.getClass(), this.hibernate$primaryKey);
        OgmBackEnd.getInstance().save(this);
        if (logger.isDebugEnabled()) {
            logger.debug("Saved " + this);
        }
    }

    // dealing with domain object identifiers

    String getHibernate$primaryKey() {
        return this.hibernate$primaryKey;
    }

    void setHibernate$primaryKey(String key) {
        this.hibernate$primaryKey = key;
    }

    @Override
    public OgmOID getOid() {
        return this.oid;
    }

    @Override
    public final String getExternalId() {
        return oid.toExternalId();
    }

}
