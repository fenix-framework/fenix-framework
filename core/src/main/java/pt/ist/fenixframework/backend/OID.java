/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.exception.MissingObjectException;

/**
 *  This class provides an internal representation of a DomainObject's identifier using a UUID.
 */
public class OID implements Comparable<OID>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OID.class);

    private static final String OID_SEPARATOR = "@";
    private static final String EXTERNAL_ID_ERROR = "Could not process externalId: ";

    public static final String ROOT_PK = "ROOT_OBJECT";
    public static final OID ROOT_OBJECT_ID = new OID(DomainRoot.class, ROOT_PK);

    private final Class objClass;
    private final String fullId; // includes class name to avoid repetitive computation

    /**
     * Create a new Object IDentifier for the given class.  For the special class {@link DomainRoot}, it will always return the
     * same ROOT_OBJECT_ID
     */
    public static OID makeNew(Class objClass) {
        if (objClass.equals(DomainRoot.class)) {
            logger.debug("Returning well-known fixed OID for singleton DomainRoot instance: " + ROOT_OBJECT_ID);
            return ROOT_OBJECT_ID;
        } else {
            return new OID(objClass);
        }
    }
    
    private OID(Class objClass) {
        this.objClass = objClass;
        this.fullId = objClass.getName() + OID_SEPARATOR + UUID.randomUUID().toString();
    }

    public OID(Class objClass, String objId) {
        this.objClass = objClass;
        this.fullId = objClass.getName() + OID_SEPARATOR + objId;
    }

    public OID(String externalId) {
        String[] tokens = externalId.split(OID_SEPARATOR);
        try {
            this.objClass = Class.forName(tokens[0]);
            this.fullId = externalId; // tokens[0] + OID_SEPARATOR + tokens[1];
        } catch (Exception e) {
            // e.g. index out of bounds, class not found, etc.
            logger.error(EXTERNAL_ID_ERROR + externalId);
            throw new MissingObjectException(EXTERNAL_ID_ERROR + externalId, e);
        }
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
            OID other = (OID)o;
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
}
