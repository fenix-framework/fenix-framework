package pt.ist.fenixframework;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import eu.cloudtm.LocalityHints;
import pt.ist.fenixframework.jmx.JmxUtil;
import pt.ist.fenixframework.messaging.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.backend.ClusterInformation;
import pt.ist.fenixframework.dml.DmlCompilerException;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.messaging.MessagingQueue;
import pt.ist.fenixframework.util.NodeBarrier;

/**
 * <p>
 * This is the main class for the Fenix Framework. It is the central point for obtaining most of the APIs that the user of the
 * framework (a programmer) should need.
 * 
 * <p>
 * Before being able to use the framework, a program must initialize it, by providing a configuration. There are two (disjoint)
 * alternative methods to initialize the FenixFramework:
 * 
 * <ul>
 * 
 * <li><strong>By convention</strong>: When the FenixFramework class is loaded, its static initialization code will check for the
 * presence of a properties file that provides the configuration information or the existence of system properties (given through
 * the <code>-D</code> switch).</li>
 * 
 * <li><strong>Explicitly</strong>: The programmer either creates a {@link Config} or {@link MultiConfig} instance and then
 * explicitly calls the corresponding initialization method: {@link FenixFramework#initialize(Config)} or
 * {@link FenixFramework#initialize(MultiConfig)}.</li>
 * 
 * </ul>
 * 
 * <p>
 * Using the configuration by convention, the framework will first load the properties from the file
 * <code>fenix-framework.properties</code> if it exists. Then it will load the properties from the file
 * <code>fenix-framework-&lt;NNN&gt;.properties</code>, if it exists (where <code>&lt;NNN&gt;</code> is the name of the BackEnd
 * that generated the domain-specific code). Files are looked up in the application's classpath using
 * <code>Thread.currentThread().getContextClassLoader().getResource()</code>. Finally, it will load any system properties that
 * start with the prefix <code>fenixframework.</code> (and discard that prefix). Whenever the same property is defined more than
 * once, the last setting prevails. After the previous steps have been taken, if there is any property set, then the framework
 * will attempt to create a {@link Config} instance and automatically invoke {@link FenixFramework#initialize(Config)}.
 * 
 * The syntax for the configuration file is to have each line in the form:
 * 
 * <blockquote>
 * 
 * <code>property=value</code>
 * 
 * </blockquote>
 * 
 * where each <code>property</code> must be the name of an existing configuration field (when providind the properties via a
 * system property use <code>-Dfenixframework.property=value</code>). Additionally, there is one optional special property named
 * <code>config.class</code>. By default, the config parser creates an instance of the {@link BackEndId#getDefaultConfigClass()}
 * provided by the current BackEndId, but this property can be used to choose a different (albeit compatible) configuration class.
 * The config instance is then populated with each property (except with the <code>config.class</code> property), using the
 * following algorithm:
 * 
 * <ol>
 * 
 * <li>Confirm that the config has a field with the same name as the <code>property</code>. If not, ignore the property with a
 * warning.</li>
 * 
 * <li>If the config class provides a method (can be private) in the format <code>&lt;property&gt;FromString(String)</code>, then
 * such method will be invoked to set the property. The {@link String} argument will be the <code>value</code>.</li>
 * 
 * <li>Else, attempt to directly set the property on the field assigning it the <code>value</code> String.</li>
 * 
 * <li>If the previous attempts to set the value fail, throw a {@link ConfigError}.</li>
 * 
 * </ol>
 * 
 * <p>
 * The rationale supporting the previous mechanism is to allow the config class to process the String provided in the
 * <code>value></code> using the <code>*FromString</code> method.
 * 
 * <p>
 * After population of the config finishes with success, the {@link FenixFramework#initialize(Config)} method is invoked with the
 * created Config instance. From this point on, the initialization process continues just as if the programmer had explicitly
 * invoked that initialization method.
 * 
 * <p>
 * The explicit configuration maintains the original configuration style (since Fenix Framework 1.0) with compile-time checking of
 * the config's attributes. To use it, read the documentation in the {@link Config} class. It also adds the possibility for the
 * programmer to provide multiple configurations and then let the initialization process decide which to use based on the current
 * {@link pt.ist.fenixframework.backend.BackEnd} (see {@link MultiConfig} for more details).
 * 
 * <p>
 * After initialization completes with success, the framework is ready to manage operations on the domain objects. Also, it is
 * possible to get an instance of the {@link DomainModel} class representing the structure of the application's domain.
 * 
 * @see Config
 * @see pt.ist.fenixframework.dml.DomainModel
 */
