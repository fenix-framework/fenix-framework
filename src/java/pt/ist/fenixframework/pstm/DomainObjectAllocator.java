package pt.ist.fenixframework.pstm;

import java.lang.reflect.Constructor;

public class DomainObjectAllocator {

    public static AbstractDomainObject allocateObject(long oid) {
        Class objClass = DomainClassInfo.mapIdToClass((int)(oid >> 32));

        if (objClass == null) {
            throw new MissingObjectException();
        }

        try {
            // the allocate-only constructor is the constructor 
            // with a single argument of the static inner class OID below
            Constructor constructor = objClass.getDeclaredConstructor(OID.class);
            return (AbstractDomainObject)constructor.newInstance(new OID(oid));
        } catch (NoSuchMethodException nsme) {
            throw new Error("Could not allocate a domain object because the necessary constructor is missing", nsme);
        } catch (InstantiationException ie) {
            System.out.println("++++++ Found an InstantiationException that prevented the allocation of an object of class " + objClass);
            ie.printStackTrace();
            throw new Error("Could not allocate a domain object because the allocation constructor failed", ie);
        } catch (Exception exc) {
            System.out.println("++++++ Found an Exception that prevented the allocation of an object of class " + objClass);
            exc.printStackTrace();
            throw new Error("Could not allocate a domain object because of an exception", exc);
        }
    }

    public static class OID {
        public final long oid;

        private OID(long oid) {
            this.oid = oid;
        }
    }
}
