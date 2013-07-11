package pt.ist.fenixframework.atomic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.dml.CodeGenerator;

/**
 * This DefaultContextFactoryViaReflection tries to find, via reflection, the {@link
 * pt.ist.fenixframework.atomic.ContextFactory} class of the backend in use.  It does so, first by
 * getting the current <code>BackEndId</code> (by reflection) and then by getting the factory class
 * (through <code>getAtomicContextFactoryClass()</code> --- again by reflection).  It delegates to
 * that factory the creation of the appropriate {@link AtomicContext}.  If it fails, it throws an
 * Error, indicating such condition.
 */
public final class DefaultContextFactoryViaReflection extends ContextFactory {
    private static final Logger logger = LoggerFactory.getLogger(DefaultContextFactoryViaReflection.class);

    private static final String BACKEND_ID_FULL_CLASS_NAME = CodeGenerator.BACKEND_PACKAGE + "." +
        CodeGenerator.ABSTRACT_BACKEND_ID_CLASS;

    // This code uses reflection, but it only runs once per occurrence of the @Atomic annotation in
    // the code, regardless of the number of times that such method runs.
    public static AtomicContext newContext(Atomic atomic) {
        if (logger.isTraceEnabled()) {
            logger.trace("Creating a new AtomicContext via reflection");
        }

        try {
            // GOAL: currentBackendId = BackEndId.getInstance();
            Class backendIdClass = Class.forName(BACKEND_ID_FULL_CLASS_NAME);
            Method getInstance = backendIdClass.getMethod("getBackEndId");
            Object currentBackendId = getInstance.invoke(null);

            // GOAL: Class<? extends ContextFactory> factoryClass = BackEndId.getAtomicContextFactoryClass()
            Method getAtomicContextFactoryClass = currentBackendId.getClass().getMethod("getAtomicContextFactoryClass");
            Class<? extends ContextFactory> factoryClass = (Class<? extends ContextFactory>)getAtomicContextFactoryClass.invoke(currentBackendId);

            // GOAL: return (AtomicContext)contextFactory.newContext(atomic);
            Method newContext = factoryClass.getMethod("newContext", new Class[]{Atomic.class});
            return (AtomicContext)newContext.invoke(null, new Object[]{atomic});
        } catch (Exception e) {
            throw new Error("Could not obtain an AtomicContext via the ContextFactory", e);
        }
    }

}
