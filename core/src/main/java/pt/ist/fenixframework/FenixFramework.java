package pt.ist.fenixframework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.dml.DmlCompilerException;
import pt.ist.fenixframework.dml.DomainModel;

/**
 * <p>This is the main class for the Fenix Framework.  It is the central point for obtaining most of
 * the APIs that the user of the framework (a programmer) should need.
 *
 * <p>Before being able to use the framework, a program must initialize it, by providing a
 * configuration.  There are two (disjoint) alternative methods to initialize the FenixFramework:
 * 
 * <ul>
 *
 * <li><strong>By convention</strong>: When the FenixFramework class is loaded, its static
 * initialization code will check for the presence of a properties file that provides the
 * configuration information.</li>
 *
 * <li><strong>Explicitly</strong>: The programmer either creates a {@link Config} or {@link
 * MultiConfig} instance and then explicitly calls the corresponding initialization method: {@link
 * FenixFramework#initialize(Config)} or {@link FenixFramework#initialize(MultiConfig)}.</li>
 *
 * </ul>
 *
 * <p>Using the configuration by convention, the framework will first look up the name of the {@link
 * pt.ist.fenixframework.backend.BackEnd} that generated the domain-specific code (this is done by
 * instantiating the class {@link pt.ist.fenixframework.backend.CurrentBackEndId} and by requesting
 * {@link pt.ist.fenixframework.backend.BackEndId#getBackEndName}) and then use that name to check
 * for the presence of the file '<code>fenix-framework-&lt;NNN&gt;.properties</code>' in the
 * classpath using <code>Thread.currentThread().getContextClassLoader().getResource()</code> (where
 * <code>&lt;NNN&gt;</code> is the name of the BackEnd).  If this file is found, and contains the
 * correct content, the framework will create the appropriate Config instance and automatically
 * invoke {@link FenixFramework#initialize(Config)}.
 *
 * The syntax for the configuration file is to have each line in the form:
 *
 * <blockquote>
 *
 * <code>property=value</code>
 *
 * </blockquote>
 *
 * where each <code>property</code> must be the name of an existing configuration field.
 * Additionally, there is one optional special property named <code>config.class</code>.  By
 * default, the config parser creates an instance of the {@link BackEndId#getDefaultConfigClass()}
 * provided by the current BackEndId, but this property can be used to choose a different (albeit
 * compatible) configuration class.  The config instance is then populated with each property
 * (except with the <code>config.class</code> property), using the following algorithm:
 *
 * <ol>
 *
 * <li> Confirm that the config has a field with the same name as the <code>property</code>.  If not
 * a {@link ConfigError} is thrown.</li>
 *
 * <li> If the config class provides a method (can be private) in the format
 * <code>&lt;property&gt;FromString(String)</code>, then such method will be invoked to set the
 * property.  The {@link String} argument will be the <code>value</code>.</li>
 *
 * <li> Else, attempt to directly set the property on the field assigning it the <code>value</code>
 * String.</li>
 *
 * <li> If all previous attempts have failed, throw a {@link ConfigError}.</li>
 *
 * </ol>
 *
 * <p>The rationale supporting the previous mechanism is to allow the config class to process the
 * String provided in the <code>value></code> using the <code>*FromString</code> method.
 * 
 * <p> After population of the config finishes with success, the {@link
 * FenixFramework#initialize(Config)} method is invoked with the created Config instance.  From this
 * point on, the initialization process continues just as if the programmer had explicitly invoked
 * that initialization method.
 *
 * <p>The explicit configuration maintains the original configuration style (since Fenix Framework
 * 1.0) with compile-time checking of the config's attributes. To use it, read the documentation in
 * the {@link Config} class.  It also adds the possibility for the programmer to provide multiple
 * configurations and then let the initialization process decide which to use based on the current
 * {@link pt.ist.fenixframework.backend.BackEnd} (see {@link MultiConfig} for more details).
 *
 * <p>After initialization completes with success, the framework is ready to manage operations on
 * the domain objects.  Also, it is possible to get an instance of the {@link DomainModel} class
 * representing the structure of the application's domain.
 * 
 * @see Config
 * @see dml.DomainModel
 */
