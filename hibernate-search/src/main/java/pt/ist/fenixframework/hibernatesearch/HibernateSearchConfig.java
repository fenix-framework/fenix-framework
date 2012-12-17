package pt.ist.fenixframework.hibernatesearch;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionListener;
import pt.ist.fenixframework.dap.FFDAPConfig;

public abstract class HibernateSearchConfig extends FFDAPConfig {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchConfig.class);

    /**
     * This well-known name specifies the location of the properties file used to configure
     * Hibernate Search.  This file should be available in the application's classpath.
     */
    public static final String CONFIG_FILE = "fenix-framework-hibernate-search.properties";

    @Override
    protected void init() {
        super.init();

        URL hibernateSearchConfigURL = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE);
        if (hibernateSearchConfigURL == null) {
            logger.info("Cannot access resource '" + CONFIG_FILE + "'. Hibernate Search disabled");
            return;
        }

        logger.trace("Using Hibernate Search config file: " + hibernateSearchConfigURL);
        Properties properties = new Properties();
        try {
            properties.load(hibernateSearchConfigURL.openStream());
        } catch (IOException e) {
            logger.error("Hibernate Search unable to create properties. Hibernate Search disabled", e);
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
