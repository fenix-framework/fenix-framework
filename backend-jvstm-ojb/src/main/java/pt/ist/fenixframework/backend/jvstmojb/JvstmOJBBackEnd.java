package pt.ist.fenixframework.backend.jvstmojb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;
import pt.ist.fenixframework.pstm.DomainClassInfo;

public class JvstmOJBBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBBackEnd.class);

    private static final String BACKEND_NAME = "jvstm-ojb";

    private final TransactionManager transactionManager;

    public JvstmOJBBackEnd() {
	transactionManager = new JvstmOJBTransactionManager();
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
	if (logger.isTraceEnabled()) {
	    logger.trace("fromOid(" + oid + ")");
	}

	AbstractDomainObject obj = SharedIdentityMap.getCache().lookup(oid);

	if (obj == null) {
	    obj = DomainObjectAllocator.allocateObject(DomainClassInfo.mapOidToClass(((Long) oid).longValue()), oid);
	    obj = SharedIdentityMap.getCache().cache(obj);
	}

	return (T) obj;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
	return fromOid(Long.parseLong(externalId));
    }

    @Override
    public DomainRoot getDomainRoot() {
	DomainRoot root = fromOid(1L);
	if (root == null) {
	    root = new DomainRoot();
	    root = fromOid(1L);
	    assert root != null;
	}
	return root;
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
    }
}
