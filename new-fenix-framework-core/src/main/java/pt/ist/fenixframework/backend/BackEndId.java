package pt.ist.fenixframework.backend;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.ConfigError;
import pt.ist.fenixframework.dml.CodeGenerator;

/**
 * This class exists to document a feature that all DML code generators must implement.  All {@link
 * pt.ist.fenixframework.dml.CodeGenerator} implementations should support the creation of a class
 * named {@link pt.ist.fenixframework.backend.CurrentBackEndId} (see {@link
 * pt.ist.fenixframework.dml.CodeGenerator#generateBackEndId}) that implements this abstract class
 * and provides information about the current backend that is being used to manage the domain model.
 * This is part of the support required for configuration by convention.
 *
 * @see pt.ist.fenixframework.FenixFramework
 */
public abstract class BackEndId {
    private static final Logger logger = Logger.getLogger(BackEndId.class);

    /**
     * Get the (unique) name of this {@link BackEnd}.  The String returned by this method should
     * contain only valid characters in a filename (because it can be used for configuration by
     * convention (see {@link pt.ist.fenixframework.FenixFramework}).
     *
     * @see pt.ist.fenixframework.FenixFramework
     */
    public abstract String getBackEndName();

    /**
     * Get the Class instance that represents the default configuration class to use for this
     * backend.  This class is used to instantite a {@link Config} instance is none is explicitly
     * indicated.
     */
    public abstract Class<? extends Config> getDefaultConfigClass();

    
    /**
     *  Lookup via reflection the {@link pt.ist.fenixframework.backend.CurrentBackEndId} class and
     *  return an instance of it.
     *
     * @throws ConfigError if the expected class does not exit, or it does not extend the BackEndId
     * class
     */
    public static final BackEndId getBackEndId() throws ConfigError {
        Exception ex = null;
        try {
            Class<CurrentBackEndId> currentBackEndIdClass = (Class<CurrentBackEndId>)Class.forName(CurrentBackEndId.class.getName());
            BackEndId beId = (BackEndId)currentBackEndIdClass.newInstance();
            // dont forget to catch ClassCastException from line 2
            return beId;
        } catch (ClassNotFoundException e) {
            ex = e;
        } catch (InstantiationException e) {
            ex = e;
        } catch (IllegalAccessException e) {
            ex = e;
        }

        String message = "Missing required BackEndId: " + CodeGenerator.BACKEND_PACKAGE
            + "." + CodeGenerator.CURRENT_BACKEND_ID_CLASS;
        logger.error(message);
        throw new ConfigError(message);
    }
}
