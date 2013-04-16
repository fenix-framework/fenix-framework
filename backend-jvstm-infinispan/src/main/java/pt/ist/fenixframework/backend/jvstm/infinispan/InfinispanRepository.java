/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.infinispan;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.transaction.TransactionManager;

import jvstm.Transaction;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.backend.jvstm.pstm.VersionedValue;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;
import pt.ist.fenixframework.backend.jvstm.repository.Repository;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.Externalization;

/**
 * This class implements the Repository interface using the Infinispan NoSQL key/value data store.
 */
public class InfinispanRepository extends Repository {

    private static final Logger logger = LoggerFactory.getLogger(InfinispanRepository.class);

    DefaultCacheManager cacheManager;

    // the name of the key used to store the DomainClassInfo instances.
    private final String DOMAIN_CLASS_INFO = "DomainClassInfo";

    private static final String SINGLE_BOX_SLOT_NAME = "$";
    private static final String MAX_COMMITTED_TX_ID = "maxTxId";

    // the name of the cache used to store system information
    static final String SYSTEM_CACHE_NAME = "SystemCache";
    // the name of the cache used to store all instances of all domain classes
    static final String DOMAIN_CACHE_NAME = "DomainCache";

    // this is a marker, so that when bootstrapping the repository, we can identify whether it already exists 
    private static final String CACHE_IS_FULLY_INITIALZED = "CacheAlreadExists";

    Cache<String, Object> systemCache;
    Cache<String, DataVersionHolder> domainCache;

    private int maxCommittedTxId = -1;

    // creates the manager of caches for Infinispan
    private void createCacheContainer(String ispnConfigFile) {
        try {
            if (ispnConfigFile == null || ispnConfigFile.isEmpty()) {
                logger.info("Initializing CacheManager with defaults", ispnConfigFile);
                this.cacheManager = new DefaultCacheManager();  // smf: make ispnConfigFile optional???
            } else {
                logger.info("Initializing CacheManager with default configuration provided in {}", ispnConfigFile);
                this.cacheManager = new DefaultCacheManager(ispnConfigFile);
            }
        } catch (java.io.IOException e) {
            logger.error("Error creating cache manager with configuration file: {} -> {}", ispnConfigFile, e);
            throw new PersistenceException(e);
        }
    }

