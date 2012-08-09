package pt.ist.fenixframework;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.Properties;

import pt.ist.fenixframework.core.BackEnd;
import pt.ist.fenixframework.util.Converter;

/**
 * <p> An instance of the <code>Config</code> class bundles together the initialization parameters
 * used by the Fenix Framework.
 *
 * Therefore, before initializing the framework (via the call to the
 * {@link FenixFramework#initialize(Config)} method), the programmer should create an instance of
 * <code>Config</code> with the correct values for each of the parameters.</p>
 * 
 * <p> No constructor is provided for this class (other than the default constructor), because the
 * <code>Config</code> class has several parameters, some of which are optional. But, whereas
 * optional parameters do not need to be specified, the parameters that are required must be
 * specified by the programmer before calling the <code>FenixFramework.initialize</code> method.
 *
 * <p> Additional configuration parameters may be added by subclassing this class.  Subclasses of
 * config can override the {@link #init()} method.  Typically, their own <code>init()</code> should
 * also call <code>super.init()</code>if an hierarchy of configs is used.
 * 
 * <p> To create an instance of this class with the proper values for its parameters, programmers
 * should generally use code like this:
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * Config config = new DefaultConfig() { // any subclass of Config should be ok
 *     {
 * 	this.domainModelURLs = resourceToURLArray(&quot;path/to/domain.dml&quot;);
 * 	this.appName = &quot;MyAppName&quot;;
 *     }
 * };
 * 
 * </pre>
 *
 * </blockquote>
 * 
 * Note that the <code>Config</code> takes an array of URLs for the <code>domainModelURLs</code>.
 * Utility methods are provided in this class to convert from other typical representations to their
 * corresponding URL.  All of the utility method return the required <code>URL[]</code>.  Here are
 * some guidelines:
 *
 * <ul>
 *
 * <li>{@link #resourceToURLArray(String)}: looks up the given resource using
 * <code>Thread.currentThread().getContextClassLoader().getResource(String)</code>;</li>
 *
 * <li>{@link #filenameToURLArray(String)}: looks up the given file on the local filesystem;
 * </li>
 *
 * <li>Methods using the plural form (e.g. {@link #resourcesToURLArray(String [])}) are equivalent,
 * except that they take multiple DML file locations.</li>
 *
 * </ul>
 * 
 * Note the use of the double
 * 
 * <pre>
 * { {} }
 * </pre>
 * 
 * to delimit an instance initializer block for the anonymous inner class being
 * created.
 * 
 * Each of the parameters of the <code>Config</code> class is represented as a
 * protected class field. Look at the documentation of each field to see what is
 * it for, whether it is optional or required, and in the former case, what is
 * its default value.
 * 
 * @see pt.ist.fenixframework.FenixFramework
 * @see pt.ist.fenixframework.core.DefaultConfig
 * 
 */
public abstract class Config {

    static final String PROPERTY_CONFIG_CLASS = "config.class";
    static final String DEFAULT_CONFIG_CLASS_NAME = "pt.ist.fenixframework.core.DefaultConfig";
    // the suffix of the method that sets a property from a String property
    static final String SETTER_FROM_STRING = "FromString";

    /**
     * This <strong>required</strong> parameter specifies the <code>URL[]</code> to each file
     * containing the DML code that corresponds to the domain model of the application. A non-empty
     * array must be specified for this parameter.
     */
    protected URL[] domainModelURLs = null;

    /**
     * This <strong>optional</strong> parameter specifies a name for the application that will be used by the framework in the
     * statistical logs performed during the application execution. The default value for this parameter is <code>null</code>.
     * Additionally, when using configuration by convention, this is the name used to lookup the
     * <code>&lt;appName&gt;/project.properties</code> file.
     */
    protected String appName = null;

    protected void checkRequired(Object obj, String fieldName) {
	if (obj == null) {
	    missingRequired(fieldName);
	}
    }

    /**
     * Subclasses of this class can overwrite this method, but they should specifically call
     * <code>super()</code> to check the superclass's configuration.
     */
    protected void checkConfig() {
	if ((domainModelURLs == null) || (domainModelURLs.length == 0)) {
	    missingRequired("domainModelURLs");
	}
    }
    
    /**
     * This method is invoked by the <code>FenixFramework.initialize(Config)</code>
     */
    final void initialize() {
        init();
        checkConfig();
    }

    // set each property via reflection, ignoring the config.class property, which was used to
    // define which config instance to create
    final void populate(Properties props) {
        for (String propName : props.stringPropertyNames()) {
            if (PROPERTY_CONFIG_CLASS.equals(propName)) { continue; }
            String value = props.getProperty(propName);
            setProperty(propName, value);
        }
    }

    final void setProperty(String propName, String value) {
        // first check if it really exists
        Field field = getField(this.getClass(), propName);

        // note that the OR lazy evaluation is used on purpose!
        boolean success = attemptSetPropertyUsingMethod(getSetterFor(this.getClass(), propName + SETTER_FROM_STRING), value)
            || attemptSetPropertyUsingField(field, value);

        if (! success) {
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
                return getField((Class<? extends Config>)superclass, fieldName);
            } else {
                throw new ConfigError(ConfigError.UNKNOWN_PROPERTY, e);
            }
        }
    }

    private boolean attemptSetPropertyUsingMethod(Method setter, String value) {
        if (setter == null) return false;
        
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
            return clazz.getDeclaredMethod(setterName, new Class[]{String.class});
        } catch (NoSuchMethodException e) {
            // climb the hierarchy, but only up to Config class
            Class<?> superclass = clazz.getSuperclass();
            if (Config.class.isAssignableFrom(superclass)) {
                return getSetterFor((Class<? extends Config>)superclass, setterName);
            } else {
                return null;
            }
        }
    }

    protected void appNameFromString(String value) {
        this.appName = value;
        // TO DO: also run the code to process <appName>/project.properties and thus fill the domainModelURLs
    }

    private void domainModelURLsFromString(String value) {
        String[] tokens = value.split("\\s*,\\s*");
        URL[] urls = new URL[tokens.length];

        for (int i = 0; i < tokens.length; i++) {
            // try URL from resource
            urls[i] = Converter.resourceToURL(tokens[i]);
            if (urls[i] != null) { continue; }

            // try URL from filename
            urls[i] = Converter.filenameToURL(tokens[i]);
            if (urls[i] != null) { continue; }

            // failed to get the URL
            throw new Error("FenixFramework config error: cannot find DML '" + tokens[i] + "'");
        }
        domainModelURLs = urls;
    }

    public URL[] getDomainModelURLs() {
	return domainModelURLs;
    }

    protected abstract void init();
    protected abstract BackEnd getBackEnd();

    public String getAppName() {
	return appName;
    }


    protected static void missingRequired(String fieldName) {
	throw new ConfigError(ConfigError.MISSING_REQUIRED_FIELD, "'" + fieldName + "'");
    }
    
    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - BEGIN */
    /* code linked from pt.ist.fenixframework.util.Converter to make configuration API more straightforward */

    // REGARDING RESOURCES

    public static URL[] resourceToURLArray(String resource) {
        return Converter.resourceToURLArray(resource);
    }

    public static URL[] resourcesToURLArray(String [] resources) {
        return Converter.resourcesToURLArray(resources);
    }

    // REGARDING FILENAMES

    public static URL[] filenameToURLArray(String filename) {
        return Converter.filenameToURLArray(filename);
    }

    public static URL[] filenamesToURLArray(String [] filenames) {
        return Converter.filenamesToURLArray(filenames);
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - END */
}
