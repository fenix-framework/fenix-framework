package pt.ist.fenixframework.backend.infinispan;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchConfig;

/**
 * This is the infinispan configuration manager used by the fenix-framework-backend-infinispan
 * project.
 * 
 * @see Config
 *
 */
public class InfinispanConfig extends HibernateSearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanDomainObject.class);

    private static final String FAILED_INIT = "Failed to initialize Backend Infinispan";

    // /**
    //  * This enumeration lists the possible options for the behaviour of the domain object's {@link
    //  * IdentityMap}.
    //  */
    // public static enum MapType {
    //     /**
    //      * A global {@link IdentityMap}.  An object instance is shared application-wide, i.e. if any
    //      * two references to a {@link DomainObject} have the same identifier (OID), then they refer
    //      * the same object (they are <code>==</code>).  The same object instance will not exist more
    //      * than once in memory.
    //      */
    //     SHARED,
    //         /**
    //          * A transaction-local {@link IdentityMap}.  Each transaction that accesses a given
    //          * domain object will get a different copy of that object.  The identity map assotiated
    //          * with the transaction will be destroyed at the end of the transaction.
    //          */
    //         LOCAL };

    // /**
    //  * This <strong>optional</strong> parameter specifies whether the object identity map to use
    //  * should have either a global (<code>SHARED</code>) or a transaction-local (<code>LOCAL</code>)
    //  * scope.  The default value for this parameter is {@link MapType#SHARED}.
    //  */
    // protected MapType identityMap = MapType.SHARED;

    /**
     * This <strong>required</strong> parameter specifies the location of the XML file used to
     * configure Infinispan.  This file should be available in the application's classpath.
     */
    protected String ispnConfigFile = null;

    /**
     * See {@link #useGrouping()}.
     */
    protected boolean useGrouping = false;

    /**
     * for test only
     */
    protected Configuration defaultConfiguration;

    protected GlobalConfiguration globalConfiguration;

    protected final InfinispanBackEnd backEnd;


    public InfinispanConfig() {
        this.backEnd = InfinispanBackEnd.getInstance();
    }

    // process this config's parameters

    // protected void identityMapFromString(String value) {
    //     String cleanValue = value.trim().toUpperCase();
    //     try {
    //         identityMap = MapType.valueOf(cleanValue);
    //     } catch (IllegalArgumentException e) {
    //         String message = "Unknown value for configuration property 'identityMap': " + value;
    //         logger.error(message);
    //         throw new ConfigError(message, e);
    //     }
    // }

    public final String getIspnConfigFile() {
        return this.ispnConfigFile;
    }

    public final void setIspnConfigFile(String ispnConfigFile) {
        this.ispnConfigFile = ispnConfigFile;
    }

    /**
     * The Infinispan grouping goal is to group some keys in the same node. A {@link pt.ist.fenixframework.DomainObject}
     * belongs to a group if, when created, it is provided a {@link eu.cloudtm.LocalityHints} is provided with
     * {@link eu.cloudtm.Constants#GROUP_ID}.
     *
     * @return {@code true} to use the grouping API.
     */
    public final boolean useGrouping() {
        return useGrouping;
    }

    public final void setUseGrouping(boolean useGrouping) {
        this.useGrouping = useGrouping;
    }

    public final Configuration getDefaultConfiguration() {
        return defaultConfiguration;
    }

    public final void setDefaultConfiguration(Configuration defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public final GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public final void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    @Override
    protected void init() {
        try {
            this.backEnd.configInfinispan(this);
        } catch (Exception e) {
            throw new ConfigError(FAILED_INIT, e);
        }

        // DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);
        super.init();
    }

    @Override
    protected void checkConfig() {
        super.checkConfig();
        if (ispnConfigFile == null && (defaultConfiguration == null || globalConfiguration == null)) {
            missingRequired("ispnConfigFile");
        }
    }

    @Override
    public InfinispanBackEnd getBackEnd() {
        return this.backEnd;
    }
}