    private boolean bootstrapIfNeeded(String ispnConfigFile) {
        createSystemCache();
        createDomainCache();
        if (this.systemCache.get(CACHE_IS_FULLY_INITIALZED) == null) {
            logger.info("Initialization marker not present. SystemCache is being initialized for the first time.");
            return true; // repository is new
        } else {
            logger.info("Initialization marker is present. SystemCache already existed.");
            return false;  // repository is not new
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

        // enforce required configuration

        // use REPEATABLE_READ
        confBuilder.locking().isolationLevel(IsolationLevel.REPEATABLE_READ).concurrencyLevel(32).writeSkewCheck(true)
                .useLockStriping(false).lockAcquisitionTimeout(10000);
        // detect DEALOCKS (is this needed?)
        confBuilder.deadlockDetection().enable();
        // transactional optimistic cache
        confBuilder.transaction().transactionMode(TransactionMode.TRANSACTIONAL).syncRollbackPhase(false).cacheStopTimeout(30000)
                .useSynchronization(true).syncCommitPhase(false).lockingMode(LockingMode.OPTIMISTIC)
                .use1PcForAutoCommitTransactions(false).autoCommit(false);
        // use versioning
        confBuilder.versioning().enable().scheme(VersioningScheme.SIMPLE);
        // TODO: enable eviction when using a cache store (data persistence) 
        confBuilder.eviction().strategy(EvictionStrategy.NONE);
        confBuilder.eviction().maxEntries(-1);
        // disable expiration
        confBuilder.expiration().wakeUpInterval(-1);

        Configuration conf = confBuilder.build();

        return conf;
    }

    private void createSystemCache() {
        Configuration conf = makeRequiredConfiguration();

        logger.debug("Configuration for {} is: {}", SYSTEM_CACHE_NAME, conf.toString());

        this.cacheManager.defineConfiguration(SYSTEM_CACHE_NAME, conf);
        this.systemCache = this.cacheManager.getCache(SYSTEM_CACHE_NAME);
    }

    private void createDomainCache() {
        Configuration conf = makeRequiredConfiguration();

        logger.debug("Configuration for {} is: {}", DOMAIN_CACHE_NAME, conf.toString());

        this.cacheManager.defineConfiguration(DOMAIN_CACHE_NAME, conf);
        this.domainCache = this.cacheManager.getCache(DOMAIN_CACHE_NAME);
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
        // either cache uses the same manager instance, so we just pick one cache to get the manager from
        return getDomainCache().getAdvancedCache().getTransactionManager();
    }

    /* implementation of the Repository interface */

    @Override
    public boolean init(JVSTMConfig jvstmConfig) {
        String ispnConfigFile = ((JvstmIspnConfig) jvstmConfig).getIspnConfigFile();

        createCacheContainer(ispnConfigFile);
        return bootstrapIfNeeded(ispnConfigFile);
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
    public long getMaxOidForClass(Class<? extends AbstractDomainObject> domainClass, long lowerLimitOid, final long upperLimitOid) {
        final String key = domainClass.getName() + (upperLimitOid >> 48); // this is the serverOID

        return doWithinBackingTransactionIfNeeded(new Callable<Long>() {
            @Override
            public Long call() {
                Long max = (Long) getSystemCache().get(key);

                if (max == null) {
                    return 0L;
                }

                return max;
            }
        });

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
                persistCommittedTransactionNumber(txNumber);

                for (Entry<jvstm.VBox, Object> entry : changes) {
                    VBox vbox = (VBox) entry.getKey();
                    DomainObject owner = vbox.getOwnerObject();
                    Object newValue = entry.getValue();

                    newValue = (newValue == nullObject) ? null : newValue;

                    String key = getKey(vbox.getSlotName(), owner);
                    DataVersionHolder current = cache.get(key);
                    DataVersionHolder newVersion = new DataVersionHolder();

                    if (current != null) {
                        cache.put(key + current.version, current); // TODO: colocar aqui um timeout
                        newVersion.previousVersion = current.version;
                    } else {
                        newVersion.previousVersion = -1;
                    }

                    newVersion.version = txNumber;
                    newVersion.data = Externalization.externalizeObject(newValue);
                    // newVersion.data = (java.io.Serializable)newValue;
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
        this.cacheManager.stop();
        this.cacheManager = null;
        maxCommittedTxId = -1;
    }

    // called when a new domain object is created inside a transaction and a new oid is assigned to the
    // created object.
    @Override
    public void createdNewOidFor(final long newOid, long serverOidBase, DomainClassInfo instantiatedClass) {
        final String key = instantiatedClass.domainClassName + (serverOidBase >> 48);

        synchronized (this) {

            doWithinBackingTransactionIfNeeded(new Callable<Void>() {
                @Override
                public Void call() {
                    Long max = (Long) getSystemCache().get(key);

                    if (max == null || max < newOid) {
                        getSystemCache().put(key, newOid);
                    }

                    return null;
                }
            });

        }
    }

    /* utility methods used by the implementation of the Repository interface methods */

    private void reloadAttribute(VBox box) {
        int txNumber = Transaction.current().getNumber();

        List<VersionedValue> vvalues = getMostRecentVersions(box.getOwnerObject(), box.getSlotName(), txNumber);
        box.mergeVersions(vvalues);
    }

    List<VersionedValue> getMostRecentVersions(final DomainObject owner, final String slotName, final int desiredVersion) {
        final Cache<String, DataVersionHolder> cache = getDomainCache();
        final String key = getKey(slotName, owner);

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

                        current = cache.get(key + current.previousVersion);
                    }
                }
                throw new PersistenceException("Version of domain object " + owner.getExternalId()
                        + " not found for transaction number " + desiredVersion);
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

    // computes and returns the key of a slotName of a domain object
    private String getKey(String slotName, DomainObject domainObject) {
        return convertSlotName(slotName) + ((AbstractDomainObject) domainObject).getOid();
    }

    private final String convertSlotName(String slotName) {
        if (slotName.equals("obj$state")) {
            return SINGLE_BOX_SLOT_NAME;
        } else {
            return slotName;
        }
    }

    /* DataVersionHolder class */

    private static class DataVersionHolder implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public int version;
        public int previousVersion;
        public byte[] data;
        // public java.io.Serializable data;
    }

}
