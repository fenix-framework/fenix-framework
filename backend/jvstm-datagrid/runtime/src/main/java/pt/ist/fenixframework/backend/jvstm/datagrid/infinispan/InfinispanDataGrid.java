package pt.ist.fenixframework.backend.jvstm.datagrid.infinispan;

import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.context.Flag;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.backend.jvstm.datagrid.DataGrid;
import pt.ist.fenixframework.backend.jvstm.datagrid.JvstmDataGridConfig;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;

public class InfinispanDataGrid implements DataGrid {

    private static final Logger logger = LoggerFactory.getLogger(InfinispanDataGrid.class);

    private static final String INCORRECT_CONFIG =
            "The InfinispanDataGrid requires config.class=pt.ist.fenixframework.backend.jvstm.datagrid.infinispan.DataGridConfig";

    static final String CACHE_NAME = "FFCache";

    DefaultCacheManager cacheManager;
    TransactionManager transactionManager;
    Cache<String, Object> cache;
    Cache<String, Object> cacheOptimizeWrites;

    @Override
    public void init(JvstmDataGridConfig config) {
        DataGridConfig dgConfig = null;
        try {
            dgConfig = (DataGridConfig) config;
        } catch (ClassCastException e) {
            logger.error(INCORRECT_CONFIG);
            throw new ConfigError(INCORRECT_CONFIG);
        }

        String ispnConfigFile = dgConfig.getDataGridConfigFile();

        createCacheContainer(ispnConfigFile);
        initTransactionManager();
        createCache();
    }

    @Override
    public void stop() {
        this.cacheManager.stop();
    }

    @Override
    public Object get(Object key) {
        return this.cache.get(key);
    }

    @Override
    public void put(Object key, Object value) {
        this.cacheOptimizeWrites.put(key.toString(), value);
    }

    @Override
    public void beginTransaction() {
        try {
            getTransactionManager().begin();
        } catch (Exception e) {
            logger.warn("Failed to start a data grid transaction: {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void commitTransaction() {
        try {
            getTransactionManager().commit();
        } catch (RuntimeException e) {
            logger.warn("Failed to commit a data grid transaction: {}", e);
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to commit a data grid transaction: {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void rollbackTransaction() {
        try {
            getTransactionManager().rollback();
        } catch (RuntimeException e) {
            logger.warn("Failed to rollback a data grid transaction: {}", e);
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to rollback a data grid transaction: {}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean inTransaction() {
        try {
            return getTransactionManager().getTransaction() != null;
        } catch (SystemException e) {
            logger.warn("Failed to get transaction status for a data grid transaction: {}", e);
            throw new RuntimeException(e);
        }
    }

    private void createCacheContainer(String ispnConfigFile) {
        try {
            if (ispnConfigFile == null || ispnConfigFile.isEmpty()) {
                logger.info("Initializing CacheManager with defaults", ispnConfigFile);
                this.cacheManager = new DefaultCacheManager();
            } else {
                logger.info("Initializing CacheManager with default configuration provided in {}", ispnConfigFile);
                this.cacheManager = new DefaultCacheManager(ispnConfigFile);
            }
        } catch (java.io.IOException e) {
            logger.error("Error creating cache manager with configuration file: {} -> {}", ispnConfigFile, e);
            throw new PersistenceException(e);
        }
    }

    // we need the transaction manager to init the caches. So, we just create a dummy cache to get its TxManager :-(  There should be a better way to do this...
    private void initTransactionManager() {
        Configuration conf = makeRequiredConfiguration();
        // for the dummy cache disable, cache loaders if any was configured
        ConfigurationBuilder confBuilder = new ConfigurationBuilder().read(conf);
        confBuilder.loaders().clearCacheLoaders();
        conf = confBuilder.build();

        String DUMMY_CACHE = "dummy-cache";
        logger.debug("Configuration for {} is: {}", DUMMY_CACHE, conf.toString());

        try {
            this.cacheManager.defineConfiguration(DUMMY_CACHE, conf);
            Cache<?, ?> dummyCache = this.cacheManager.getCache(DUMMY_CACHE);
            TransactionManager tm = dummyCache.getAdvancedCache().getTransactionManager();
            this.transactionManager = tm;
        } catch (Exception e) {
            logger.error("Failed to get Repository TransactionManager", e);
            throw new PersistenceException(e);
        }
    }

    // ensure the required configuration regardless of possible extra stuff in the configuration file
    private Configuration makeRequiredConfiguration() {
        logger.debug("Ensuring required Infinispan configuration");

        // get default config
        Configuration defaultConf = this.cacheManager.getDefaultCacheConfiguration();

        // initialize config builder with default config
        ConfigurationBuilder confBuilder = new ConfigurationBuilder();
        confBuilder.read(defaultConf);

        /* enforce required configuration */

        // use REPEATABLE_READ
//        confBuilder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ).concurrencyLevel(32).writeSkewCheck(true)
//                .useLockStriping(false).lockAcquisitionTimeout(10000);
        // use READ_COMMITTED
        confBuilder.locking().isolationLevel(IsolationLevel.READ_COMMITTED).concurrencyLevel(32).useLockStriping(false)
                .lockAcquisitionTimeout(10000);

        // detect DEALOCKS (is this needed? it performs better when on... go figure)
        confBuilder.deadlockDetection().enable();
//        confBuilder.deadlockDetection().disable();

        // transactional optimistic cache (useSynchronization(true) provides better performance)
        confBuilder.transaction().transactionMode(TransactionMode.TRANSACTIONAL).syncRollbackPhase(false).cacheStopTimeout(30000)
                .useSynchronization(true).syncCommitPhase(true).lockingMode(LockingMode.OPTIMISTIC)
                .use1PcForAutoCommitTransactions(false).autoCommit(false);

        // use versioning (check if it's really needed, especially in READ_COMMITTED!) 
//        confBuilder.versioning().enable().scheme(VersioningScheme.SIMPLE);
        confBuilder.versioning().disable();

        // disable expiration
        confBuilder.expiration().wakeUpInterval(-1);

        Configuration conf = confBuilder.build();

        // allow eviction only when using a cache loader. Causes writeSkew exception so that needs to be off when using eviction :-(
//        if (conf.loaders().usingCacheLoaders()) {
//            confBuilder = new ConfigurationBuilder().read(conf);
//            confBuilder.eviction().strategy(EvictionStrategy.LIRS).maxEntries(1/*talk about randomness*/);
//?            confBuilder.eviction().threadPolicy(EvictionThreadPolicy.PIGGYBACK);
//        } else {
        confBuilder.eviction().strategy(EvictionStrategy.NONE);
        confBuilder.eviction().maxEntries(-1);
//        }

        conf = confBuilder.build();

        return conf;
    }

    private void createCache() {
        Configuration conf = makeRequiredConfiguration();

        logger.debug("Configuration for {} is: {}", CACHE_NAME, conf.toString());

        this.cacheManager.defineConfiguration(CACHE_NAME, conf);
        this.cache = this.cacheManager.getCache(CACHE_NAME);
        this.cacheOptimizeWrites = this.cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
//        this.cache = doWithinBackingTransactionIfNeeded(new Callable<Cache<String, Object>>() {
//            @Override
//            public Cache<String, Object> call() {
//                return finalCacheManager.getCache(CACHE_NAME);
//            }
//        });
    }

    private TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

}
