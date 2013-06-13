/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.infinispan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.Transaction;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VersionedValue;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;
import pt.ist.fenixframework.backend.jvstm.repository.Repository;
import pt.ist.fenixframework.core.Externalization;

//import jvstm.Transaction;

/**
 * This class implements the Repository interface using the Infinispan NoSQL key/value data store.
 */
public class InfinispanRepository implements Repository {

    private static final Logger logger = LoggerFactory.getLogger(InfinispanRepository.class);

    DefaultCacheManager cacheManager;

    // the name of the key used to store the DomainClassInfo instances.
    private final String DOMAIN_CLASS_INFO = "DomainClassInfo";

    private static final String MAX_COMMITTED_TX_ID = "maxTxId";

    // the name of the cache used to store system information
//    static final String SYSTEM_CACHE_NAME = "SystemCache";
    static final String SYSTEM_CACHE_NAME = "FFCache";
    // the name of the cache used to store all instances of all domain classes
//    static final String DOMAIN_CACHE_NAME = "DomainCache";
    static final String DOMAIN_CACHE_NAME = "FFCache";

    // this is a marker, so that when bootstrapping the repository, we can identify whether it already exists 
    private static final String CACHE_IS_NEW = "CacheAlreadExists";

    private static final String KEY_INSTANTIATED_CLASSES = "Set<DomainClassInfo>";

    Cache<String, Object> systemCache;
    Cache<String, DataVersionHolder> domainCache;
    TransactionManager transactionManager;

    private int maxCommittedTxId = -1;

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

