package pt.ist.fenixframework;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.DmlFile;
import pt.ist.fenixframework.core.Project;
import pt.ist.fenixframework.core.exception.ProjectException;
import pt.ist.fenixframework.util.Converter;

/**
 * <p>
 * An instance of the <code>Config</code> class bundles together the initialization parameters used by the Fenix Framework.
 * 
 * Therefore, before initializing the framework (via the call to the {@link FenixFramework#initialize(Config)} method), the
 * programmer should create an instance of <code>Config</code> with the correct values for each of the parameters.
 * </p>
 * 
 * <p>
 * No constructor is provided for this class (other than the default constructor), because the <code>Config</code> class has
 * several parameters, some of which are optional. But, whereas optional parameters do not need to be specified, the parameters
 * that are required must be specified by the programmer before calling the {@link FenixFramework#initialize(Config)} method.
 * 
 * <p>
 * Additional configuration parameters may be added by subclassing this class. Subclasses of config can override the
 * {@link #init()} method. Typically, their own {@link #init()} should also call {@link super#init()} if an hierarchy of configs
 * is used.
 * 
 * <p>
 * To create an instance of this class with the proper values for its parameters, programmers should generally use code like this
 * (assuming one specific backend as an example):
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * Config config = new MemConfig() { // any subclass of Config should be ok
 *             {
 *                 this.domainModelURLs = resourceToURLArray(&quot;path/to/domain.dml&quot;);
 *                 this.appName = &quot;MyAppName&quot;;
 *             }
 *         };
 * 
 * </pre>
 * 
 * </blockquote>
 * 
 * Note that the <code>Config</code> takes an array of URLs for the <code>domainModelURLs</code>. Utility methods are provided in
 * this class to convert from other typical representations to their corresponding URL. All of the utility method return the
 * required <code>URL[]</code>. Here are some guidelines:
 * 
 * <ul>
 * 
 * <li>{@link #resourceToURLArray(String)}: looks up the given resource using
 * <code>Thread.currentThread().getContextClassLoader().getResource(String)</code>;</li>
 * 
 * <li>{@link #filenameToURLArray(String)}: looks up the given file on the local filesystem;</li>
 * 
 * <li>Methods using the plural form (e.g. {@link #resourcesToURLArray(String [])}) are equivalent, except that they take multiple
 * DML file locations.</li>
 * 
 * </ul>
 * 
 * Note the use of the double
 * 
 * <pre>
 * { {} }
 * </pre>
 * 
 * to delimit an instance initializer block for the anonymous inner class being created.
 * 
 * Each of the parameters of the <code>Config</code> class is represented as a protected class field. Look at the documentation of
 * each field to see what is it for, whether it is optional or required, and in the former case, what is its default value.
 * 
 * @see pt.ist.fenixframework.FenixFramework
 * @see pt.ist.fenixframework.backend.mem.MemConfig
 * 
 */
