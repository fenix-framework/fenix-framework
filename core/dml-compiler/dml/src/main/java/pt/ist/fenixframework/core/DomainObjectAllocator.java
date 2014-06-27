package pt.ist.fenixframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.reflect.ReflectionFactory;

@SuppressWarnings("restriction")
public class DomainObjectAllocator {

    private final ConcurrentMap<String, Constructor<?>> cache = new ConcurrentHashMap<>();

    private final Constructor<?> baseCtor;

    public DomainObjectAllocator(Class<?> baseType) {
        this.baseCtor = getConstructor(baseType);
    }

    public <T extends AbstractDomainObject> T allocateObject(Class<T> objClass, Object oid) {
        if (objClass == null) {
            throw new RuntimeException("Cannot allocate object '" + oid + "'. Class not found");
        }
        try {
            return getInstantiatorOf(objClass).newInstance(new OID(oid));
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Could not allocate object " + oid + " of class " + objClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Constructor<T> getInstantiatorOf(Class<T> clazz) {
        Constructor<?> instantiator = cache.get(clazz.getName());
        if (instantiator == null) {
            Constructor<?> newInstantiator =
                    ReflectionFactory.getReflectionFactory().newConstructorForSerialization(clazz, baseCtor);
            newInstantiator.setAccessible(true);
            instantiator = cache.putIfAbsent(clazz.getName(), newInstantiator);
            if (instantiator == null) {
                instantiator = newInstantiator;
            }
        }
        return (Constructor<T>) instantiator;
    }

    private Constructor<?> getConstructor(Class<?> baseType) {
        try {
            return baseType.getDeclaredConstructor(new Class<?>[] { DomainObjectAllocator.OID.class });
        } catch (NoSuchMethodException e) {
            throw new Error("Base type " + baseType.getName() + " does not declare the required constructor!");
        }
    }

    public static class OID {
        public final Object oid;

        private OID(Object oid) {
            this.oid = oid;
        }
    }

}
