/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.OID;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.IdentityMap;
import pt.ist.fenixframework.core.SharedIdentityMap;

public abstract class JVSTMDomainObject extends AbstractDomainObjectAdapter {
    private static final Logger logger = LoggerFactory.getLogger(JVSTMDomainObject.class);

    // this should be final, but the ensureOid and restoreOid methods prevent it
    private OID oid;

    // We need to have the default constructor, because we've added the allocate-instance constructor
    protected JVSTMDomainObject() {
        super(); // top-level constructor will invoke ensureOid()
    }

    protected JVSTMDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = (OID) oid.oid;
    }

    @Override
    protected void ensureOid() {
        try {
            Class objClass = this.getClass();
            IdentityMap idMap = SharedIdentityMap.getCache();

            while (true) {
                // assign new OID
                this.oid = OID.makeNew(objClass);
                // cache this instance
                Object shouldBeSame = idMap.cache(this);
                if (shouldBeSame == this) {
                    return;
                } else {
                    logger.warn("Another object was already cached with the same key as this new object: " + oid);
                }
            }
        } catch (Exception e) {
            throw new UnableToDetermineIdException(e);
        }

    }

    // dealing with domain object identifiers

    @Override
    public OID getOid() {
        return this.oid;
    }

    @Override
    public final String getExternalId() {
        return oid.toExternalId();
    }

}
