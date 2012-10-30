package pt.ist.fenixframework.backend.fenixjvstm;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.SharedIdentityMap;

public class FenixJvstmBackEnd implements BackEnd {
    private static final Logger logger = Logger.getLogger(FenixJvstmBackEnd.class);

    private static final String BACKEND_NAME = "fenixjvstm";

    private final TransactionManager transactionManager;

    public FenixJvstmBackEnd() {
	transactionManager = new FenixJvstmTransactionManager();
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
	if (logger.isEnabledFor(Level.TRACE)) {
	    logger.trace("fromOid(" + oid + ")");
	}
	return (T) SharedIdentityMap.getCache().lookup(oid);
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
