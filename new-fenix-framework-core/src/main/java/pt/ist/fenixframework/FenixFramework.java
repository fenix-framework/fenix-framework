package pt.ist.fenixframework;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

// import jvstm.TransactionalCommand;
// import pt.ist.fenixframework.pstm.DataAccessPatterns;
// import pt.ist.fenixframework.pstm.MetadataManager;
// import pt.ist.fenixframework.core.PersistentRoot;
// import pt.ist.fenixframework.pstm.Transaction;
// import pt.ist.fenixframework.pstm.repository.RepositoryBootstrap;
// import pt.ist.fenixframework.core.Repository;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.core.DefaultConfig;

/**
 * <p>This is the main class for the Fenix Framework.  It is the central point for obtaining most of
 * the APIs that the user of the framework (a programmer) should need.
 *
 * <p>Before being able to use the framework, a program must initialize it, by providing a
 * configuration.  There are two (disjoint) alternative methods to initialize the FenixFramework:
 * 
 * <ul>
 *
 * <li>Explicitly: The programmer creates the {@link Config} instance and explicitly calls the
 * {@link FenixFramework#initialize(Config)} method.</li>
 *
 * <li>By convention: When the FenixFramework class is loaded, its static initialization code will
 * check for the presence of the file 'fenix-framework.properties' in the classpath using
 * <code>Thread.currentThread().getContextClassLoader().getResource("fenix-framework.properties")</code>.
 * If this file is found, and contains the correct content, the framework will create the
 * appropriate Config (or subclass) instance and automatically invoke {@link
 * FenixFramework#initialize(Config)}.</li>
 *
 * </ul>
 * 
 * <p>The first alternative maintains the previous configuration style with compile-time checking of
 * the config's attributes. To use it, read the documentation in the {@link Config} class.
 *
 * <p>Using the second option, the syntax for the configuration file is to have each line in the
 * form:
 *
 * <blockquote>
 *
 * <code>property=value</code>
 *
 * </blockquote>
 *
 * where each <code>property</code> must be the name of an existing configuration field.
 * Additionally, there is one optional property named <code>config.class</code>.  By default, the
 * config parser creates an instance of {@link pt.ist.fenixframework.core.DefaultConfig}, unless the
 * <code>config.class</code> is given, in which case the mentioned class' default constructor is
 * invoked to create an instance of a more specific Config instance.  The config instance is then
 * populated with each property (except with the <code>config.class</code> property), using the
 * following algorythm:
 *
 * <ol>
 *
 * <li> Confirm that the config has a field with the same name as the <code>property</code>.  If not
 * a {@link ConfigurationError} is thrown.</li>
 *
 * <li> If the config class provides a method (can be private) in the format
 * <code>&lt;property&gt;FromString(String)</code>, then such method will be used to set the
 * property.  The String argument will be the <code>value</code>.</li>
 *
 * <li> Else, attempt to directly set the property on the field assigning it the <code>value</code>
 * String.</li>
 *
 * <li> If all previous attempts have failed, throw a {@link ConfigurationError}.</li>
 *
 * </ol>
 *
 * <p>The rationale supporting the previous mechanism is to allow the config class to process the
 * String provided in the <code>value></code> using the <code>*FromString</code> method.
 * 
 * <p> After population of the config finishes with success, the {@link
 * FenixFramework#initialize(Config)} method is invoked with the created Config instance.
 *
 * <p>After initialization, it is possible to get an instance of the {@link DomainModel} class
 * representing the structure of the application's domain.
 * 
 * @see Config
 * @see dml.DomainModel
 */
public class FenixFramework {

    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE = "fenix-framework.properties";

    private static final Object INIT_LOCK = new Object();
    // private static boolean bootstrapped = false;
    private static boolean initialized = false;

    private static Config config;

    static {
        tryAutoInit();
    }

    /* Attempts to automatically initialize the framework by reading the configuration parameters
     * from a resource. */
    private static void tryAutoInit() {
        synchronized (INIT_LOCK) {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(FENIX_FRAMEWORK_CONFIG_RESOURCE);
            if (in == null) {
                // failed to auto init
                return;
            }
            Config config = null;
            try {
                config = createConfigFromResourceStream(in);
            } catch (IOException e) {
                // failed to auto init
                return;
            }
            FenixFramework.initialize(config);
        }
    }

    private static Config createConfigFromResourceStream(InputStream in) throws IOException {
        // get the properties
        Properties props = new Properties();
        props.load(in);
        try { in.close(); } catch (Throwable ignore) {}
        
        // get the config instance
        String configClassName = props.getProperty(Config.PROPERTY_CONFIG_CLASS, Config.DEFAULT_CONFIG_CLASS_NAME);
        Config config = null;
        try {
            Class<? extends Config> configClass = (Class<? extends Config>)Class.forName(configClassName);
            config = configClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigError(ConfigError.CONFIG_CLASS_NOT_FOUND, configClassName);
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
     * @return whether the <code>FenixFramework.initialize</code> method has already been invoked
     */
    public static boolean isInitialized() {
	synchronized (INIT_LOCK) {
            return initialized;
        }
    }

    /** This method initializes the FenixFramework.  It must be the first method to be called, and
     * it should be invoked only once.  It needs to be called before starting to access any
     * Transactions/DomainObjects, etc.
     *
     * @param config The configuration that will be used by this instance of the framework.
     */
    public static void initialize(Config config) {
	synchronized (INIT_LOCK) {
	    if (initialized) {
		throw new Error("Fenix framework already initialized");
	    }

	    FenixFramework.config = ((config != null) ? config
                                     : new DefaultConfig() {{ this.domainModelURLs = Config.resourceToURL("empty.dml"); }});
            FenixFramework.config.initialize();

	    // DataAccessPatterns.init(FenixFramework.config);
	    initialized = true;
	}
    }
    
    public static Config getConfig() {
        if (config == null) {
            throw new ConfigError(ConfigError.MISSING_CONFIG);
        }
	return config;
    }

    public static TransactionManager getTransactionManager() {
        return getConfig().getTransactionManager();
    }

    // public static DomainModel getDomainModel() {
    //     return MetadataManager.getDomainModel();
    // }

    public static <T extends DomainObject> T getDomainObject(String externalId) {
        return getConfig().getDomainObject(externalId);
    }
}
