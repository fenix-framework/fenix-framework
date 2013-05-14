/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstm.pstm.VersionedSubject;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public abstract class JVSTMDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(JVSTMDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private long oid;

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected JVSTMDomainObject() {
        super(); // top-level constructor will invoke ensureOid()
    }

    protected JVSTMDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (Long) oid.oid;
    }

    @Override
    protected void ensureOid() {
        try {
            // find successive ids until one is available
            while (true) {
                this.oid = DomainClassInfo.getNextOidFor(this.getClass());
                Object cached = SharedIdentityMap.getCache().cache(this);
                if (cached == this) {
                    // break the loop once we got this instance cached
                    return;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Another object was already cached with the same key as this new object: {}",
                            Long.toHexString(oid));
                }

            }
        } catch (Exception e) {
            logger.debug("Exception in ensureOid:", e);
            throw new UnableToDetermineIdException(e);
        }
    }

    // dealing with domain object identifiers

    @Override
    public Long getOid() {
        return this.oid;
    }

    @Override
    public final String getExternalId() {
        return Long.toHexString(this.oid);
//        return String.valueOf(this.oid);
    }

    public jvstm.VBoxBody addNewVersion(String attrName, int txNumber) {
        VersionedSubject vs = getSlotNamed(attrName);
        if (vs != null) {
            return vs.addNewVersion(txNumber);
        }

        logger.warn("!!! WARNING !!!: addNewVersion couldn't find the appropriate slot");
        return null;
    }

    VersionedSubject getSlotNamed(String attrName) {
        Class myClass = this.getClass();
        while (myClass != Object.class) {
            try {
                Field f = myClass.getDeclaredField(attrName);
                f.setAccessible(true);
                return (VersionedSubject) f.get(this);
            } catch (NoSuchFieldException nsfe) {
                myClass = myClass.getSuperclass();
            } catch (IllegalAccessException iae) {
                throw new Error("Couldn't find attribute " + attrName + ": " + iae);
            } catch (SecurityException se) {
                throw new Error("Couldn't find attribute " + attrName + ": " + se);
            }
        }
        logger.warn("Couldn't find attribute {}", attrName);
        return null;
    }

}
