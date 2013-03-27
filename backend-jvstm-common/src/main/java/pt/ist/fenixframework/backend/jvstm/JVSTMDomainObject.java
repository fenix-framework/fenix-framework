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

import pt.ist.fenixframework.backend.jvstm.pstm.DomainClassInfo;
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
                logger.debug("Another object was already cached with the same key as this new object: {}", oid);

            }
        } catch (Exception e) {
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

}
