/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.datagrid.infinispan;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
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

import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeConfig;
import pt.ist.fenixframework.backend.jvstm.repository.DataGrid;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;

public class InfinispanDataGrid implements DataGrid {

    private static final Logger logger = LoggerFactory.getLogger(InfinispanDataGrid.class);

    /**
     * This <strong>optional</strong> parameter specifies the location of the XML file used to configure Infinispan. This file
     * should be available in the application's classpath. This can be set via FenixFramework config file by prefixing it with
     * {@link JvstmLockFreeConfig#DATAGRID_PARAM_PREFIX}.
     */
    public static final String ISPN_CONFIG_FILE = "ispnConfigFile";

    static final String CACHE_NAME = "FFCache";

    DefaultCacheManager cacheManager;
    Cache<Object, Object> cache;
    TransactionManager transactionManager;

    @Override
    public void init(JvstmLockFreeConfig config) {
        String ispnConfigFile = config.getDataGridProperty(ISPN_CONFIG_FILE);

        createCacheContainer(ispnConfigFile);
        initTransactionManager();
        createCache();
    }

    @Override
    public void stop() {
        logger.info("stop() invoked");
        this.cacheManager.stop();
        this.cacheManager = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key) {
//        if (!inTransaction()) {
//            logger.error("==========================================");
//            logger.error("WHAT?!?! NO BACKING TRANSACTION!? REALLY?!");
//            logger.error("==========================================");
//            new Exception().printStackTrace();
//            System.exit(1);
//        }
        return (T) this.cache.get(key);
    }

    @Override
    public void put(Object key, Object value) {
//        if (!inTransaction()) {
//            logger.error("==========================================");
//            logger.error("WHAT?!?! NO BACKING TRANSACTION!? REALLY?!");
//            logger.error("==========================================");
//            new Exception().printStackTrace();
//            System.exit(1);
//        }
        this.cache.put(key, value);
    }

    @Override
    public void putIfAbsent(Object key, Object value) {
//        if (!inTransaction()) {
//            logger.error("==========================================");
//            logger.error("WHAT?!?! NO BACKING TRANSACTION!? REALLY?!");
//            logger.error("==========================================");
//            new Exception().printStackTrace();
//            System.exit(1);
//        }
        this.cache.putIfAbsent(key, value);
    }

    @Override
    public void beginTransaction() {
        TransactionManager tm = getTransactionManager();
        try {
            tm.begin();
        } catch (NotSupportedException | SystemException e) {
            logger.warn("Failed to beginTransaction.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void commitTransaction() {
        TransactionManager tm = getTransactionManager();
        try {
            tm.commit();
        } catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SystemException e) {
            logger.warn("Failed to commitTransaction.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public void rollbackTransaction() {
        TransactionManager tm = getTransactionManager();
        try {
            tm.commit();
        } catch (SecurityException | IllegalStateException | RollbackException | HeuristicMixedException
                | HeuristicRollbackException | SystemException e) {
            logger.warn("Failed to rollbackTransaction.", e);
            throw new PersistenceException(e);
        }
    }

    @Override
    public boolean inTransaction() {
        try {
            return getTransactionManager().getTransaction() != null;
        } catch (SystemException e) {
            logger.warn("Failed to compute inTransaction.", e);
            throw new PersistenceException(e);
        }
    }

    // creates the manager of caches for Infinispan
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

        this.cache = this.cacheManager.getCache(CACHE_NAME).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);

//        final DefaultCacheManager finalCacheManager = this.cacheManager;
//        this.cache = doWithinBackingTransactionIfNeeded(new Callable<Cache<Object, Object>>() {
//            @Override
//            public Cache<Object, Object> call() {
//                return finalCacheManager.getCache(CACHE_NAME).getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);
//            }
//        });
    }

//    /* used internally to wrap every access to the dataGrid in a transaction.
//    It only controls transactional operations when running 'control' operations,
//    such as bootstrapping.  The user of this interface InfinispanDataGrid is
//    responsible for starting data grid transactions whenever needed in all other
//    cases */
//    private <T> T doWithinBackingTransactionIfNeeded(Callable<T> command) {
//        boolean inTopLevel = false;
//        boolean commandFinished = false;
//
//        try {
//            if (!inTransaction()) {
//                beginTransaction();
//                inTopLevel = true;
//            }
//
//            T result = command.call();
//            commandFinished = true;
//
//            return result;
//        } catch (Exception e) {
//            throw new PersistenceException(e);
//        } finally {
//            if (inTopLevel) {
//                try {
//                    if (commandFinished) {
//                        commitTransaction();
//                    } else {
//                        rollbackTransaction();
//                    }
//                } catch (Exception e) {
//                    throw new PersistenceException(e);
//                }
//            }
//        }
//    }

    private TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

}
