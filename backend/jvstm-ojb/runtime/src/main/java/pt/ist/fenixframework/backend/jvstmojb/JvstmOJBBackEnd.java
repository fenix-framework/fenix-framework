package pt.ist.fenixframework.backend.jvstmojb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstmojb.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstmojb.pstm.OneBoxDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.repository.DbUtil;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class JvstmOJBBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBBackEnd.class);

    public static final String BACKEND_NAME = "jvstm-ojb";

    private final TransactionManager transactionManager;

    private final DomainObjectAllocator allocator = new DomainObjectAllocator(OneBoxDomainObject.class);

    private boolean newInstance = false;

    public JvstmOJBBackEnd() {
        transactionManager = new JvstmOJBTransactionManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> T fromOid(Object oid) {
        logger.trace("fromOid({})", oid);

        AbstractDomainObject obj = SharedIdentityMap.getCache().lookup(oid);

        if (obj == null) {
            obj = allocator.allocateObject(DomainClassInfo.mapOidToClass(((Long) oid).longValue()), oid);
            obj = SharedIdentityMap.getCache().cache(obj);
        }

        return (T) obj;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        if (externalId == null) {
            return null;
        }
        return fromOid(Long.parseLong(externalId));
    }

    @Override
    public DomainRoot getDomainRoot() {
        return fromOid(1L);
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public void shutdown() {
        DbUtil.deregisterDriver();
    }

    @Override
    public boolean isNewInstance() {
        return newInstance;
    }

    void setNewInstance(boolean newInstance) {
        this.newInstance = newInstance;
    }

    @Override
    public boolean isDomainObjectValid(DomainObject object) {
        if (object instanceof OneBoxDomainObject) {
            return ((OneBoxDomainObject) object).is$$do$$Valid();
        } else {
            return false;
        }
    }

}
