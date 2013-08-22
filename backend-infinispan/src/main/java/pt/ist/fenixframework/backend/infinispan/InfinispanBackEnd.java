package pt.ist.fenixframework.backend.infinispan;

import eu.cloudtm.LocalityHints;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.GroupsConfiguration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.context.Flag;
import org.infinispan.dataplacement.c50.C50MLObjectLookupFactory;
import org.infinispan.dataplacement.c50.keyfeature.KeyFeatureManager;
import org.infinispan.dataplacement.lookup.ObjectLookupFactory;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.BasicClusterInformation;
import pt.ist.fenixframework.backend.ClusterInformation;
import pt.ist.fenixframework.backend.OID;
import pt.ist.fenixframework.backend.infinispan.messaging.LoadBalancePolicy;
import pt.ist.fenixframework.backend.infinispan.messaging.LocalMessagingQueue;
import pt.ist.fenixframework.backend.infinispan.messaging.RemoteMessagingQueue;
import pt.ist.fenixframework.backend.infinispan.messaging.ThreadPoolRequestProcessor;
import pt.ist.fenixframework.backend.infinispan.messaging.drd.DRDLoadBalancePolicy;
import pt.ist.fenixframework.core.*;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Slot;
import pt.ist.fenixframework.messaging.MessagingQueue;

