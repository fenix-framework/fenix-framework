package pt.ist.fenixframework.backend.infinispan;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.manager.DefaultCacheManager;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.TransactionManager;
import pt.ist.fenixframework.backend.BackEnd;

public class InfinispanBackEnd implements BackEnd {
    private static final Logger logger = Logger.getLogger(InfinispanBackEnd.class);

    private static final String DOMAIN_CACHE_NAME = "DomainCache";

    protected final TransactionManager transactionManager;
    protected Cache<String, Object> domainCache;

    public InfinispanBackEnd() {
        this.transactionManager = new InfinispanTransactionManager();
    }

    private static String ROOT_OBJECT_ID = "ROOT_OBJECT";
    @Override
    public DomainRoot getDomainRoot() {
        DomainRoot root = InfinispanDomainObject.fromOid(ROOT_OBJECT_ID);
        if (root == null) {
            root = new DomainRoot(); // which automatically caches this instance, but does not
                                     // ensure that it is the first, as a concurrent request might
                                     // create another

            // so we get it again from the cache before returning if
            root = InfinispanDomainObject.fromOid(ROOT_OBJECT_ID);
            assert root != null; // there must be at least one
        }
        return root;
    }

    @Override
    public <T extends DomainObject> T getDomainObject(String externalId) {
        // return CoreDomainObject.fromOid(Long.parseLong(externalId));
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        return InfinispanDomainObject.fromOid((String)oid);
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

    protected void configInfinispan(InfinispanConfig config) {
        long start = System.currentTimeMillis();
        CacheContainer cc = null;
        try {
            cc = new DefaultCacheManager(config.getIspnConfigFile());
        } catch (java.io.IOException ioe) {
            String message = "Error creating Infinispan cache manager with configuration file: "
                + config.getIspnConfigFile();
            logger.fatal(message, ioe);
            throw new Error(message, ioe);
        }
        domainCache = cc.getCache(DOMAIN_CACHE_NAME);
        if (logger.isEnabledFor(Level.DEBUG)) {
            DateFormat df = new SimpleDateFormat("HH:mm.ss");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            logger.debug("Infinispan initialization took " +
                         df.format(new Date(System.currentTimeMillis() - start)));
        }
    }

}
