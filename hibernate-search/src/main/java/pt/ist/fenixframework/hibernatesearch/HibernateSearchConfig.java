package pt.ist.fenixframework.hibernatesearch;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dap.FFDAPConfig;
import pt.ist.fenixframework.txintrospector.TxStats;

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
            if (logger.isInfoEnabled()) {
                logger.info("Resource '" + CONFIG_FILE + "' not found. Hibernate Search disabled");
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Using config resource: " + hibernateSearchConfigURL);
        }

        Properties properties = new Properties();
        try {
            properties.load(hibernateSearchConfigURL.openStream());
        } catch (IOException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Hibernate Search unable to create properties. Hibernate Search disabled", e);
            }
        }

        // Ensure TxIntrospector is available
        if (! TxStats.ENABLED) {
            if (logger.isErrorEnabled()) {
                logger.error("TxIntrospector is disabled!" +
                        " -> Module Hibernate-search will not be available." +
                        " Please enable TxIntrospector and rebuild your application");
            }
            return;
        }

        HibernateSearchSupport.initializeSearchFactory(properties);

        // Register our listener
        FenixFramework.getTransactionManager().addCommitListener(new CommitIndexer());

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