import javax.transaction.SystemException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class InfinispanBackEnd implements BackEnd, InfinispanTransactionManager.TransactionClassManager {
    public static final String BACKEND_NAME = "ispn";
    private static final Logger logger = LoggerFactory.getLogger(InfinispanBackEnd.class);
    private static final InfinispanBackEnd instance = new InfinispanBackEnd();
    private static final BoundedConcurrentHashMap<String, Object> L2_CACHE = new BoundedConcurrentHashMap<String, Object>(4000000, 64, BoundedConcurrentHashMap.Eviction.LRU);
    // 4M references, approx 32MB in 64bit references + CHM structure (mostly eviction related) = at most 40MB for the L2 Cache
    // assuming average of 40 bytes per object, this would keep in memory 160MB of additional objects
    // so at most we'd keep additional 200MB
    
    protected final InfinispanTransactionManager transactionManager;
    protected Cache<String, Object> readCache;
    protected Cache<String, Object> writeCache;
    protected Cache<String, Object> ghostCache;
    private LocalMessagingQueue localMessagingQueue;
    private String jgroupsMessagingConfigurationFile;
    private String loadBalancePolicyClass;

    private InfinispanBackEnd() {
        this.transactionManager = new InfinispanTransactionManager(this);
    }

    public static InfinispanBackEnd getInstance() {
        return instance;
    }

    public static <T> T getL2Cache(String key) {
        return (T) L2_CACHE.get(key);
    }

    public static void putL2Cache(String key, Object obj) {
        if (obj == null) {
            L2_CACHE.remove(key);
        } else {
            L2_CACHE.put(key, obj);
        }
    }

    @Override
    public String getName() {
        return BACKEND_NAME;
    }

    @Override
    public DomainRoot getDomainRoot() {
        return fromOid(OID.ROOT_OBJECT_ID);
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        return fromOid(OID.fromExternalId(externalId));
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        OID internalId = (OID) oid;
        if (logger.isTraceEnabled()) {
            logger.trace("fromOid(" + internalId.getFullId() + ")");
        }

        IdentityMap cache = getIdentityMap();
        AbstractDomainObject obj = cache.lookup(internalId);

        if (obj == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("Object not found in IdentityMap: " + internalId.getFullId());
            }
            obj = DomainObjectAllocator.allocateObject(internalId.getObjClass(), internalId);

            // cache object and return the canonical object
            obj = cache.cache(obj);
        }

        return (T) obj;
    }

    /**
     * Shuts down Infinispan's cache(s) and the(ir) manager(s)
     */
    @Override
    public void shutdown() {
        // not sure whether is still safe, after a stop() to getCacheManager(), so I get it first
        EmbeddedCacheManager manager = readCache.getCacheManager();
        readCache.stop();
        manager.stop();
    }

    @Override
    public void setTransactionClass(String transactionClass) {
        if (transactionClass != null) {
            writeCache.getAdvancedCache().setTransactionClass(transactionClass);
        }
    }

    /**
     * Store in Infinispan. This method supports null values. This method is used by the code
     * generated in the Domain Objects.
     */
    public final void cachePut(String key, Object value) {
        writeCache.put(key, (value != null) ? value : Externalization.NULL_OBJECT);
    }

    /**
     * Reads from Infinispan a value with a given key. This method is used by the code generated in
     * the Domain Objects.
     */
    public final <T> T cacheGet(String key) {
        Object obj = readCache.get(key);
        return (T) (obj instanceof Externalization.NullClass ? null : obj);
    }

    /**
     * Reads from Infinispan a value with a given key such that the transactional context does not keep
     * track of this key. This means that this read can never cause the transaction to abort.
     * This method is used by the code generated in the Domain Objects.
     */
    public final <T> T cacheGetGhost(String key) {
        Object obj = ghostCache.getAdvancedCache().withFlags(Flag.READ_WITHOUT_REGISTERING).get(key);
        return (T) (obj instanceof Externalization.NullClass ? null : obj);
    }

    public final void registerGet(String key) {
        AdvancedCache advCache = readCache.getAdvancedCache();
        try {
            advCache.getTxTable().getLocalTransaction(advCache.getTransactionManager().getTransaction()).addReadKey(key);
        } catch (SystemException e) {
            logger.error("Exception while getting the current JPA Transaction to register a key read", e);
        }
    }

    /**
     * WARNING: This is a backend-specific method. It was added as an hack to enable some tests by
     * Algorithmica and will be removed later. The programmer should not use this method directly,
     * because by doing so the code becomes backend-dependent.
     */
    @Deprecated
    public final Cache getInfinispanCache() {
        return this.readCache;
    }

    @Override
    public <T extends DomainObject> T getOwnerDomainObject(String storageKey) {
        String fullId = storageKey.substring(0, storageKey.lastIndexOf(':')); // ok, because it ends with the slot name
        return fromOid(OID.recoverFromFullId(fullId));
    }

    @Override
    public String[] getStorageKeys(DomainObject domainObject) {
        if (domainObject == null) {
            return new String[0];
        }

        DomainModel domainModel = FenixFramework.getDomainModel();
        DomainClass domClass = domainModel.findClass(domainObject.getClass().getName());
        if (domClass == null) {
            return new String[0];
        }

        String oid = ((InfinispanDomainObject) domainObject).getOid().getFullId();

        ArrayList<String> keys = new ArrayList<String>();
        for (Slot slot : domClass.getSlotsList()) {
            keys.add(oid + ':' + slot.getName());
        }
        return keys.toArray(new String[keys.size()]);
    }

    public final LocalityHints getLocalityHints(String externalId) {
        return OID.getLocalityHint(externalId);
    }

    @Override
    public ClusterInformation getClusterInformation() {
        RpcManager rpcManager = readCache.getAdvancedCache().getRpcManager();
        //if the cache does not have the rpc manager, then the cache is configured in local mode only
        if (rpcManager == null) {
            return ClusterInformation.LOCAL_MODE;
        }
        List<Address> members = rpcManager.getMembers();
        int thisMemberIndex = members.indexOf(rpcManager.getAddress());
        if (thisMemberIndex < 0) {
            return ClusterInformation.NOT_AVAILABLE;
        }

        return new BasicClusterInformation(members.size(), thisMemberIndex);
    }

    @Override
    public synchronized MessagingQueue createMessagingQueue(String appName) throws Exception {
        if (appName == null || appName.isEmpty()) {
            throw new IllegalArgumentException("Application name should be not null neither empty");
        }
        if (readCache == null) {
            throw new IllegalStateException("Infinispan backend is not initialized!");
        }
        String localName = readCache.getName();
        if (localName.equals(appName)) {
            if (localMessagingQueue == null) {
                throw new IllegalStateException("Local Message Queue is not initialized!");
            }
            return localMessagingQueue;
        } else {
            return new RemoteMessagingQueue(appName, localName, jgroupsMessagingConfigurationFile,
                    readCache.getAdvancedCache().getComponentRegistry().getCacheMarshaller(), createLoadBalancePolicy());
        }
    }

    protected void configInfinispan(InfinispanConfig config) throws Exception {
        setupCache(config);
        setupTxManager();
        setupMessaging(config);
        config.waitForExpectedInitialNodes("backend-infinispan-init-barrier");
    }

    protected IdentityMap getIdentityMap() {
        return SharedIdentityMap.getCache();
    }

    private void setupCache(InfinispanConfig config) {
        long start = System.currentTimeMillis();
        try {
            String applicationName = config.getAppName();
            Configuration defaultConfiguration;
            GlobalConfiguration globalConfiguration;
            if (config.getDefaultConfiguration() != null && config.getGlobalConfiguration() != null) {
                defaultConfiguration = config.getDefaultConfiguration();
                globalConfiguration = config.getGlobalConfiguration();
            } else {
                ConfigurationBuilderHolder holder = new ParserRegistry(Thread.currentThread().getContextClassLoader())
                        .parseFile(config.getIspnConfigFile());
                globalConfiguration = holder.getGlobalConfigurationBuilder().build();
                defaultConfiguration = holder.getDefaultConfigurationBuilder().build();
            }
            EmbeddedCacheManager cacheManager = new DefaultCacheManager(globalConfiguration, defaultConfiguration);
            Configuration domainCacheConfiguration = updateAndValidateConfiguration(defaultConfiguration, config);
            cacheManager.defineConfiguration(applicationName, domainCacheConfiguration);
            readCache = cacheManager.getCache(applicationName);
            writeCache = readCache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
            ghostCache = readCache.getAdvancedCache().withFlags(Flag.READ_WITHOUT_REGISTERING);
        } catch (IOException ioe) {
            String message = "Error creating Infinispan cache manager with configuration file: " + config.getIspnConfigFile();
            if (logger.isErrorEnabled()) {
                logger.error(message, ioe);
            }
            throw new Error(message, ioe);
        } catch (Exception e) {
            String message = "Error creating Infinispan cache manager";
            if (logger.isErrorEnabled()) {
                logger.error(message, e);
            }
            throw new Error(message, e);
        }

        if (logger.isDebugEnabled()) {
            DateFormat df = new SimpleDateFormat("HH:mm.ss");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            logger.debug("Infinispan initialization took " + df.format(new Date(System.currentTimeMillis() - start)));
        }
    }

    private Configuration updateAndValidateConfiguration(Configuration defaultConfiguration, InfinispanConfig config)
            throws Exception {
        ConfigurationBuilder builder = new ConfigurationBuilder().read(defaultConfiguration);

        if (config.useGrouping()) {
            builder.clustering().hash().groups().enabled().addGrouper(new FenixFrameworkGrouper(this));
        } else if (defaultConfiguration.clustering().hash().groups().enabled()) {
            GroupsConfiguration groupsConfiguration = defaultConfiguration.clustering().hash().groups();
            int groupers = groupsConfiguration.groupers().size();
            if (groupers == 0) {
                builder.clustering().hash().groups().addGrouper(new FenixFrameworkGrouper(this));
            } else if (groupers != 1 || !groupsConfiguration.groupers().get(0).getClass().equals(FenixFrameworkGrouper.class)) {
                builder.clustering().hash().groups().clearGroupers().addGrouper(new FenixFrameworkGrouper(this));
            }
        }

        if (defaultConfiguration.dataPlacement().enabled()) {
            ObjectLookupFactory factory = defaultConfiguration.dataPlacement().objectLookupFactory();
            if (factory != null && factory instanceof C50MLObjectLookupFactory) {
                String managerClassName = defaultConfiguration.dataPlacement().properties()
                        .getProperty(C50MLObjectLookupFactory.KEY_FEATURE_MANAGER);
                KeyFeatureManager manager = Util.<KeyFeatureManager>loadClass(managerClassName,
                        Thread.currentThread().getContextClassLoader())
                        .newInstance();
                LocalityHints.init(manager);
            }
        }

        return builder.build();
    }

    private void setupTxManager() {
        transactionManager.setDelegateTxManager(readCache.getAdvancedCache().getTransactionManager());
    }

    private void setupMessaging(InfinispanConfig config) throws Exception {
        this.jgroupsMessagingConfigurationFile = config.messagingJgroupsFile();
        this.loadBalancePolicyClass = config.loadBalancePolicyClass();
        String applicationName = config.getAppName();
        ThreadPoolRequestProcessor threadPoolRequestProcessor = new ThreadPoolRequestProcessor(
                config.coreThreadPoolSize(), config.maxThreadPoolSize(), config.keepAliveTime(),
                config.maxQueueSizeLoadNotification(), config.minQueueSizeLoadNotification(), applicationName);
        localMessagingQueue = new LocalMessagingQueue(applicationName, readCache, jgroupsMessagingConfigurationFile,
                threadPoolRequestProcessor, createLoadBalancePolicy());
    }

    private LoadBalancePolicy createLoadBalancePolicy() throws Exception {
        if (loadBalancePolicyClass == null || loadBalancePolicyClass.isEmpty()) {
            return new DRDLoadBalancePolicy();
        }
        Class<? extends LoadBalancePolicy> loadBalancePolicy = (Class<? extends LoadBalancePolicy>)
                pt.ist.fenixframework.util.Util.loadClass(loadBalancePolicyClass);
        if (loadBalancePolicy == null) {
            return new DRDLoadBalancePolicy();
        }
        return loadBalancePolicy.newInstance();
    }
}
