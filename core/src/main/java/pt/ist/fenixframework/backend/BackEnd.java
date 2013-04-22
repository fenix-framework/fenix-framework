package pt.ist.fenixframework.backend;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;

/**
 * Each concrete back end should implement this interface. The framework's core already provides a
 * {@link pt.ist.fenixframework.backend.mem.DefaultConfig} with a minimal back end that implements a
 * no-op {@link TransactionManager}.
 */
public interface BackEnd {
    /**
     * Get the unique name of this BackEnd. The String returned by this method should contain only
     * valid characters in a filename (because it can be used for configuration by convention
     * 
     * @see pt.ist.fenixframework.FenixFramework
     */
    public String getName();

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
     * Backend-specific method to get a {@link DomainObject} given its OID. Callers of this method
     * are responsible for providing a valid OID.
     * 
     * @param oid The backend-specific identifier of the object to get
     */
    public <T extends DomainObject> T fromOid(Object oid);

    /**
     * Perform any required operations for a successful shutdown of the BackEnd. After invoking
     * this method there is no guarantee that the Fenix Framework is able to provide any more
     * services.
     */
    public void shutdown();

    /**
     * @see pt.ist.fenixframework.FenixFramework#getOwnerDomainObject(String)
     */
    public <T extends DomainObject> T getOwnerDomainObject(String storageKey);

    /**
     * @see pt.ist.fenixframework.FenixFramework#getStorageKeys(DomainObject)
     */
    public String[] getStorageKeys(DomainObject domainObject);

}
