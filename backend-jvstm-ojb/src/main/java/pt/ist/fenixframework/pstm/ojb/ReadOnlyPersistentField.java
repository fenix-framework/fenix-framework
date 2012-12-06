package pt.ist.fenixframework.pstm.ojb;

import java.lang.reflect.Method;

import org.apache.ojb.broker.metadata.MetadataException;

public class ReadOnlyPersistentField extends FenixPersistentField {
    private final Method getterMethod;

    public ReadOnlyPersistentField() {
        super();
        this.getterMethod = null;
    }

    public ReadOnlyPersistentField(Class declaringClass, String propName) {
        super(declaringClass, propName);
        this.getterMethod = findGetter(declaringClass, "get$" + propName);
        this.getterMethod.setAccessible(true);
    }

    public Class getType() {
        return getterMethod.getReturnType();
    }

    public void set(Object obj, Object value) throws MetadataException {
        throw new Error("The set of a ReadOnlyPersistentField should never be called");
    }

    public Object get(Object anObject) throws MetadataException {
        if (anObject == null) {
            // is this really needed?
            return null;
        }

        try {
            return getterMethod.invoke(anObject);
        } catch (Throwable e) {
            throw new MetadataException("Error invoking method:" + getterMethod.getName() 
                                        + " in object " + anObject.getClass().getName(), 
                                        e);
        }
    }
}
