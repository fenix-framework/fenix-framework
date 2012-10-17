package pt.ist.fenixframework.backend.ogm;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// import org.infinispan.Cache;
// import org.infinispan.manager.CacheContainer;
// import org.infinispan.manager.EmbeddedCacheManager;
// import org.infinispan.manager.DefaultCacheManager;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.Externalization;
import pt.ist.fenixframework.core.IdentityMap;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class OgmBackEnd implements BackEnd {
    private static final Logger logger = Logger.getLogger(OgmBackEnd.class);

    public static final String BACKEND_NAME = "ogm";
    // private static final String DOMAIN_CACHE_NAME = "DomainCache";

    private static final OgmBackEnd instance = new OgmBackEnd();

    protected final OgmTransactionManager transactionManager;
    // protected Cache<String, Object> domainCache;

    private OgmBackEnd() {
        this.transactionManager = new OgmTransactionManager();
    }

    public static OgmBackEnd getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public DomainRoot getDomainRoot() {
        return fromOid(OgmOID.ROOT_OBJECT_ID);
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(new OgmOID(externalId));
    }

    @Override
    public OgmTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        OgmOID internalId = (OgmOID)oid;
        if (logger.isEnabledFor(Level.INFO)) {
            logger.info("fromOid(" + internalId + ")");
        }
        return (T)transactionManager.getEntityManager().find(internalId.getObjClass(),
                                                             internalId.getPrimaryKey());
    }

    @Override
    public void shutdown() {
    }

    protected void configOgm(OgmConfig config) {
        transactionManager.setupTxManager(config);
    }

    public void save(OgmDomainObject obj) {
        transactionManager.getEntityManager().persist(obj);
    }


    // protected IdentityMap getIdentityMap() {
    //     return SharedIdentityMap.getCache();
    // }
}
