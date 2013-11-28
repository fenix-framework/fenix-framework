package pt.ist.fenixframework.core;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainObjectAllocator {

    private static final Logger logger = LoggerFactory.getLogger(DomainObjectAllocator.class);

    public static AbstractDomainObject allocateObject(Class objClass, Object oid) {
        if (objClass == null) {
            throw new RuntimeException("Cannot allocate object '" + oid + "'. Class not found");
        }

        try {
            // the allocate-only constructor is the constructor 
            // with a single argument with the type of the static inner class OID below
            Constructor constructor = objClass.getDeclaredConstructor(OID.class);
            return (AbstractDomainObject) constructor.newInstance(new OID(oid));
        } catch (NoSuchMethodException nsme) {
            throw new Error("Could not allocate a domain object because the necessary constructor is missing", nsme);
        } catch (InstantiationException ie) {
            logger.error("Found an InstantiationException that prevented the allocation of an object of class " + objClass,
                    ie.getCause());
            throw new Error("Could not allocate a domain object because the allocation constructor failed", ie);
        } catch (Exception exc) {
            logger.error("Found an Exception that prevented the allocation of an object of class " + objClass, exc);
            throw new Error("Could not allocate a domain object because of an exception", exc);
        }
    }

    public static class OID {
        public final Object oid;

        private OID(Object oid) {
            this.oid = oid;
        }
    }
}
