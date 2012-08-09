package pt.ist.fenixframework.atomic;

import java.lang.reflect.Method;

import pt.ist.fenixframework.Atomic;

/**
 * This DefaultContextFactoryViaReflection tries to find, via reflection, a class named {@link
 * pt.ist.fenixframework.atomic.DefaultContextFactory}.  If it fails, it throws an Error, indicating such condition.  If it
 * succeeds, it delegates to the {@link pt.ist.fenixframework.atomic.DefaultContextFactory} the creation of the {@link
 * AtomicContext}.
 */
public final class DefaultContextFactoryViaReflection extends ContextFactory {

    public static AtomicContext newContext(Atomic atomic) {
        try {
            Class<? extends ContextFactory> factoryClass =
                (Class<? extends ContextFactory>)Class.forName("pt.ist.fenixframework.atomic.DefaultContextFactory");
            
            Method m = factoryClass.getMethod("newContext", new Class[]{Atomic.class});
            
            return (AtomicContext)m.invoke(null, new Object[]{atomic});
        } catch (Exception e) {
            throw new Error("Could not obtain an AtomicContext via the DefaultContextFactory", e);
        }
    }

}
