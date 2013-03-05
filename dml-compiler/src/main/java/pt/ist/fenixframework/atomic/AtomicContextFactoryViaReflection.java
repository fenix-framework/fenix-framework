package pt.ist.fenixframework.atomic;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.esw.advice.Advice;
import pt.ist.esw.advice.AdviceFactory;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.dml.CodeGenerator;

/**
 * This AtomicContextFactoryViaReflection tries to find, via reflection, the
 * {@link pt.ist.fenixframework.atomic.AtomicContextFactory} class of the backend in use. It does so, first by getting the current
 * <code>BackEndId</code> (by reflection) and then by
 * getting the factory class (through <code>getAtomicContextFactoryClass()</code> --- again by reflection). It delegates to
 * that factory the creation of the appropriate {@link AtomicContext}. If it fails, it throws an Error, indicating such condition.
 */
public final class AtomicContextFactoryViaReflection extends AdviceFactory<Atomic> {
    private static final Logger logger = LoggerFactory.getLogger(AtomicContextFactoryViaReflection.class);

    private static final String BACKEND_ID_FULL_CLASS_NAME = CodeGenerator.BACKEND_PACKAGE + "."
            + CodeGenerator.ABSTRACT_BACKEND_ID_CLASS;

    private final static AdviceFactory<Atomic> instance = new AtomicContextFactoryViaReflection();

    public static AdviceFactory<Atomic> getInstance() {
        return instance;
    }

    // This code uses reflection, but it only runs once per occurrence of the @Atomic annotation in
    // the code, regardless of the number of times that such method runs.
    @Override
    public Advice newAdvice(Atomic atomic) {
        logger.trace("Creating a new AtomicContext via reflection");

        try {
            // GOAL: currentBackendId = BackEndId.getInstance();
            Class backendIdClass = Class.forName(BACKEND_ID_FULL_CLASS_NAME);
            Method getInstance = backendIdClass.getMethod("getBackEndId");
            Object currentBackendId = getInstance.invoke(null);

            // GOAL: Class<? extends AtomicContextFactory> factoryClass = currentBackendId.getAtomicContextFactoryClass()
            Method getAtomicContextFactoryClass = currentBackendId.getClass().getMethod("getAtomicContextFactoryClass");
            Class<? extends AtomicContextFactory> factoryClass =
                    (Class<? extends AtomicContextFactory>) getAtomicContextFactoryClass.invoke(currentBackendId);

            // GOAL: return (AtomicContext)contextFactory.newAtomicContext(atomic);
            Method newAtomicContext = factoryClass.getMethod("newAtomicContext", new Class[] { Atomic.class });
            return (AtomicContext) newAtomicContext.invoke(null, new Object[] { atomic });
        } catch (Exception e) {
            throw new Error("Could not obtain an AtomicContext via the AtomicContextFactory", e);
        }
    }

}