public class FenixFramework {
    private static final Logger logger = LoggerFactory.getLogger(FenixFramework.class);

    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_DEFAULT = "fenix-framework.properties";
    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_PREFIX = "fenix-framework-";
    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_SUFFIX = ".properties";

    private static final String FENIX_FRAMEWORK_LOGGING_CONFIG = "fenix-framework-log4j.properties";

    private static final Object INIT_LOCK = new Object();
    // private static boolean bootstrapped = false;
    private static boolean initialized = false;

    private static Config config;

    /** This is initialized on first invocation of {@link FenixFramework#getDomainModel()}, which
     * can only be invoked after the framework is initialized. */
    private static DomainModel domainModel = null;

    // private static Logger logger = null;
    static {
        // System.out.println("ERROR?: " + logger.isErrorEnabled());
        // System.out.println("WARN?: " + logger.isWarnEnabled());
        // System.out.println("INFO?: " + logger.isInfoEnabled());
        // System.out.println("DEBUG?: " + logger.isDebugEnabled());
        // System.out.println("TRACE?: " + logger.isTraceEnabled());
        // logger.error("INIT FF");
        // logger.warn("INIT FF");
        // logger.info("INIT FF");
        // logger.debug("INIT FF");
        // logger.trace("INIT FF");

        logger.trace("Static initializer block for FenixFramework class [BEGIN]");
        synchronized (INIT_LOCK) {
            logger.info("Trying auto-initialization with configuration by convention");
            tryAutoInit();
        }
        logger.trace("Static initializer block for FenixFramework class [END]");
    }

    /**
     * Attempts to automatically initialize the framework by reading the configuration parameters
     * from a resource.  The name of the resource depends on the name of the current backend.  If
     * such resource is not found, a default fenix-framework.properties will still be attempted.  If
     * neither a backend specific nor a default properties file is found, then the auto
     * initialization process gives up.
     */
    private static void tryAutoInit() {
        // look up current backend's name
        String currentBackEndName = BackEndId.getBackEndId().getBackEndName();
        logger.debug("CurrentBackEndName = " + currentBackEndName);

        // try auto init for the given backend
        boolean success = tryAutoInit(FENIX_FRAMEWORK_CONFIG_RESOURCE_PREFIX + currentBackEndName
                                      + FENIX_FRAMEWORK_CONFIG_RESOURCE_SUFFIX);
        if (!success) {
            // try auto init with default resource name
            if (!tryAutoInit(FENIX_FRAMEWORK_CONFIG_RESOURCE_DEFAULT)) {
                logger.info("Skipping configuration by convention.");
            }
        }
    }

