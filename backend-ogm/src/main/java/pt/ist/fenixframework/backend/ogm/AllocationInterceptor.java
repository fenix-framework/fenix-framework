package pt.ist.fenixframework.backend.ogm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;

import java.io.Serializable;

import pt.ist.fenixframework.core.DomainObjectAllocator;

public class AllocationInterceptor extends EmptyInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AllocationInterceptor.class);

    // @Override
    // public Boolean isTransient(Object entity) {
    //     logger.trace("isTransient(" + entity.getClass().getName() + ")?");
    //     try {
    //         OgmDomainObject obj = (OgmDomainObject)entity;
    //         logger.trace("isTransient(" + entity.getClass().getName() + ")?: " + (obj.getHibernate$primaryKey() == null));
    //         return obj.getHibernate$primaryKey() == null;
    //     } catch (ClassCastException ex) {
    //         // not a domain object
    //         logger.trace("isTransient(" + entity.getClass().getName() + ")?: UNKNOWN");
    //         return null;
    //     }
    // }

    @Override
    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        if (logger.isTraceEnabled()) {
            logger.trace("EntityName: " + entityName);
            logger.trace("EntityMode: " + entityMode);
            logger.trace("        Id: " + id);
        }

        try {
            Class clazz = Class.forName(entityName);
            OgmOID oid = new OgmOID(clazz, (String)id);
            return DomainObjectAllocator.allocateObject(clazz, oid);
        } catch (ClassNotFoundException ex) {
            // Should not occur.
            if (logger.isErrorEnabled()) {
                logger.error("ClassNotFoundException for " + entityName);
            }
            // This will probably break ahead, but we can just let hibernate try its luck
            return null;
        }
    }

}
