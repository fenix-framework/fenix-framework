package pt.ist.fenixframework.backend.infinispan;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
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
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.Externalization;
import pt.ist.fenixframework.core.IdentityMap;
import pt.ist.fenixframework.core.SharedIdentityMap;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Slot;

public class InfinispanBackEnd implements BackEnd {
    private static final Logger logger = LoggerFactory.getLogger(InfinispanBackEnd.class);

    public static final String BACKEND_NAME = "ispn";
    private static final String DOMAIN_CACHE_NAME = "DomainCache";

    private static final InfinispanBackEnd instance = new InfinispanBackEnd();

    protected final InfinispanTransactionManager transactionManager;
    protected Cache<String, Object> domainCache;

    private InfinispanBackEnd() {
        this.transactionManager = new InfinispanTransactionManager();
    }

    public static InfinispanBackEnd getInstance() {
        return instance;
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
        EmbeddedCacheManager manager = domainCache.getCacheManager();
        domainCache.stop();
        manager.stop();
    }

    protected void configInfinispan(InfinispanConfig config) throws Exception {
        setupCache(config);
        setupTxManager(config);
        config.waitForExpectedInitialNodes("backend-infinispan-init-barrier");
    }

    private void setupCache(InfinispanConfig config) {
        long start = System.currentTimeMillis();
        CacheContainer cc = null;
        try {
            cc = new DefaultCacheManager(config.getIspnConfigFile());
        } catch (java.io.IOException ioe) {
            String message = "Error creating Infinispan cache manager with configuration file: " + config.getIspnConfigFile();
            logger.error(message, ioe);
            throw new Error(message, ioe);
        }
        domainCache = cc.getCache(DOMAIN_CACHE_NAME);
        if (logger.isDebugEnabled()) {
            DateFormat df = new SimpleDateFormat("HH:mm.ss");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            logger.debug("Infinispan initialization took " + df.format(new Date(System.currentTimeMillis() - start)));
        }
    }

    private void setupTxManager(InfinispanConfig config) {
        transactionManager.setDelegateTxManager(domainCache.getAdvancedCache().getTransactionManager());
    }

    protected IdentityMap getIdentityMap() {
        return SharedIdentityMap.getCache();
    }

    /**
     * Store in Infinispan. This method supports null values. This method is used by the code
     * generated in the Domain Objects.
     */
    public final void cachePut(String key, Object value) {
        domainCache.put(key, (value != null) ? value : Externalization.NULL_OBJECT);
    }

    /**
     * Reads from Infinispan a value with a given key. This method is used by the code generated in
     * the Domain Objects.
     */
    public final <T> T cacheGet(String key) {
        Object obj = domainCache.get(key);
        return (T) (obj instanceof Externalization.NullClass ? null : obj);
    }

    /**
     * WARNING: This is a backend-specific method. It was added as an hack to enable some tests by
     * Algorithmica and will be removed later. The programmer should not use this method directly,
     * because by doing so the code becomes backend-dependent.
     */
    @Deprecated
    public final Cache getInfinispanCache() {
        return this.domainCache;
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

    @Override
    public ClusterInformation getClusterInformation() {
        RpcManager rpcManager = domainCache.getAdvancedCache().getRpcManager();
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
}