    /** Attempts to automatically initialize the framework by reading the
     * configuration parameters from a named resource. */
    private static boolean tryAutoInit(String resourceName) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            logger.info("Resource '" + resourceName + "' not found");
            return false;
        }

        logger.info("Found configuration by convention in resource '" + resourceName + "'.");

        Config config = null;
        try {
            config = createConfigFromResourceStream(in);
        } catch (IOException e) {
            logger.debug("Failed auto initialization with " + resourceName, e);
            return false;
        }
        FenixFramework.initialize(config);
        return true;
    }

    private static Config createConfigFromResourceStream(InputStream in) throws IOException {
        // get the properties
        Properties props = new Properties();
        props.load(in);
        try { in.close(); } catch (Throwable ignore) {}
        
        // get the config instance
        Config config = null;
        try {
            Class<? extends Config> configClass = null;
            // first check for possible overriding in the config file
            String configClassName = props.getProperty(Config.PROPERTY_CONFIG_CLASS);
            if (configClassName != null) {
                try {
                    configClass = (Class<? extends Config>)Class.forName(configClassName);
                } catch (ClassNotFoundException e) {
                    // here, we could ignore and attempt the default config class, but it's best if
                    // the programmer understands that the configuration is flawed
                    throw new ConfigError(ConfigError.CONFIG_CLASS_NOT_FOUND, configClassName);
                }
            } else { // fallback to the current backend's default
                configClass = BackEndId.getBackEndId().getDefaultConfigClass();
            }
            config = configClass.newInstance();
        } catch (InstantiationException e) {
            throw new ConfigError(e);
        } catch (IllegalAccessException e) {
            throw new ConfigError(e);
        }
        
        // populate config from properties
        config.populate(props);
        
        return config;
    }

    /**
     * @return whether the <code>FenixFramework.initialize</code> method has
     * already been invoked
     */
    public static boolean isInitialized() {
	synchronized (INIT_LOCK) {
            return initialized;
        }
    }

    /** This method initializes the FenixFramework.  It must be the first method
     * to be called, and it should be invoked only once.  It needs to be called
     * before starting to access any Transactions/DomainObjects, etc.
     *
     * @param newConfig The configuration that will be used by this instance of the framework.
     */
    public static void initialize(Config newConfig) {
	synchronized (INIT_LOCK) {
	    if (initialized) {
		throw new ConfigError(ConfigError.ALREADY_INITIALIZED);
	    }

            if (newConfig == null) {
                logger.warn("Initialization with a 'null' config instance.");
                throw new ConfigError("A configuration must be provided");
            }

            logger.info("Initializing Fenix Framework with config.class=" + newConfig.getClass().getName());
	    FenixFramework.config = newConfig;

            // domainModelURLs should have been set by now
            FenixFramework.config.checkForDomainModelURLs();

            // set the domain model.  This must be done before calling FenixFramework.config.initialize()
            try {
                domainModel = DmlCompiler.getDomainModel(Arrays.asList(FenixFramework.config.getDomainModelURLs()));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new ConfigError(ex);
            }

            FenixFramework.config.initialize();
	    // DataAccessPatterns.init(FenixFramework.config);
	    initialized = true;
	}
        logger.info("Initialization of Fenix Framework is now complete.");
    }
    
    public static void initialize(MultiConfig configs) {
	synchronized (INIT_LOCK) {
            // look up current backend's name
            String currentBackEndName = BackEndId.getBackEndId().getBackEndName();
            logger.debug("CurrentBackEndName = " + currentBackEndName);
            // get the correct config for the current backend
            Config config = configs.get(currentBackEndName);
            // initialize
            FenixFramework.initialize(config);
        }
    }

    public static <T extends Config> T getConfig() {
        if (config == null) {
            throw new ConfigError(ConfigError.MISSING_CONFIG);
        }
	return (T) config;
    }

    /**
     * Gets the model of the domain classes.  Should only be invoked after the framework is
     * initialized.
     *
     * @return The current {@link DomainModel} in use.
     * @throws ConfigError If this method is invoked before the framework is initialized or if a
     * {@link DmlCompilerException} occurs (only possible on first invocation).
     */
    public static DomainModel getDomainModel() {
        return domainModel;
    }

    /**
     *  Always gets a well-known singleton instance of {@link DomainRoot}.  The intended use of this
     *  instance is to provide a single entry point to the graph of {@link DomainObject}s.  The user
     *  of the framework may connect (via DML) any {@link DomainObject} to this class.
     */
    public static DomainRoot getDomainRoot() {
        return getConfig().getBackEnd().getDomainRoot();
    }

    /**
     * Get any {@link DomainObject} given its external identifier.
     *
     * The external identifier must have been obtained by a previous invocation to {@link
     * DomainObject#getExternalId}.  If the external identifier is tampered with (in which case a
     * valid {@link DomainObject} cannot be found), the result of calling this method is undefined.
     *
     * @param externalId The external identifier of the domain object to get
     * @return The domain object requested
     *
     */
    public static <T extends DomainObject> T getDomainObject(String externalId) {
        return getConfig().getBackEnd().getDomainObject(externalId);
    }

    public static TransactionManager getTransactionManager() {
        return getConfig().getBackEnd().getTransactionManager();
    }

    /**
     * Inform the framework components that the application intends to shutdown.  This allows for an
     * orderly termination of any running components.  The execution of this method is also
     * delegated on the backend-specific implementation.  After invoking this method there is no
     * guarantee that the Fenix Framework is able to provide any more services.
     */
    public static void shutdown() {
        getConfig().getBackEnd().shutdown();
    }
}
