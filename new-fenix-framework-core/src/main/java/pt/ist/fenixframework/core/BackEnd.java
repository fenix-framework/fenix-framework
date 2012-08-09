package pt.ist.fenixframework.core;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;

/**
 * Each concrete back end should implement this interface.  The framework's core
 * already provides a {@link DefaultConfiguration} with a minimal back end that
 * implements a no-op {@link TransactionManager}.
 */
public interface BackEnd {
    public DomainRoot getDomainRoot();
    public <T extends DomainObject> T getDomainObject(String externalId);
    public TransactionManager getTransactionManager();
}
