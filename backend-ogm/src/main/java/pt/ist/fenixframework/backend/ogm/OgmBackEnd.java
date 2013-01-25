package pt.ist.fenixframework.backend.ogm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.ogm.jpa.impl.OgmEntityManager;

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
        OgmOID internalId = (OgmOID)oid;
        if (logger.isTraceEnabled()) {
            logger.trace("fromOid(" + internalId + ")");
        }
        return (T)transactionManager.getEntityManager().find(internalId.getObjClass(),
                                                             internalId.getPrimaryKey());
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
        logger.debug("Saving " + obj.getClass());
        transactionManager.getEntityManager().persist(obj);
    }


    // protected IdentityMap getIdentityMap() {
    //     return SharedIdentityMap.getCache();
    // }
}
