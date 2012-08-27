package pt.ist.fenixframework.backend;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;

/**
 * Each concrete back end should implement this interface.  The framework's core
 * already provides a {@link DefaultConfiguration} with a minimal back end that
 * implements a no-op {@link TransactionManager}.
 */
public interface BackEnd {
    /**
     * @see pt.ist.fenixframework.FenixFramework#getDomainRoot()
     */
    public DomainRoot getDomainRoot();

    /**
     * @see pt.ist.fenixframework.FenixFramework#getDomainObject(String)
     */
    public <T extends DomainObject> T getDomainObject(String externalId);

    /**
     * @see pt.ist.fenixframework.FenixFramework#getTransactionManager()
     */
    public TransactionManager getTransactionManager();

    /**
     * Backend-specific method to get a {@link DomainObject} given its OID.  Callers of this method
     * are responsible for providing a valid OID.
     *
     * @param oid The backend-specific identifier of the object to get
     */
    public <T extends DomainObject> T fromOid(Object oid);
}