public abstract class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    protected static final String NO_DOMAIN_MODEL =
            "Fenix Framework cannot proceed without a domain model!.  Either provide \"appName\" (for which there is a project.properties file) or explicitly set \"domainModelURLs\".";

    protected static final String PROPERTY_CONFIG_CLASS = "config.class";
    // the suffix of the method that sets a property from a String property
    protected static final String SETTER_FROM_STRING = "FromString";

    /**
     * This <strong>required</strong> parameter specifies the <code>URL[]</code> to each file
     * containing the DML code that corresponds to the domain model of the application. A non-empty
     * array must be specified for this parameter.
     */
    protected URL[] domainModelURLs = null;

    /**
     * This <strong>optional</strong> parameter specifies a name for the application. When using
     * configuration by convention, this name (if set) is used to lookup the <code>&lt;appName&gt;/project.properties</code> file.
     * Additionally, this name will be used
     * by the framework in the statistical logs performed during the application execution. The
     * default value for this parameter is <code>null</code>.
     */
    protected String appName = null;

    /**
     * This <strong>optional</strong> parameter specifies the number of nodes that are expected to
     * be deployed. This can be used by the backends to perform some setup, e.g. to wait for some
     * number of nodes to become live in order to complete the initialization process. The default
     * value for this parameter is <code>1</code>.
     */
    protected int expectedInitialNodes = 1;

    /**
     * This <strong>optional</strong> parameter specifies the JGroups configuration file. This
     * configuration will used to create channels between Fenix Framework nodes. The default value
     * for this parameter is <code>fenix-framework-udp-jgroups.xml</code>, which is the default
     * configuration file that ships with the framework.
     */
    protected String jGroupsConfigFile = "fenix-framework-udp-jgroups.xml";

    protected void checkRequired(Object obj, String fieldName) {
        if (obj == null) {
            missingRequired(fieldName);
        }
    }

    /**
     * Check if the value of <code>domainModelURLs</code> is already set.
     */
    protected void checkForDomainModelURLs() {
        if ((domainModelURLs == null) || (domainModelURLs.length == 0)) {
            if (logger.isErrorEnabled()) {
                logger.error(NO_DOMAIN_MODEL);
            }
            missingRequired("domainModelURLs");
        }
    }

    /**
     * Subclasses of this class can overwrite this method, but they should specifically call <code>super.checkConfig()</code> to
     * check the superclass's configuration.
     */
    protected void checkConfig() {
        checkForDomainModelURLs();
    }

    protected static void missingRequired(String fieldName) {
        throw new ConfigError(ConfigError.MISSING_REQUIRED_FIELD, "'" + fieldName + "'");
    }

    /**
     * This method is invoked by the {@link FenixFramework#initialize(Config)}.
     */
    protected final void initialize() {
        checkConfig();
        init();
    }

    // set each property via reflection, ignoring the config.class property, which was used to
    // define which config instance to create
    protected final void populate(Properties props) {
        for (String propName : props.stringPropertyNames()) {
            if (PROPERTY_CONFIG_CLASS.equals(propName)) {
                continue;
            }
            String value = props.getProperty(propName);
            setProperty(propName, value);
        }
    }

    protected final void setProperty(String propName, String value) {
        // first check if it really exists
        Field field = null;
        try {
            field = getField(this.getClass(), propName);
        } catch (ConfigError e) {
            // we choose to ignore unknown config properties, but we do give a loud warning
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return;
        }

        // note that lazy evaluation is used on purpose!
        boolean success =
                attemptSetPropertyUsingMethod(getSetterFor(this.getClass(), propName + SETTER_FROM_STRING), value)
                        || attemptSetPropertyUsingField(field, value);

        if (!success) {
            throw new ConfigError(ConfigError.COULD_NOT_SET_PROPERTY, propName);
        }
    }

    private Field getField(Class<? extends Config> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            // climb the hierarchy, but only up to Config class
            Class<?> superclass = clazz.getSuperclass();
            if (Config.class.isAssignableFrom(superclass)) {
                return getField((Class<? extends Config>) superclass, fieldName);
            } else {
                throw new ConfigError(ConfigError.UNKNOWN_PROPERTY + fieldName, e);
            }
        }
    }

    private boolean attemptSetPropertyUsingMethod(Method setter, String value) {
        if (setter == null) {
            return false;
        }

        setter.setAccessible(true);
        try {
            setter.invoke(this, value);
        } catch (Exception e) {
            throw new ConfigError(e);
        }
        return true;
    }

    private boolean attemptSetPropertyUsingField(Field field, String value) {
        field.setAccessible(true);
        try {
            field.set(this, value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Method getSetterFor(Class<? extends Config> clazz, String setterName) {
        try {
            return clazz.getDeclaredMethod(setterName, new Class[] { String.class });
        } catch (NoSuchMethodException e) {
            // climb the hierarchy, but only up to Config class
            Class<?> superclass = clazz.getSuperclass();
            if (Config.class.isAssignableFrom(superclass)) {
                return getSetterFor((Class<? extends Config>) superclass, setterName);
            } else {
                return null;
            }
        }
    }

    private void checkForMultipleDomainModelUrlsDefinition() {
        if (domainModelURLs != null) { // means that it was already set
            if (logger.isErrorEnabled()) {
                logger.error(ConfigError.DUPLICATE_DEFINITION_OF_DOMAIN_MODEL_URLS);
            }
            throw new ConfigError(ConfigError.DUPLICATE_DEFINITION_OF_DOMAIN_MODEL_URLS);
        }
    }

    /**
     * Note: Either appNameFromString or domainModelURLsFromString should be used, but not both!
     */
    public void appNameFromString(String value) {
        this.appName = value;

        try {
            List<DmlFile> dmlFiles = Project.fromName(value).getFullDmlSortedList();
            if (dmlFiles.size() > 0) {
                checkForMultipleDomainModelUrlsDefinition();
            }

            domainModelURLs = new URL[dmlFiles.size()];
            int counter = 0;
            for (DmlFile dmlFile : dmlFiles) {
                domainModelURLs[counter++] = dmlFile.getUrl();
            }

        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("failed when setting appNameFromString", e);
            }
        } catch (ProjectException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("failed when setting appNameFromString", e);
            }
        }
    }

    /**
     * Note: Either appNameFromString or domainModelURLsFromString should be used, but not both!
     */
    protected void domainModelURLsFromString(String value) {
        checkForMultipleDomainModelUrlsDefinition();

        String[] tokens = value.split("\\s*,\\s*");
        URL[] urls = new URL[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            // try URL from resource
            urls[i] = Converter.resourceToURL(tokens[i]);
            if (urls[i] != null) {
                continue;
            }

            // try URL from filename
            urls[i] = Converter.filenameToURL(tokens[i]);
            if (urls[i] != null) {
                continue;
            }

            // failed to get the URL
            throw new Error("FenixFramework config error: cannot find DML '" + tokens[i] + "'");
        }
        domainModelURLs = urls;
    }

    protected void expectedInitialNodesFromString(String value) {
        try {
            expectedInitialNodes = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ConfigError(e);
        }
    }

    protected abstract void init();

    /**
     * Utility method to wait for the number of expected initial nodes to be up.
     */
    public void waitForExpectedInitialNodes(String barrierName) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("expectedInitialNodes=" + expectedInitialNodes);
        }
        if (expectedInitialNodes > 1) {
            if (logger.isDebugEnabled()) {
                logger.debug("Waiting until " + expectedInitialNodes + " nodes are up");
            }
            FenixFramework.barrier(barrierName, expectedInitialNodes);
            if (logger.isDebugEnabled()) {
                logger.debug("All nodes are up.");
            }
        }
    }

    /**
     * Get the current {@link BackEnd} in use.
     */
    public abstract BackEnd getBackEnd();

    public URL[] getDomainModelURLs() {
        return domainModelURLs;
    }

    public int getExpectedInitialNodes() {
        return expectedInitialNodes;
    }

    public String getJGroupsConfigFile() {
        return jGroupsConfigFile;
    }

    public String getAppName() {
        return appName;
    }

    /**
     * Subclasses of this class can overwrite this method, but they should specifically call <code>super.shutdown()</code> to
     * orderly shutdown the framework.
     */
    protected void shutdown() {
        getBackEnd().shutdown();
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - BEGIN */
    /* code linked from pt.ist.fenixframework.util.Converter to make configuration API more straightforward */

    // REGARDING RESOURCES

    public static URL[] resourceToURLArray(String resource) {
        return Converter.resourceToURLArray(resource);
    }

    public static URL[] resourcesToURLArray(String... resources) {
        return Converter.resourcesToURLArray(resources);
    }

    // REGARDING FILENAMES

    public static URL[] filenameToURLArray(String filename) {
        return Converter.filenameToURLArray(filename);
    }

    public static URL[] filenamesToURLArray(String... filenames) {
        return Converter.filenamesToURLArray(filenames);
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - END */
}