    private boolean bootstrapIfNeeded() {
        return doWithinBackingTransactionIfNeeded(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                if (getSystemCache().get(CACHE_IS_NEW) == null) {
                    getSystemCache().put(CACHE_IS_NEW, "false");
                    logger.info("Initialization marker not present. SystemCache is being initialized for the first time.");
                    return true; // repository is new
                } else {
                    logger.info("Initialization marker is present. SystemCache already existed.");
                    return false;  // repository is not new
                }
            }
        });
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

    private void createSystemCache() {
        Configuration conf = makeRequiredConfiguration();

        logger.debug("Configuration for {} is: {}", SYSTEM_CACHE_NAME, conf.toString());

        this.cacheManager.defineConfiguration(SYSTEM_CACHE_NAME, conf);

        final DefaultCacheManager finalCacheManager = this.cacheManager;
        this.systemCache = doWithinBackingTransactionIfNeeded(new Callable<Cache<String, Object>>() {
            @Override
            public Cache<String, Object> call() {
                return finalCacheManager.getCache(SYSTEM_CACHE_NAME);
            }
        });
    }

    private void createDomainCache() {
        Configuration conf = makeRequiredConfiguration();

        logger.debug("Configuration for {} is: {}", DOMAIN_CACHE_NAME, conf.toString());

        this.cacheManager.defineConfiguration(DOMAIN_CACHE_NAME, conf);

        final DefaultCacheManager finalCacheManager = this.cacheManager;
        this.domainCache = doWithinBackingTransactionIfNeeded(new Callable<Cache<String, DataVersionHolder>>() {
            @Override
            public Cache<String, DataVersionHolder> call() {
                return finalCacheManager.getCache(DOMAIN_CACHE_NAME);
            }
        });

    }

    /* some useful methods for accessing Infinispan's caches */

    // returns the single cache object that holds all system information
    private Cache<String, Object> getSystemCache() {
        return this.systemCache;
    }

    // returns the single cache object that holds all domain objects
    private Cache<String, DataVersionHolder> getDomainCache() {
        return this.domainCache;
    }

    protected InfinispanRepository() {
    }

    // used to wrap every access to the cache in a transaction
    private <T> T doWithinBackingTransactionIfNeeded(Callable<T> command) {
        TransactionManager tm = getTransactionManager();
        boolean inTopLevel = false;
        boolean commandFinished = false;

        try {
            if (tm.getTransaction() == null) {
                tm.begin();
                inTopLevel = true;
            }

            T result = command.call();
            commandFinished = true;

            return result;
        } catch (Exception e) {
            throw new PersistenceException(e);
        } finally {
            if (inTopLevel) {
                try {
                    if (commandFinished) {
                        tm.commit();
                    } else {
                        tm.rollback();
                    }
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }
            }
        }
    }

    private TransactionManager getTransactionManager() {
        return this.transactionManager;
//        // either cache uses the same manager instance, so we just pick one cache to get the manager from
//        return getDomainCache().getAdvancedCache().getTransactionManager();
    }

    /* implementation of the Repository interface */

    @Override
    public boolean init(JVSTMConfig jvstmConfig) {
        String ispnConfigFile = ((JvstmIspnConfig) jvstmConfig).getIspnConfigFile();

        createCacheContainer(ispnConfigFile);
        initTransactionManager();
        createSystemCache();
        createDomainCache();
        return bootstrapIfNeeded();
    }

    // get the stored information concerning the DomainClassInfo
    @Override
    public final DomainClassInfo[] getDomainClassInfos() {
        return doWithinBackingTransactionIfNeeded(new Callable<DomainClassInfo[]>() {
            @Override
            public DomainClassInfo[] call() {
                DomainClassInfo infos[] = (DomainClassInfo[]) getSystemCache().get(DOMAIN_CLASS_INFO);

                if (infos == null) {
                    return new DomainClassInfo[0];
                }

                return infos;
            }
        });

    }

    // update the stored information concerning the DomainClassInfo adding the vector domainClassInfos
    @Override
    public void storeDomainClassInfos(final DomainClassInfo[] newDomainClassInfos) {
        if (newDomainClassInfos == null || newDomainClassInfos.length == 0) {
            return;
        }

        int i, j;
        final DomainClassInfo[] all, stored = getDomainClassInfos();
        all = new DomainClassInfo[stored.length + newDomainClassInfos.length];
        for (j = 0; j < stored.length; j++) {
            all[j] = stored[j];
        }

        for (i = 0; i < newDomainClassInfos.length; i++, j++) {
            all[j] = newDomainClassInfos[i];
        }

        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                getSystemCache().put(DOMAIN_CLASS_INFO, all);
                return null;
            }
        });

    }

    @Override
    public int getMaxCounterForClass(DomainClassInfo domainClassInfo) {
        final String key = makeKeyForMaxCounter(domainClassInfo);

        return doWithinBackingTransactionIfNeeded(new Callable<Integer>() {
            @Override
            public Integer call() {
                Integer max = (Integer) getSystemCache().get(key);

                if (max == null) {
                    return -1;
                }

                return max;
            }
        });

    }

    // called when a new domain object is created inside a transaction and a new oid is assigned to the
    // created object.  Register the class info to be updated upon commit
    @Override
    public void updateMaxCounterForClass(DomainClassInfo domainClassInfo, final int newCounterValue) {
        Transaction current = FenixFramework.getTransaction();

        Set<DomainClassInfo> infos = current.getFromContext(KEY_INSTANTIATED_CLASSES);
        if (infos == null) {
            infos = new HashSet<DomainClassInfo>();
            current.putInContext(KEY_INSTANTIATED_CLASSES, infos);
        }

        if (infos.add(domainClassInfo)) {
            logger.debug("Will update counter for instances of {} upon commit.", domainClassInfo.domainClassName);
        }
    }

    private String makeKeyForMaxCounter(DomainClassInfo domainClassInfo) {
        return String.valueOf(DomainClassInfo.getServerId()) + ":" + domainClassInfo.classId;
    }

    // reloads a primitive value from the storage for the specified box
    @Override
    public void reloadPrimitiveAttribute(VBox box) {
        reloadAttribute(box);
    }

    // reloads a reference attribute from the storage for the specified box
    @Override
    public void reloadReferenceAttribute(VBox box) {
        reloadAttribute(box);
    }

    // stores persistently a set of changes
    // the third arguments represents the reference used by the stm to represent null objects.
    @Override
    public void persistChanges(final Set<Entry<jvstm.VBox, Object>> changes, final int txNumber, final Object nullObject) {
        final Cache<String, DataVersionHolder> cache = getDomainCache();

        // begin repo tx here
        doWithinBackingTransactionIfNeeded(new Callable<Void>() {
            @Override
            public Void call() {
                updatePersistentInstanceCounters();
                persistCommittedTransactionNumber(txNumber);

                for (Entry<jvstm.VBox, Object> entry : changes) {
                    VBox vbox = (VBox) entry.getKey();
                    Object newValue = entry.getValue();

                    newValue = (newValue == nullObject) ? null : newValue;

                    String key = makeKeyFor(vbox);
                    DataVersionHolder current = cache.get(key);
                    DataVersionHolder newVersion;
                    byte[] externalizedData = Externalization.externalizeObject(newValue);

                    if (current != null) {
                        cache.put(makeVersionedKey(key, current.version), current); // TODO: colocar aqui um timeout ?
                        newVersion = new DataVersionHolder(txNumber, current.version, externalizedData);
                    } else {
                        newVersion = new DataVersionHolder(txNumber, -1, externalizedData);
                    }

                    cache.put(key, newVersion); // TODO: colocar aqui um timeout
                }
                return null;
            }
        });

    }

    // returns the greatest committed transaction number. This implementation 
    // assumes a single JVSTM. Note the absence of any synchronization.
    @Override
    public int getMaxCommittedTxNumber() {
        if (maxCommittedTxId == -1) {
            Integer max = doWithinBackingTransactionIfNeeded(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return (Integer) getSystemCache().get(MAX_COMMITTED_TX_ID);
                }
            });

            if (max == null) {
                maxCommittedTxId = 0;
            } else {
                maxCommittedTxId = max.intValue();
            }
        }

        return maxCommittedTxId;
    }

    // close the connection to the repository
    @Override
    public void closeRepository() {
        logger.info("closeRepository()");
        this.cacheManager.stop();
        this.cacheManager = null;
        maxCommittedTxId = -1;
    }

    /* utility methods used by the implementation of the Repository interface methods */

    @Override
    public void reloadAttribute(VBox box) {
        int txNumber = jvstm.Transaction.current().getNumber();

        List<VersionedValue> vvalues = getMostRecentVersions(box, txNumber);
        box.mergeVersions(vvalues);
    }

    List<VersionedValue> getMostRecentVersions(final VBox vbox, final int desiredVersion) {
        final Cache<String, DataVersionHolder> cache = getDomainCache();
        final String key = makeKeyFor(vbox);

        return doWithinBackingTransactionIfNeeded(new Callable<List<VersionedValue>>() {
            @Override
            public List<VersionedValue> call() {
                ArrayList<VersionedValue> result = new ArrayList<VersionedValue>();
                DataVersionHolder current;

                current = cache.get(key);

                if (current != null) {

                    while (true) {
                        result.add(new VersionedValue(Externalization.internalizeObject(current.data), current.version));

                        if (current.version <= desiredVersion) {
                            return result;
                        }

                        if (current.previousVersion == -1) {
                            break;
                        }

                        current = cache.get(makeVersionedKey(key, current.previousVersion));
                    }
                }
                throw new PersistenceException("Version of vbox " + vbox.getId() + " not found for transaction number "
                        + desiredVersion);
            }
        });
    }

    // persist the number of the committed transaction. Maybe this should be made differently.
    // It may abort transactions because they try to change this same slot.
    private void persistCommittedTransactionNumber(final int txNumber) {
        // there might be some synchronization issues concerning maxCommittedTxId
        if (txNumber > this.maxCommittedTxId) {
            this.maxCommittedTxId = txNumber;

            doWithinBackingTransactionIfNeeded(new Callable<Void>() {
                @Override
                public Void call() {

                    getSystemCache().put(MAX_COMMITTED_TX_ID, new Integer(maxCommittedTxId));
                    return null;
                }
            });
        }
    }

    // only correct if invoked within a backing transaction.  Also this code
    // assumes a single global commit lock. Otherwise the counter might be set
    // backwards by a late-running thread.
    private void updatePersistentInstanceCounters() {
        Transaction current = FenixFramework.getTransaction();

        Set<DomainClassInfo> infos = current.getFromContext(KEY_INSTANTIATED_CLASSES);

        if (infos != null) {
            for (DomainClassInfo info : infos) {
                String key = makeKeyForMaxCounter(info);
                Integer max = (Integer) getSystemCache().get(key);

                int newCounterValue = info.getLastKey();

                if (max == null || max < newCounterValue) {
                    getSystemCache().put(key, newCounterValue);
                    logger.debug("Update persistent counter for class {}: {}", info.domainClassName, newCounterValue);
                }

            }

        }
    }

    private String makeKeyFor(VBox vbox) {
        return vbox.getId();
    }

    private String makeVersionedKey(String key, int version) {
        return key + ":" + version;
    }

    /* DataVersionHolder class. Ensures safe publication. */

    private static class DataVersionHolder implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public final int version;
        public final int previousVersion;
        public final byte[] data;

        // public java.io.Serializable data;

        DataVersionHolder(int version, int previousVersion, byte[] data) {
            this.version = version;
            this.previousVersion = previousVersion;
            this.data = data;
        }
    }

    /*
      Notes on the usage of Infinispan:

      One of the goals is to enable the usage of infinispan in READ_COMMITTED
      mode (thus no writeSkew checks, no versioning).  This is supposedly more
      efficient.  Moreover, this is  work already being performed by the STM.

      Part of the strategy to accomplish this is to ensure that the cache is
      "write-only".  If this were true, then we could say that regardless of
      what is seen in other STM transactions, it never changed, which would make
      correctness proof attempts easier.

      However, that is not the case.  Still we argue that whenever the value of
      some key changes, the system as a whole behaves as expected. Here are some
      things to consider:

      - Keys are never removed.  At most their corresponding value is updated.

      - The domain cache contains the domain entities. When committing (to
      persistence), a given VBox, we store its most recent value in a key built
      from the slotName + ownerOid.  Thus, this key's value will change over
      time.  But we ensure that if a newer value is seen here the previous values
      already exist somewhere else (in another key).  The previous values in
      history are written each in their own key built from slotName + ownerOid
      + #version.  Everything else in the domain cache is write-only.

      - The system cache may be updated.  I'll look further into this.  For now,
      these updates are performed within a global (cluster-wide) commit lock,
      so no two updates will ever be attempted on the same key.  Thus, at most,
      within a transaction two reads from the system cache may seen values that
      have been written by another meanwhile committing/committed transaction.
      We need to ensure that whatever is seen is not problematic from the point
      of view of the running transaction.  For now, this can be:

      1. initialization marker and updates to the domain class infos.  This
      should only occur at startup time and is work performed by a single node.
      It occurs  before the system is actually up and running, so it's not a
      problem.

      2. updates to the highest committed tx number: should occurs within the
      global commit
      
      3. updates to the highest oid per class: I need to revise this. Currently,
      it's broken.  The idea is that it should only be updated at commit time.
      However, I need to consider the following aspects: handle a different
      counter per node? where is the serverOid part? within the same node if a
      transaction is trying to create a new object of a given class, I know that
      it's in-memory domain class info will be updated, so I should make sure
      that only on attempt is made to grab it for ispn's cache. I.e. when updating
      the highest number per class, by definition, such number is already updated
      in memory.

     */
}
