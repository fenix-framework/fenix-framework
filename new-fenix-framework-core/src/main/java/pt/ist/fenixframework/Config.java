package pt.ist.fenixframework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import pt.ist.fenixframework.core.Repository;

/**
 * <p> An instance of the <code>Config</code> class bundles together the initialization parameters
 * used by the Fenix Framework. Therefore, before initializing the framework (via the call to the
 * {@link FenixFramework#initialize(Config)} method), the programmer should create an instance of
 * <code>Config</code> with the correct values for each of the parameters.</p>
 * 
 * <p> No constructor is provided for this class (other than the default constructor), because the
 * <code>Config</code> class has several parameters, some of which are optional. But, whereas
 * optional parameters do not need to be specified, the parameters that are required must be
 * specified by the programmer before calling the <code>FenixFramework.initialize</code> method.
 *
 * <p> Additional configuration parameters may be added by subclassing this class.  Subclasses of
 * config can override the {@link init()} method.  Typically, their own <code>init()</code> should
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
 * 	this.domainModelURLs = Config.resourceToURL(&quot;path/to/domain.dml&quot;);
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
 * <li>{@link #resourceToURL(String)}: looks up the given resource using
 * <code>Thread.currentThread().getContextClassLoader().getResource(String)</code>;</li>
 *
 * <li>{@link #filenameToURL(String)}: looks up the given file on the local filesystem;
 * </li>
 *
 * <li>Methods using the plural form (e.g. {@link #resourcesToURL(String [])}) are equivalent,
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
 * @see pt.ist.fenixframework.core.DefaultConfig
 * 
 */
public abstract class Config {

    /**
     * This <strong>required</strong> parameter specifies the <code>URL[]</code> to each file
     * containing the DML code that corresponds to the domain model of the application. A non-empty
     * array must be specified for this parameter.
     */
    protected URL[] domainModelURLs = null;

    /**
     * This <strong>optional</strong> parameter specifies a name for the
     * application that will be used by the framework in the statistical logs
     * performed during the application execution. The default value for this
     * parameter is <code>null</code>.
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

    public URL[] getDomainModelURLs() {
	return domainModelURLs;
    }

    protected abstract void init();
    protected abstract <T extends DomainObject> T getDomainObject(String externalId);
    protected abstract Repository getRepository();
    protected abstract TransactionManager getTransactionManager();

    public String getAppName() {
	return appName;
    }

    protected static void missingRequired(String fieldName) {
	throw new ConfigError(ConfigError.MISSING_REQUIRED_FIELD, "'" + fieldName + "'");
    }
    
    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - BEGIN */

    // REGARDING RESOURCES

    private static URL internalResourceToURL(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new Error("FenixFramework config error: cannot find DML for resource '" + resource + "'");
        }
        return url;
    }

    public static URL[] resourceToURL(String resource) {
        return resourcesToURL(new String[]{resource});
    }

    public static URL[] resourcesToURL(String [] resources) {
        final URL[] urls = new URL[resources.length];
        for (int i = 0; i < resources.length; i++) {
            urls[i] = internalResourceToURL(resources[i]);
        }
        return urls;
    }

    // REGARDING FILENAMES

    private static URL internalFilenameToURL(String filename) {
        URL url = null;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                throw new Error("FenixFramework config error: cannot find DML for file'" + filename + "'");
            }

            url = file.toURI().toURL();
        } catch (MalformedURLException mue) {
            throw new Error("FenixFramework config error: cannot find DML for file'" + filename + "'");
        }
        return url;
    }

    public static URL[] filenameToURL(String filename) {
        return filenamesToURL(new String[]{filename});
    }

    public static URL[] filenamesToURL(String [] filenames) {
        final URL[] urls = new URL[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            urls[i] = internalFilenameToURL(filenames[i]);
        }
        return urls;
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - END */
}
