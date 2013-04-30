/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.repository;

import java.util.Map.Entry;
import java.util.Set;

import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;
import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

//import pt.ist.fenixframework.pstm.DomainClassInfo;
//import pt.ist.fenixframework.pstm.PersistentTransaction;
//import pt.ist.fenixframework.Config;
//import pt.ist.fenixframework.pstm.VBox;

/**
 * This class abstracts the interface for storing and accessing persistent objects and concerned information.
 * 
 * <!-- It implements also a
 * interface for storing/retrieving key-value pairs from the underlying repository, which allows for storing generic data.-->
 */
public abstract class Repository {

//    // the repository instance used to persist the changes
//    private static Repository repository;

//    public Repository() {
//    }

//    public static Repository getRepository() {
//        return Repository.repository;
//    }
//
//    public static void setRepository(Repository rep) {
//        Repository.repository = rep;
//    }
//
    // creates a repository bootstrap instance
//    public abstract RepositoryBootstrap createRepositoryBootstrap(Config config);

    // initialize this repository instance given a configuration
    // returns whether the repository is new, so that we know we need to create the DomainRoot
    public abstract boolean init(JVSTMConfig jvstmConfig);

    // get the stored information concerning the DomainClassInfo
    public abstract DomainClassInfo[] getDomainClassInfos();

    // update the stored information concerning the DomainClassInfo adding the vector domainClassInfos
    public abstract void storeDomainClassInfos(final DomainClassInfo[] domainClassInfos);

    /**
     * Returns the counter stored for a given domain class.
     * 
     * @param domainClassInfo The information about the class for which the required counter refers.
     * @return The (non negative) counter value stored for the given domain class or a negative value if there is no stored
     *         information regarding this class's counter.
     */
    public abstract int getMaxCounterForClass(DomainClassInfo domainClassInfo);

    /**
     * Invoked by the framework whenever a new OID is generated (i.e. a new DomainObject is created inside a transaction).
     * Depending on the concrete repository, this may be needed for maintaining the maximum id per class (and maybe per server).
     * By default this method does nothing.
     * 
     * @param domainClassInfo Information about the instantiated class
     * @param newCounterValue The new value of the counter
     */
    public void updateMaxCounterForClass(DomainClassInfo domainClassInfo, int newCounterValue) {
    }

    // reloads a primitive value from the storage for the specified box
    public abstract void reloadPrimitiveAttribute(VBox box);

    // reloads a reference attribute from the storage for the specified box
    public abstract void reloadReferenceAttribute(VBox box);

    // stores persistently a set of changes
    // the third arguments represents the reference used by the stm to represent null objects.
    public abstract void persistChanges(Set<Entry<jvstm.VBox, Object>> changes, int txNumber, Object nullObject);

    /**
     * Return the greatest committed transaction number, persisted in the Repository
     */
    public abstract int getMaxCommittedTxNumber();

    /**
     * Close the connection to the repository.
     */
    public abstract void closeRepository();

//    /**
//     * Store the given value using the given key.
//     * 
//     * @param key The key that uniquely identifies the value being stored.
//     * @param value The value to store.
//     */
//    public abstract void storeKeyValue(Serializable key, Serializable value);
//
//    /**
//     * Retrieve the value associated with the given key.
//     * 
//     * @param key The key to lookup
//     * @return The value associated with the key.
//     */
//    public abstract Serializable getValue(Serializable key);
}