public class FenixFramework {
    private static final Logger logger = LoggerFactory.getLogger(FenixFramework.class);

    public static final String FENIX_FRAMEWORK_SYSTEM_PROPERTY_PREFIX = "fenixframework.";

    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_DEFAULT = "fenix-framework.properties";
    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_PREFIX = "fenix-framework-";
    private static final String FENIX_FRAMEWORK_CONFIG_RESOURCE_SUFFIX = ".properties";

    private static final Object INIT_LOCK = new Object();
    // private static boolean bootstrapped = false;
    private static boolean initialized = false;

    private static Config config;

    private static final ConcurrentMap<String, MessagingQueue> APPLICATION_LOAD_BALANCER = new ConcurrentHashMap<String, MessagingQueue>();

    private static final Object REQUEST_PROCESSOR_LOCK = new Object();
    private static RequestProcessor receiver;

    /**
     * This is initialized on first invocation of {@link FenixFramework#getDomainModel()}, which
     * can only be invoked after the framework is initialized.
     */
    private static DomainModel domainModel = null;

    private static NodeBarrier barrier;

    // private static Logger logger = null;
    static {
        // System.out.println("out.ERROR?: " + logger.isErrorEnabled());
        // System.out.println("out.WARN?: " + logger.isWarnEnabled());
        // System.out.println("out.INFO?: " + logger.isInfoEnabled());
        // System.out.println("out.DEBUG?: " + logger.isDebugEnabled());
        // System.out.println("out.TRACE?: " + logger.isTraceEnabled());
        // System.err.println("err.ERROR?: " + logger.isErrorEnabled());
        // System.err.println("err.WARN?: " + logger.isWarnEnabled());
        // System.err.println("err.INFO?: " + logger.isInfoEnabled());
        // System.err.println("err.DEBUG?: " + logger.isDebugEnabled());
        // System.err.println("err.TRACE?: " + logger.isTraceEnabled());
        // logger.error("INIT FF");
        // logger.warn("INIT FF");
        // logger.info("INIT FF");
        // logger.debug("INIT FF");
        // logger.trace("INIT FF");

        if (logger.isTraceEnabled()) {
            logger.trace("Static initializer block for FenixFramework class [BEGIN]");
        }
        synchronized (INIT_LOCK) {
            logger.info("Trying auto-initialization with configuration by convention");
            tryAutoInit();
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Static initializer block for FenixFramework class [END]");
        }
    }

    /**
     * Attempts to automatically initialize the framework by reading the configuration parameters
     * from a resource. The name of the resource depends on the name of the current backend. If
     * such resource is not found, a default fenix-framework.properties will still be attempted. If
     * neither a backend specific nor a default properties file is found, then the auto
     * initialization process gives up.
     */
    private static void tryAutoInit() {
        /* first load the default configuration if it exists */
        Properties props = loadProperties(FENIX_FRAMEWORK_CONFIG_RESOURCE_DEFAULT, new Properties());
        if (logger.isDebugEnabled()) {
            logger.debug("Fenix Framework properties after reading default config file:" + props.toString());
        }

        /* look up current backend's name */
        String currentBackEndName = BackEndId.getBackEndId().getBackEndName();
        if (logger.isDebugEnabled()) {
            logger.debug("CurrentBackEndName = " + currentBackEndName);
        }
        /* then override with the backend-specific config file */
        props =
                loadProperties(FENIX_FRAMEWORK_CONFIG_RESOURCE_PREFIX + currentBackEndName
                        + FENIX_FRAMEWORK_CONFIG_RESOURCE_SUFFIX, props);
        if (logger.isDebugEnabled()) {
            logger.debug("Fenix Framework properties after reading backend config file:" + props.toString());
        }

        /* finally, enforce any system properties */
        props = loadSystemProperties(props);
        if (logger.isDebugEnabled()) {
            logger.debug("Fenix Framework properties after enforcing system properties:" + props.toString());
        }

        // try auto init for the given properties.  If none exists just skip
        if (props.isEmpty() || !tryAutoInit(props)) {
            if (logger.isInfoEnabled()) {
                logger.info("Skipping configuration by convention.");
            }
        }
    }

