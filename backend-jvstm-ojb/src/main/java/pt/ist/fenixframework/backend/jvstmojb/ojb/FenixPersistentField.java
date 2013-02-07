package pt.ist.fenixframework.backend.jvstmojb.ojb;

import java.lang.reflect.Method;

import org.apache.ojb.broker.metadata.MetadataException;
import org.apache.ojb.broker.metadata.fieldaccess.PersistentField;

public abstract class FenixPersistentField implements PersistentField {
    private final Class declaringClass;
    private final String propName;

    public FenixPersistentField() {
        // this constructor is needed by OJB just to
        // invoke the usesAccessorsAndMutators method
        this.declaringClass = null;
        this.propName = null;
    }

    public FenixPersistentField(Class declaringClass, String propName) {
        this.declaringClass = declaringClass;
        this.propName = propName;
    }

    @Override
    public boolean usesAccessorsAndMutators() {
        return true;
    }

    @Override
    public Class getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public String getName() {
        return propName;
    }

    protected Method findGetter(Class declaringClass, String name) {
        try {
            Class currentClass = declaringClass;
            while (currentClass != null) {
                try {
                    return currentClass.getDeclaredMethod(name);
                } catch (NoSuchMethodException nsme) {
                    currentClass = currentClass.getSuperclass();
                }
            }
        } catch (Exception e) {
            throw new MetadataException("Can't find method " + name + " in " + declaringClass.getName(), e);
        }

        throw new MetadataException("Can't find method " + name + " in " + declaringClass.getName());
    }
}
