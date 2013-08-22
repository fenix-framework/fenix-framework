/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.exception.MissingObjectException;
import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

/**
 * This class provides an internal representation of a DomainObject's identifier using a UUID.
 */
public class OID implements Comparable<OID>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OID.class);

    private static final String OID_SEPARATOR = "@";
    private static final String EXTERNAL_ID_ERROR = "Could not process externalId: ";

    public static final String ROOT_PK = "ROOT_OBJECT";
    public static final OID ROOT_OBJECT_ID;

    private static final ConcurrentMap<String, Class<?>> CACHED_CLASSES;

    static {
        CACHED_CLASSES = new ConcurrentHashMap<String, Class<?>>();
        ROOT_OBJECT_ID = new OID(DomainRoot.class, DomainRoot.class.getName() + OID_SEPARATOR + ROOT_PK);
    }

    private final Class objClass;
    /* 
     * fullId format: <classname>@<UUID>@[hints] 
     * This works ok, because there are no @ in the first two fields. 
     */
    private final String fullId; // includes class name to avoid repetitive computation

    public static final boolean AUTOMATIC_LOCALITY_HINTS = Boolean.parseBoolean(System.getProperty("automaticLocalityHints", "false"));
    
    /**
     * Create a new Object IDentifier for the given class. For the special class {@link DomainRoot}, it will always return the
     * same ROOT_OBJECT_ID. Any {@link LocalityHints} for the {@link DomainRoot} object are ignored, as this object always
     * exists.
     *
     * @param objClass The Class of the {@link DomainObject} for which to create a new OID.
     * @param hints The {@link LocalityHints} if any.
     * @return A unique identifier for an object of the given type.
     */
    public static OID makeNew(Class objClass, LocalityHints hints) {
        if (objClass.equals(DomainRoot.class)) {
            if (logger.isTraceEnabled()) {
                logger.trace("Returning well-known fixed OID for singleton DomainRoot instance: " + ROOT_OBJECT_ID);
            }
            return ROOT_OBJECT_ID;
        } else {
            String uuid = UUID.randomUUID().toString();
            if (hints == null) {
            	if (AUTOMATIC_LOCALITY_HINTS) {
            		hints = new LocalityHints(new String[]{Constants.GROUP_ID, objClass.getName()});
            	} else {
            		hints = new LocalityHints(new String[]{Constants.GROUP_ID, uuid});
            	}
            }
            OID oid = new OID(objClass, uuid, hints);
            if (logger.isTraceEnabled()) {
                logger.trace("Making new oid: " + oid.toString());
            }
            return oid;
        }
    }

    /**
     * Calls {@link #makeNew(Class, LocalityHints)} with <code>null</code> {@link LocalityHints}.
     *
     * @see #makeNew(Class, LocalityHints)
     */
    public static OID makeNew(Class objClass) {
        return makeNew(objClass, null);
    }

    private OID(Class objClass, String objId, LocalityHints hints) {
        this(objClass, objClass.getName() + OID_SEPARATOR + objId + OID_SEPARATOR + (hints == null ? "" : hints.hints2String()));
    }

    private OID(Class objClass, String fullId) {
        CACHED_CLASSES.putIfAbsent(objClass.getName(), objClass);
        this.objClass = objClass;
        this.fullId = fullId;
    }

    /**
     * Creates an OID from the given external representation. If the external representation as been tampered with, the results of
     * this method are undefined.
     *
     * @param externalId The external representation of the object's identifier.
     * @return the OID that corresponds to the given external representation
     */
    public static OID fromExternalId(String externalId) {
        if (logger.isTraceEnabled()) {
            logger.trace("Building OID from externalId: " + externalId);
        }

        try {
            String className = extractClassNameFromExtenalId(externalId);
            Class objClass = CACHED_CLASSES.get(className);
            if (objClass != null) {
                return new OID(objClass, externalId);
            } else {
                return new OID(Thread.currentThread().getContextClassLoader().loadClass(className), externalId);
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(EXTERNAL_ID_ERROR + externalId);
            }
            throw new MissingObjectException(EXTERNAL_ID_ERROR + externalId, e);
        }
    }

    public static OID recoverFromFullId(String fullId) {
        if (logger.isTraceEnabled()) {
            logger.trace("Building OID from fullId: " + fullId);
        }
        // currently the same as an externalId
        return fromExternalId(fullId);
    }

    private static String extractClassNameFromExtenalId(String externalId) {
        return externalId.substring(0, externalId.indexOf(OID_SEPARATOR));
    }

    public Class getObjClass() {
        return this.objClass;
    }

    public String getFullId() {
        return this.fullId;
    }

    public String toExternalId() {
        return fullId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OID) {
            OID other = (OID) o;
            // return (this.objClass.equals(other.objClass)
            //         && this.fullId.equals(other.fullId));
            return (this.fullId.equals(other.fullId));
        }
        return false;
    }

    @Override
    public int hashCode() {
        // return objClass.hashCode() + fullId.hashCode();
        return fullId.hashCode();
    }

    @Override
    public int compareTo(OID other) {
        // int classes = this.objClass.getName()compareTo(other.objClass.getName());
        // if (classes != 0) {  // having different classes is enough to order them
        //     return classes;
        // }
        // return this.objId.compareTo(other.objId);

        // now that we have the fullId it's simpler
        return this.fullId.compareTo(other.fullId);
    }

    @Override
    public String toString() {
        return toExternalId();
    }

    /**
     * Get the LocalityHints of this OID.
     *
     * @return The {@link LocalityHints} instance or <code>null</code> is this OID does not have {@link LocalityHints}
     */
    public LocalityHints getLocalityHints() {
        return getLocalityHint(fullId);
    }

    public static LocalityHints getLocalityHint(String fullId) {
        if (fullId.startsWith(ROOT_OBJECT_ID.fullId)) {
            return new LocalityHints();
        }
        int firstSep = fullId.indexOf(OID_SEPARATOR);
        if (firstSep == -1) {
            return new LocalityHints();
        }
        int secondSep = fullId.indexOf(OID_SEPARATOR, firstSep + 1);
        if (secondSep == -1) {
            return new LocalityHints();
        }
        String localityHintsStr = fullId.substring(secondSep + 1);

        return (localityHintsStr.isEmpty() ? new LocalityHints() : LocalityHints.string2Hints(localityHintsStr));
    }
}
