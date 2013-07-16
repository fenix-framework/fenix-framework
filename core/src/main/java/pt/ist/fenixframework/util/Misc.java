package pt.ist.fenixframework.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Miscellaneous utility methods 
 */
public class Misc {
    private static final Logger logger = LoggerFactory.getLogger(Misc.class);

    /* use this class's logger if non is provided */
    public static void traceClassLoaderHierarchy() {
        traceClassLoaderHierarchy(logger);
    }

    /* Use with the provided logger, so that logging appear in the correct context. */
    public static void traceClassLoaderHierarchy(Logger logger) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        if (logger.isTraceEnabled()) {
            logger.trace("Classloader hierarchy starting from Thread Context Class Loader:");
        }
        try {
            do {
                if (logger.isTraceEnabled()) {
                    logger.trace("    -> " + cl);
                }
            } while ((cl = cl.getParent()) != null);
        } catch (Exception e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to list all classloaders", e);
            }
        }
    }
}
