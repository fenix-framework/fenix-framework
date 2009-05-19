package pt.ist.fenixframework.pstm.ojb;

import java.lang.reflect.Method;

import org.apache.ojb.broker.metadata.MetadataException;

public class WriteOnlyPersistentField extends FenixPersistentField {
    private final Method setterMethod;

    public WriteOnlyPersistentField() {
        super();
        this.setterMethod = null;
    }

    public WriteOnlyPersistentField(Class declaringClass, String propName) {
        super(declaringClass, propName);
        this.setterMethod = findSetter(declaringClass, "set$" + propName);
        this.setterMethod.setAccessible(true);
    }

    public Class getType() {
        // we are sure that the setter method has one parameter
        return setterMethod.getParameterTypes()[0];
    }

    public void set(Object obj, Object value) throws MetadataException {
        if (obj == null) {
            // is this really needed?
            return;
        }

        try {
            setterMethod.invoke(obj, value);
        } catch (Throwable e) {
            throw new MetadataException("Error invoking method:" + setterMethod.getName() 
                                        + " in object " + obj.getClass().getName(), 
                                        e);
        }
    }

    public Object get(Object anObject) throws MetadataException {
        throw new Error("The get of a WriteOnlyPersistentField should never be called");
    }

    protected Method findSetter(Class declaringClass, String name) {
        try {
            Class currentClass = declaringClass;
            while (currentClass != null) {
                for (Method m : currentClass.getDeclaredMethods()) {
                    if (m.getName().equals(name) && (m.getParameterTypes().length == 1)) {
                        return m;
                    }
                }
                currentClass = currentClass.getSuperclass();
            }
        } catch (Exception e) {
            throw new MetadataException("Can't find method " + name + " in " + declaringClass.getName(), e);
        }

        throw new MetadataException("Can't find method " + name + " in " + declaringClass.getName());
    }
}
