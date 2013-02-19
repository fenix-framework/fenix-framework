/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.repository;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

//import pt.ist.fenixframework.pstm.DomainClassInfo;
//import pt.ist.fenixframework.pstm.PersistentTransaction;
//import pt.ist.fenixframework.Config;
//import pt.ist.fenixframework.pstm.VBox;

/**
 * This class abstracts the interface for storing and accessing persistent objects and concerned information. It implements also a
 * interface for storing/retrieving key-value pairs from the underlying repository, which allows for storing generic data.
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

    // get the stored information concerning the DomainClassInfo
//    public abstract DomainClassInfo[] getDomainClassInfos();

    // update the stored information concerning the DoaminCalssInfo adding the vector domainClassInfos
//    public abstract void storeDomainClassInfos(final DomainClassInfo[] domainClassInfos);

    // returns the maximum stored oid for class domainClass. upperLimitOID is the maximum value the oid
    // can have. it may be useful for optimize the search.
    public abstract long getMaxIdForClass(Class domainClass, final long upperLimitOID);

    // reloads a primitive value from the storage for the specified box
    public abstract void reloadPrimitiveAttribute(VBox box);

    // reloads a reference attribute from the storage for the specified box
    public abstract void reloadReferenceAttribute(VBox box);

    // stores persistently a set of changes
    // the third arguments represents the reference used by the stm to represent null objects.
    public abstract void persistChanges(Set<Entry<jvstm.VBox, Object>> changes, int txNumber, Object nullObject);

    // returns the greatest committed transaction number
    public abstract int getMaxCommittedTxNumber();

    // close the connection to the repository
    public abstract void closeRepository();

    // invoked by the fenixframework whenever a new oid is generated (i.e. a new
    // persistent object is created inside a transaction). This may be useful for
    // maintaining persistently the maximum id per class per server. By default this
    // method does nothing.
//    public void createdNewOidFor(long newOid, long serverOidBase, DomainClassInfo instanceatedClass) {
//    }

    /**
     * Store the given value using the given key.
     * 
     * @param key The key that uniquely identifies the value being stored.
     * @param value The value to store.
     */
    public abstract void storeKeyValue(Serializable key, Serializable value);

    /**
     * Retrieve the value associated with the given key.
     * 
     * @param key The key to lookup
     * @return The value associated with the key.
     */
    public abstract Serializable getValue(Serializable key);
}
