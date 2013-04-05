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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import jvstm.Transaction;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.manager.DefaultCacheManager;
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

    CacheContainer cacheContainer;

    // the name of the key used to store the DomainClassInfo instances.
    private final String DOMAIN_CLASS_INFO = "DomainClassInfo";

    private static final String SINGLE_BOX_SLOT_NAME = "$";
    private static final String MAX_COMMITTED_TX_ID = "maxTxId";

    // the name of the cache used to store system information
    static final String SYSTEM_CACHE_NAME = "SystemCache";
    // the name of the cache used to store all instances of all domain classes
    static final String DOMAIN_CLASSES_CACHE_NAME = "DomainCache";

    // this is a marker, so that when bootstrapping the repository, we can identify whether it already exists 
    private static final String CACHE_IS_FULLY_INITIALZED = "CacheAlreadExists";

    Cache<String, Object> systemCache;
    Cache<String, DataVersionHolder> domainCache;

    private int maxCommittedTxId = -1;

    // creates the manager of caches for Infinispan
    static CacheContainer createCacheContainer(String ispnConfigFile) {
        CacheContainer cacheContainer = null;

        try {
            if (ispnConfigFile == null || ispnConfigFile.isEmpty()) {
                cacheContainer = new DefaultCacheManager();  // smf: make ispnConfigFile optional???
            } else {
                cacheContainer = new DefaultCacheManager(ispnConfigFile);
            }
        } catch (java.io.IOException e) {
            logger.error("Error creating cache manager with configuration file: {} -> {}", ispnConfigFile, e);
            throw new PersistenceException(e);
        }

        return cacheContainer;
    }

    boolean bootstrapIfNeeded(String ispnConfigFile) {
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

    void createSystemCache() {
        this.systemCache = this.cacheContainer.getCache(DOMAIN_CLASSES_CACHE_NAME);
    }

    void createDomainCache() {
        this.domainCache = this.cacheContainer.getCache(DOMAIN_CLASSES_CACHE_NAME);
    }

    // some useful methods for accessing Infinispan

    // returns the single cache object that holds all domain objects
    private Cache<String, DataVersionHolder> getDomainCache() {
        if (this.domainCache == null) {
            this.domainCache = this.cacheContainer.getCache(DOMAIN_CLASSES_CACHE_NAME);
        }

        return this.domainCache;
    }

    // returns the single cache object that holds all system information
    private Cache<String, Object> getSystemCache() {
        if (this.systemCache == null) {
            this.systemCache = this.cacheContainer.getCache(SYSTEM_CACHE_NAME);
        }

        return this.systemCache;
    }

    // opens the persistent structure for managing persistent instances of the specified class.
    private Cache<String, DataVersionHolder> getCache(Class cl) {
        return getDomainCache();
    }

    // config has information necessary for opening the connection to the repository
//    public InfinispanRepository(Config config) {
//        this.cacheContainer = createCacheContainer(config.getDbAlias());
//    }
//
//    public InfinispanRepository(CacheContainer cache) {
//        this.cacheContainer = cache;
//    }

    protected InfinispanRepository() {
    }

    // creates a repository bootstrap instance
//    public RepositoryBootstrap createRepositoryBootstrap(Config config) {
//        return new InfinispanRepositoryBootstrap(this.cacheContainer, config);
//    }

    @Override
    public boolean init(JVSTMConfig jvstmConfig) {
        String ispnConfigFile = ((JvstmIspnConfig) jvstmConfig).getIspnConfigFile();

        this.cacheContainer = createCacheContainer(ispnConfigFile);
        return bootstrapIfNeeded(ispnConfigFile);
    }

    // get the stored information concerning the DomainClassInfo
    @Override
    public final DomainClassInfo[] getDomainClassInfos() {
        DomainClassInfo infos[] = (DomainClassInfo[]) getSystemCache().get(DOMAIN_CLASS_INFO);

        if (infos == null) {
            return new DomainClassInfo[0];
        }

        return infos;
    }

    // implementation of the Repository interface

    // called when a new domain object is created inside a transaction and a new oid is assigned to the
    // created object.
    @Override
    public void createdNewOidFor(long newOid, long serverOidBase, DomainClassInfo instanceatedClass) {
        String key = instanceatedClass.domainClassName + (serverOidBase >> 48);

        synchronized (this) {
            Long max = (Long) getSystemCache().get(key);

            if (max == null || max < newOid) {
                getSystemCache().put(key, newOid);
            }
        }
    }

    // update the stored information concerning the DomainClassInfo adding the vector domainClassInfos
    @Override
    public void storeDomainClassInfos(final DomainClassInfo[] newDomainClassInfos) {
        if (newDomainClassInfos == null || newDomainClassInfos.length == 0) {
            return;
        }

        int i, j;
        DomainClassInfo[] all, stored = getDomainClassInfos();
        all = new DomainClassInfo[stored.length + newDomainClassInfos.length];
        for (j = 0; j < stored.length; j++) {
            all[j] = stored[j];
        }

        for (i = 0; i < newDomainClassInfos.length; i++, j++) {
            all[j] = newDomainClassInfos[i];
        }

        getSystemCache().put(DOMAIN_CLASS_INFO, all);
    }

    @Override
    public long getMaxOidForClass(Class<? extends AbstractDomainObject> domainClass, final long lowerLimitOid,
            final long upperLimitOid) {
        String key = domainClass.getName() + (upperLimitOid >> 48); // this is the serverOID

        Long max = (Long) getSystemCache().get(key);

        if (max == null) {
            return 0;
        }

        return max;
    }

    private void reloadAttribute(VBox box) {
        int txNumber = Transaction.current().getNumber();

        List<VersionedValue> vvalues = getMostRecentVersions(box.getOwnerObject(), box.getSlotName(), txNumber);
        box.mergeVersions(vvalues);
    }

    private final String convertSlotName(String slotName) {
        if (slotName.equals("obj$state")) {
            return SINGLE_BOX_SLOT_NAME;
        } else {
            return slotName;
        }
    }

    List<VersionedValue> getMostRecentVersions(DomainObject owner, String slotName, int desiredVersion) {
        Cache<String, DataVersionHolder> cache = getCache(owner.getClass());
        String key = getKey(slotName, owner);
        DataVersionHolder current;
        ArrayList<VersionedValue> result = new ArrayList<VersionedValue>();

        current = cache.get(key);

        if (current != null) {

            while (true) {
                // result.add(new VersionedValue(current.data,
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
        throw new PersistenceException("Version of domain object " + owner.getExternalId() + " not found for transaction number "
                + desiredVersion);

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

    // persist the number of the committed transaction. Maybe this should be made differently.
    // It may abort transactions because they try to change this same slot.
    private void persistCommittedTransactionNumber(int txNumber) {
        // there might be some synchronization issues concernning maxCommittedTxId
        if (txNumber > this.maxCommittedTxId) {
            this.maxCommittedTxId = txNumber;
            getSystemCache().put(MAX_COMMITTED_TX_ID, new Integer(maxCommittedTxId));
        }
    }

    private static class DataVersionHolder implements java.io.Serializable {
        public int version;
        public int previousVersion;
        public byte[] data;
        // public java.io.Serializable data;
    }

    private TransactionManager getTransactionManager() {
        TransactionManager tm = getDomainCache().getAdvancedCache().getTransactionManager();

        //System.out.println("@@@@@@@@@@@@@@ TM = " + tm.hashCode());
        return tm;
    }

    // computes and returns the key of a slotName of a domain object
    private String getKey(String slotName, DomainObject domainObject) {
        return convertSlotName(slotName) + ((AbstractDomainObject) domainObject).getOid();
    }

    // stores persistently a set of changes
    // the third arguments represents the reference used by the stm to represent null objects.
    @Override
    public void persistChanges(Set<Entry<jvstm.VBox, Object>> changes, int txNumber, Object nullObject) {
        try {
            boolean inTopLevelTransaction = false;
            TransactionManager tm = getTransactionManager();

            // the purpose of this test is to enable reuse of 
            if (tm.getTransaction() == null) {
                tm.begin();
                inTopLevelTransaction = true;
            }

            persistCommittedTransactionNumber(txNumber);

            for (Entry<jvstm.VBox, Object> entry : changes) {
                VBox vbox = (VBox) entry.getKey();
                DomainObject owner = vbox.getOwnerObject();
                Object newValue = entry.getValue();

                newValue = (newValue == nullObject) ? null : newValue;

                Cache<String, DataVersionHolder> cache = getCache(owner.getClass());
                String key = getKey(vbox.getSlotName(), owner);
                DataVersionHolder current = cache.get(key);
                DataVersionHolder newVersion = new DataVersionHolder();

                if (current != null) {
                    cache.put(key + current.version, current); // colocar aqui um timeout
                    newVersion.previousVersion = current.version;
                } else {
                    newVersion.previousVersion = -1;
                }

                newVersion.version = txNumber;
                newVersion.data = Externalization.externalizeObject(newValue);
                // newVersion.data = (java.io.Serializable)newValue;
                cache.put(key, newVersion); // colocar aqui um timeout
            }

            if (inTopLevelTransaction) {
                tm.commit();
            }
        } catch (NotSupportedException nse) {
            throw new PersistenceException(nse);
        } catch (SystemException se) {
            throw new PersistenceException(se);
        } catch (RollbackException re) {
            throw new PersistenceException(re);
        } catch (HeuristicMixedException hme) {
            throw new PersistenceException(hme);
        } catch (HeuristicRollbackException hre) {
            throw new PersistenceException(hre);
        }
    }

    // returns the greatest committed transaction number. This implementation 
    // assumes a single JVSTM.
    @Override
    public int getMaxCommittedTxNumber() {
        if (maxCommittedTxId == -1) {
            Integer max = (Integer) this.getSystemCache().get(MAX_COMMITTED_TX_ID);

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
        this.cacheContainer.stop();
        this.cacheContainer = null;
        maxCommittedTxId = -1;
    }

}
