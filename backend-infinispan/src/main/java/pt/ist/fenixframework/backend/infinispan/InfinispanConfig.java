package pt.ist.fenixframework.backend.infinispan;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchConfig;

/**
 * This is the infinispan configuration manager used by the fenix-framework-backend-infinispan
 * project.
 *
 * @see Config
 */
public class InfinispanConfig extends HibernateSearchConfig {
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
    protected final InfinispanBackEnd backEnd;
    /**
     * This <strong>required</strong> parameter specifies the location of the XML file used to
     * configure Infinispan.  This file should be available in the application's classpath.
     */
    protected String ispnConfigFile = null;
    /**
     * See {@link #useGrouping()}.
     */
    protected boolean useGrouping = false;
    protected int coreThreadPoolSize = 1;
    protected int maxThreadPoolSize = 8;
    protected int keepAliveTime = 60000;
    protected int maxQueueSizeLoadNotification = 100;
    protected int minQueueSizeLoadNotification = 10;
    protected String messagingJgroupsFile = "jgrp-messaging.xml";
    protected String loadBalancePolicyClass = null;
    /**
     * for test only
     */
    protected Configuration defaultConfiguration;
    protected GlobalConfiguration globalConfiguration;


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

    public final void useGroupingFromString(String value) {
        setUseGrouping(Boolean.valueOf(value));
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
    public InfinispanBackEnd getBackEnd() {
        return this.backEnd;
    }

    public final int coreThreadPoolSize() {
        return coreThreadPoolSize;
    }

    public final void setCoreThreadPoolSize(int coreThreadPoolSize) {
        this.coreThreadPoolSize = coreThreadPoolSize;
    }

    public final void coreThreadPoolSizeFromString(String value) {
        setCoreThreadPoolSize(Integer.valueOf(value));
    }

    public final int maxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public final void setMaxThreadPoolSize(int maxThreadPoolSize) {
        this.maxThreadPoolSize = maxThreadPoolSize;
    }

    public final void maxThreadPoolSizeFromString(String value) {
        setMaxThreadPoolSize(Integer.valueOf(value));
    }

    public final int keepAliveTime() {
        return keepAliveTime;
    }

    public final void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public final void keepAliveTimeFromString(String value) {
        setKeepAliveTime(Integer.valueOf(value));
    }

    public final int maxQueueSizeLoadNotification() {
        return maxQueueSizeLoadNotification;
    }

    public final void setMaxQueueSizeLoadNotification(int maxQueueSizeLoadNotification) {
        this.maxQueueSizeLoadNotification = maxQueueSizeLoadNotification;
    }

    public final void maxQueueSizeLoadNotificationFromString(String value) {
        setMaxQueueSizeLoadNotification(Integer.valueOf(value));
    }

    public final int minQueueSizeLoadNotification() {
        return minQueueSizeLoadNotification;
    }

    public final void setMinQueueSizeLoadNotification(int minQueueSizeLoadNotification) {
        this.minQueueSizeLoadNotification = minQueueSizeLoadNotification;
    }

    public final void minQueueSizeLoadNotificationFromString(String value) {
        setMinQueueSizeLoadNotification(Integer.valueOf(value));
    }

    public final String messagingJgroupsFile() {
        return messagingJgroupsFile;
    }

    public final String loadBalancePolicyClass() {
        return loadBalancePolicyClass;
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
}
