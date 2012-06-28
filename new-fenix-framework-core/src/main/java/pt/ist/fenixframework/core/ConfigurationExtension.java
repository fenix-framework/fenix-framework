package pt.ist.fenixframework.core;

import java.io.Serializable;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.TransactionManager;

/**
 * The concrete configuration extension <code>initialize(Config)</code> method will be invoked when
 * the <code>FenixFramework.initialize(Config)</code> method is invoked.
 */
public interface ConfigurationExtension {
    public void initialize(Config config);

    public <T extends DomainObject> T getDomainObject(Serializable externalId);
    public Repository getRepository();
    public TransactionManager getTransactionManager();
}