    /**
     * Return a Properties setup from the given resourceName, backed by the default values if
     * any.
     */
    private static Properties loadProperties(String resourceName, Properties defaults) {
        Properties props = new Properties();
        props.putAll(defaults);

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
        if (in == null) {
            if (logger.isInfoEnabled()) {
                logger.info("Resource '" + resourceName + "' not found");
            }
            return props;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Found configuration by convention in resource '" + resourceName + "'.");
        }

        // add the new properties
        try {
            props.load(in);
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed auto initialization with " + resourceName, e);
            }
            return defaults;
        } finally {
            try {
                in.close();
            } catch (Throwable ignore) {
            }
        }
        return props;
    }

    private static Properties loadSystemProperties(Properties props) {
        Properties newProps = new Properties();
        newProps.putAll(props);
        Properties systemProps = System.getProperties();
        for (String propertyName : systemProps.stringPropertyNames()) {
            if (propertyName.startsWith(FENIX_FRAMEWORK_SYSTEM_PROPERTY_PREFIX)) {
                String value = systemProps.getProperty(propertyName);
                String realPropertyName = propertyName.substring(FENIX_FRAMEWORK_SYSTEM_PROPERTY_PREFIX.length());
                if (logger.isDebugEnabled()) {
                    logger.debug("Enforcing property from system: " + realPropertyName + "=" + value);
                }
                newProps.setProperty(realPropertyName, value);
            }
        }
        return newProps;
    }

    /**
     * Attempt to automatically initialize the framework with the given set of properties.
     */
    private static boolean tryAutoInit(Properties props) {
        Config config;
        try {
            config = createConfigFromProperties(props);
        } catch (ConfigError e) {
            if (logger.isErrorEnabled()) {
                logger.error("ConfigError", e);
            }
            throw e;
        }
        FenixFramework.initialize(config);
        return true;
    }

    private static Config createConfigFromProperties(Properties props) {
        // get the config instance
        Config config;
        try {
            Class<? extends Config> configClass;
            // first check for possible overriding in the config file
            String configClassName = props.getProperty(Config.PROPERTY_CONFIG_CLASS);
            if (configClassName != null) {
                try {
                    configClass = (Class<? extends Config>) Class.forName(configClassName);
                } catch (ClassNotFoundException e) {
                    // here, we could ignore and attempt the default config class, but it's best if
                    // the programmer understands that the configuration is flawed
                    if (logger.isErrorEnabled()) {
                        logger.error(ConfigError.CONFIG_CLASS_NOT_FOUND + configClassName, e);
                    }
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
     *         already been invoked
     */
    public static boolean isInitialized() {
        synchronized (INIT_LOCK) {
            return initialized;
        }
    }

    /**
     * This method initializes the FenixFramework. It must be the first method
     * to be called, and it should be invoked only once. It needs to be called
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
                if (logger.isWarnEnabled()) {
                    logger.warn("Initialization with a 'null' config instance.");
                }
                throw new ConfigError("A configuration must be provided");
            }

            if (logger.isInfoEnabled()) {
                logger.info("Initializing Fenix Framework with config.class=" + newConfig.getClass().getName());
            }
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

            try {
                FenixFramework.config.initialize();
            } catch (RuntimeException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not initialize Fenix Framework", e);
                }
                e.printStackTrace();
                throw e;
            } catch (ConfigError e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not initialize Fenix Framework", e);
                }
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not initialize Fenix Framework", e);
                }
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            try {
                getMessagingQueue(getConfig().getAppName());
            } catch (UnsupportedOperationException e) {
                //ignored!
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Could not initialize Fenix Framework", e);
                }
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            // DataAccessPatterns.init(FenixFramework.config);
            initialized = true;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Initialization of Fenix Framework is now complete.");
        }
    }

    public static void initialize(MultiConfig configs) {
        synchronized (INIT_LOCK) {
            // look up current backend's name
            String currentBackEndName = BackEndId.getBackEndId().getBackEndName();
            if (logger.isDebugEnabled()) {
                logger.debug("CurrentBackEndName = " + currentBackEndName);
            }
            // get the correct config for the current backend
            Config config = configs.get(currentBackEndName);
            // initialize
            FenixFramework.initialize(config);
        }
    }

    public static Config getConfig() {
        if (config == null) {
            throw new ConfigError(ConfigError.MISSING_CONFIG);
        }
        return config;
    }

    /**
     * Gets the model of the domain classes. Should only be invoked after the framework is
     * initialized.
     * 
     * @return The current {@link DomainModel} in use.
     * @throws ConfigError If this method is invoked before the framework is initialized or if a {@link DmlCompilerException}
     *             occurs (only possible on first invocation).
     */
    public static DomainModel getDomainModel() {
        return domainModel;
    }

    /**
     * Always gets a well-known singleton instance of {@link DomainRoot}. The intended use of this
     * instance is to provide a single entry point to the graph of {@link DomainObject}s. The user
     * of the framework may connect (via DML) any {@link DomainObject} to this class.
     */
    public static DomainRoot getDomainRoot() {
        return getConfig().getBackEnd().getDomainRoot();
    }

    /**
     * Get any {@link DomainObject} given its external identifier.
     * 
     * The external identifier must have been obtained by a previous invocation to {@link DomainObject#getExternalId}. If the
     * external identifier is tampered with (in which case a
     * valid {@link DomainObject} cannot be found), the result of calling this method is undefined.
     * 
     * @param externalId The external identifier of the domain object to get
     * @return The domain object requested
     * 
     */
    public static <T extends DomainObject> T getDomainObject(String externalId) {
        return getConfig().getBackEnd().getDomainObject(externalId);
    }

    /**
     * Returns the {@link DomainObject} that owns the given key. The key given should be a key used in the underlying key-value
     * storage platform. This method delegates to the concrete {@link BackEnd} the implementation of its behavior.
     * 
     * This method is Cloud-TM platform-specific.
     * 
     * @return the {@link DomainObject} that owns the storageKey provided.
     */
    public static <T extends DomainObject> T getOwnerDomainObject(String storageKey) {
        return getConfig().getBackEnd().getOwnerDomainObject(storageKey);
    }

    /**
     * Returns the list of keys used to store the slots (attributes) of given {@link DomainObject}. This method delegates to the
     * concrete {@link BackEnd} the implementation of its behavior.
     * 
     * This method is Cloud-TM platform-specific.
     * 
     * @param domainObject The {@link DomainObject} from which to get the storage keys
     * @return A String[] with the keys
     */
    public static String[] getStorageKeys(DomainObject domainObject) {
        return getConfig().getBackEnd().getStorageKeys(domainObject);
    }

    public static TransactionManager getTransactionManager() {
        return getConfig().getBackEnd().getTransactionManager();
    }

    public static Transaction getTransaction() {
        return getTransactionManager().getTransaction();
    }

    /**
     * Inform the framework components that the application intends to shutdown. This allows for an
     * orderly termination of any running components. The default implementation delegates to the
     * backend the task of shutting down the framework. After invoking this method there is no
     * guarantee that the Fenix Framework is able to provide any more services.
     */
    public static synchronized void shutdown() {
        synchronized (INIT_LOCK) {
            initialized = false;
            if (barrier != null) {
                barrier.shutdown();
            }
            getConfig().shutdown();
            shutdownMessagingQueue(getConfig().getAppName());
            JmxUtil.unregisterAllMBeans(getConfig().getAppName());
        }
    }

    private static synchronized NodeBarrier getNodeBarrier() throws Exception {
        //TODO: add jgroups configuration file to config
        if (barrier == null) {
            barrier = new NodeBarrier(getConfig().getJGroupsConfigFile());
        }
        return barrier;
    }

    public static void barrier(String barrierName, int expectedMembers) throws Exception {
        getNodeBarrier().blockUntil(barrierName, expectedMembers);
    }

    /**
     * See {@link pt.ist.fenixframework.backend.BackEnd#getClusterInformation()} and {@link pt.ist.fenixframework.backend.ClusterInformation}.
     *
     * @return the current cluster information.
     */
    public static ClusterInformation getClusterInformation() {
        return getConfig().getBackEnd().getClusterInformation();
    }

    private static MessagingQueue getMessagingQueue(String appName) throws Exception {
        if (appName == null) {
            return null;
        }
        MessagingQueue messagingQueue = APPLICATION_LOAD_BALANCER.get(appName);
        if (messagingQueue == null) {
            messagingQueue = getConfig().getBackEnd().createMessagingQueue(appName);
            MessagingQueue existing = APPLICATION_LOAD_BALANCER.putIfAbsent(appName, messagingQueue);
            if (existing != null) {
                messagingQueue = existing;
            } else {
                messagingQueue.init();
            }
        }
        return messagingQueue;
    }

    private static void shutdownMessagingQueue(String appName) {
        if (appName == null) {
            return;
        }
        MessagingQueue messagingQueue = APPLICATION_LOAD_BALANCER.remove(appName);
        if (messagingQueue != null) {
            messagingQueue.shutdown();
        }
    }

    public static void shutdownAllLoadBalancer() {
        for (MessagingQueue messagingQueue : APPLICATION_LOAD_BALANCER.values()) {
            messagingQueue.shutdown();
        }
        APPLICATION_LOAD_BALANCER.clear();
    }

    public static void registerReceiver(RequestProcessor r) {
        synchronized (REQUEST_PROCESSOR_LOCK) {
            if (receiver == null) {
                receiver = r;
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Request processor already registered.");
                }
            }
        }
    }

    public static Object handleRequest(String request) {
        RequestProcessor processor;
        synchronized (REQUEST_PROCESSOR_LOCK) {
            processor = receiver;
        }
        if (processor == null) {
            throw new IllegalStateException("No Request processor registered");
        }
        return processor.onRequest(request);
    }

    public static Object sendRequest(String request, String localityHint, String application, boolean sync) throws Exception {
        return sendRequest(request, localityHint, application, sync, true);
    }

    public static Object sendRequest(String request, String localityHint, String application, boolean sync, boolean write) throws Exception {
        if (application == null || application.isEmpty()) {
            throw new IllegalArgumentException("Application cannot be null or empty");
        }
        MessagingQueue queue = getMessagingQueue(application);
        if (queue == null) {
            if (logger.isErrorEnabled()) {
                logger.error("Message Queue for " + application + " not found!");
            }
            throw new RuntimeException("Message Queue for " + application + " not found!");
        }
        return queue.sendRequest(request, localityHint, sync, write);
    }

    public static void initConnection(String application) throws Exception {
        getMessagingQueue(application);
    }

    public static LocalityHints localityHintsFromExternalId(String externalId) {
        return getConfig().getBackEnd().getLocalityHints(externalId);
    }

    public static Map<String, String> printLocationInfo(String application, Collection<String> localityHintsList) {
        if (application == null || application.isEmpty() || localityHintsList == null || localityHintsList.isEmpty()) {
            return Collections.emptyMap();
        }
        MessagingQueue messagingQueue = APPLICATION_LOAD_BALANCER.get(application);
        if (messagingQueue == null) {
            return Collections.emptyMap();
        }
        return messagingQueue.printLocationInfo(localityHintsList);
    }
}
