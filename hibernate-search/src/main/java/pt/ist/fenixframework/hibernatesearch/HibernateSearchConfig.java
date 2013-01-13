package pt.ist.fenixframework.hibernatesearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionListener;
import pt.ist.fenixframework.dap.FFDAPConfig;
import pt.ist.fenixframework.txintrospector.TxStats;
import pt.ist.fenixframework.util.FileLookup;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public abstract class HibernateSearchConfig extends FFDAPConfig {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchConfig.class);

    /**
     * This well-known name specifies the location of the properties file used to configure
     * Hibernate Search.  This file should be available in the application's classpath.
     */
    private static final String DEFAULT_CONFIG_FILE = "fenix-framework-hibernate-search.properties";
    private static final String CONFIG_FILE_PROPERTY = "fenix-framework-hibernate-search-config-file";

    @Override
    protected void init() {
        super.init();

        URL hibernateSearchConfigURL = FileLookup.find(CONFIG_FILE_PROPERTY, DEFAULT_CONFIG_FILE);
        if (hibernateSearchConfigURL == null) {
            logger.info("Resource '" + DEFAULT_CONFIG_FILE + "' not found. Hibernate Search disabled");
            return;
        }
        logger.info("Using config resource: " + hibernateSearchConfigURL);

        Properties properties = new Properties();
        try {
            properties.load(hibernateSearchConfigURL.openStream());
        } catch (IOException e) {
            logger.error("Hibernate Search unable to create properties. Hibernate Search disabled", e);
        }

        // Ensure TxIntrospector is available
        if (! TxStats.ENABLED) {
            logger.error("TxIntrospector is disabled!" +
                    " -> Module Hibernate-search will not be available." +
                    " Please enable TxIntrospector and rebuild your application");
            return;
        }

        HibernateSearchSupport.initializeSearchFactory(properties);

        // Register our listener
        FenixFramework.getTransactionManager().registerForBegin(new TransactionListener() {
            @Override public void notifyBeforeBegin() { }

            @Override public void notifyAfterBegin(Transaction tx) {
                try {
                    tx.registerSynchronization(new CommitIndexer());
                } catch (IllegalStateException e) {
                    logger.error("Exception caught in HibernateSearchConfig.init()", e);
                } catch (RollbackException e) {
                    logger.error("Exception caught in HibernateSearchConfig.init()", e);
                } catch (SystemException e) {
                    logger.error("Exception caught in HibernateSearchConfig.init()", e);
                }
            }
        });
    }

    /**
     * Subclasses of this class can overwrite this method, but they should specifically call
     * <code>super.shutdown()</code> to orderly shutdown the framework and hibernate search.
     */
    @Override
    public void shutdown() {
        if (HibernateSearchSupport.getSearchFactory() != null) {
            HibernateSearchSupport.getSearchFactory().close();
        }

        super.shutdown();
    }
}
