package pt.ist.fenixframework.pstm.ojb;

import java.lang.reflect.Constructor;
import org.apache.ojb.odmg.OJB;

public class DomainAllocator {

    public static Object allocate() {
        throw new Error("DomainAllocator: Calling the allocate() method is an error.");
    }

    public static Object allocateObject(Class objClass) {
        try {
            // the allocate-only constructor is the constructor 
            // with a single argument of the class OJB
            // the actual argument may (should?) be null
            Constructor constructor = objClass.getDeclaredConstructor(OJB.class);
            return constructor.newInstance((OJB) null);
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
}
