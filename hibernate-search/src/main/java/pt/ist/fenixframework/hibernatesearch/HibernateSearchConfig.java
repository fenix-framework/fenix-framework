package pt.ist.fenixframework.hibernatesearch;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dap.FFDAPConfig;

public abstract class HibernateSearchConfig extends FFDAPConfig {

    private static final Logger logger = LoggerFactory.getLogger(HibernateSearchConfig.class);

    /**
     * This <strong>optional</strong> parameter specifies the location of the properties file used
     * to configure Hibernate Search.  This file should be available in the application's classpath.
     */
    protected String hibernateSearchConfigFile = null;

    @Override
    protected void init() {
        super.init();

        if (hibernateSearchConfigFile == null) {
            logger.info("No property 'hibernateSearchConfigFile' given. Hibernate Search disabled");
            return;
        }
        logger.info("Initializing Hibernate Search module");

        URL hibernateSearchConfigURL = Thread.currentThread().getContextClassLoader().getResource(hibernateSearchConfigFile);
        if (hibernateSearchConfigURL == null) {
            logger.error("Cannot access resource '" + hibernateSearchConfigFile + "'. Hibernate Search disabled");
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
