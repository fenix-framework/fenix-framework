package pt.ist.fenixframework.backend.ogm;

import eu.cloudtm.LocalityHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.ClusterInformation;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.messaging.MessagingQueue;

public class OgmBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(OgmBackEnd.class);

    public static final String BACKEND_NAME = "ogm";

    private static final OgmBackEnd instance = new OgmBackEnd();

    protected final OgmTransactionManager transactionManager;

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
        OgmOID rootId = OgmOID.ROOT_OBJECT_ID;
        DomainRoot domainRoot = fromOid(rootId);
        if (domainRoot == null) {
            domainRoot = new DomainRoot();
        }
        return domainRoot;
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
        OgmOID internalId = (OgmOID) oid;
        if (logger.isTraceEnabled()) {
            logger.trace("fromOid(" + internalId + ")");
        }
        return (T) transactionManager.getEntityManager().find(internalId.getObjClass(), internalId.getPrimaryKey());
    }

    @Override
    public void shutdown() {
        transactionManager.emf.close();
    }

    protected void configOgm(OgmConfig config) throws Exception {
        transactionManager.setupTxManager(config);
        config.waitForExpectedInitialNodes("backend-ogm-init-barrier");
    }

    public void save(AbstractDomainObject obj) {
        if (logger.isDebugEnabled()) {
            logger.debug("Saving " + obj.getClass());
        }
        transactionManager.getEntityManager().persist(obj);
    }

    @Override
    public <T extends DomainObject> T getOwnerDomainObject(String storageKey) {
        throw new UnsupportedOperationException("not yet implemented. Depends on support from the Hibernate OGM.");
    }

    @Override
    public String[] getStorageKeys(DomainObject domainObject) {
        throw new UnsupportedOperationException("not yet implemented. Depends on support from the Hibernate OGM.");
    }

    @Override
    public ClusterInformation getClusterInformation() {
        return ClusterInformation.NOT_AVAILABLE;
    }

    @Override
    public MessagingQueue createMessagingQueue(String appName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalityHints getLocalityHints(String externalId) {
        throw new UnsupportedOperationException();
    }

    // protected IdentityMap getIdentityMap() {
    //     return SharedIdentityMap.getCache();
    // }
}
