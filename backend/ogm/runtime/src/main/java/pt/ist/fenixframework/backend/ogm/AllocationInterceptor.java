package pt.ist.fenixframework.backend.ogm;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.core.DomainObjectAllocator;

public class AllocationInterceptor extends EmptyInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(AllocationInterceptor.class);

    private final DomainObjectAllocator allocator = new DomainObjectAllocator(OgmDomainObject.class);

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
        logger.trace("EntityName: " + entityName);
        logger.trace("EntityMode: " + entityMode);
        logger.trace("        Id: " + id);

        try {
            Class clazz = Class.forName(entityName);
            OgmOID oid = new OgmOID(clazz, (String) id);
            return allocator.allocateObject(clazz, oid);
        } catch (ClassNotFoundException ex) {
            // Should not occur.
            logger.error("ClassNotFoundException for " + entityName);
            // This will probably break ahead, but we can just let hibernate try its luck
            return null;
        }
    }

}
