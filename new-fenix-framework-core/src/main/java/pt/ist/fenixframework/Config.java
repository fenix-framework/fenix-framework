package pt.ist.fenixframework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.core.ConfigurationExtension;
import pt.ist.fenixframework.core.CoreConfigurationExtension;

/**
 * <p> An instance of the <code>Config</code> class bundles together the initialization parameters
 * used by the Fenix Framework. Therefore, before initializing the framework (via the call to the
 * <code>FenixFramework.initialize(Config)</code> method), the programmer should create an instance
 * of <code>Config</code> with the correct values for each of the parameters.
 * 
 * <p> No constructor is provided for this class (other than the default constructor), because the
 * <code>Config</code> class has several parameters, some of which are optional. But, whereas
 * optional parameters do not need to be specified, the parameters that are required must be
 * specified by the programmer before calling the <code>FenixFramework.initialize</code> method.
 *
 * <p> Additional configuration parameters may be added by subclassing this class.  During the
 * invocation of <code>FenixFramework.initialize(Config)</code>, the
 * <code>ConfigurationExtension.initialize(Config)</code> method is invoked on the
 * <code>configurationExtensionClass</code> instance given in the <code>Config</code> (if none is
 * provided, the <code>CoreConfigurationExtension</code> is used).  This enables extensions to the
 * fenix-framework-core to add configuration parameters and to initialize themselves.
 * 
 * <p> To create an instance of this class with the proper values for its parameters, programmers should
 * generally use code like this:
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * Config config = new Config() {
 *     {
 * 	this.domainModelURLs = Config.resourceToURL(&quot;path/to/domain.dml&quot;);
 * 	this.appName = &quot;MyAppName&quot;; // this is optional
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
 * <li><code>Config.resourceToURL(String)</code>: looks up the given resource using
 * <code>Config().getResource(String)</code>;</li>
 *
 * <li><code>Config.filenameToURL(String)</code>: looks up the given file on the local filesystem;
 * </li>
 *
 * <li>Methods using the plural form (e.g. <code>Config.resourcesToURL(String [])</code>) are
 * equivalent, except that they take multiple DML file locations.</li>
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
 */
public class Config {

    /**
     * This <strong>required</strong> parameter specifies the <code>URL[]</code> to each file
     * containing the DML code that corresponds to the domain model of the application. A non-empty
     * array must be specified for this parameter.
     */
    protected URL[] domainModelURLs = null;

    /**
     * This <strong>optional</strong> parameter specifies the configuration extension to use in the
     * initialization of the backend-specific parameters.  The default value is
     * <code>CoreConfigurationExtension.class</code>.
     */
    protected Class<? extends ConfigurationExtension> configurationExtensionClass = CoreConfigurationExtension.class;
    /* The following field is set during config initialization by instantiating the
     * configurationExtensionClass. */
    private ConfigurationExtension configurationExtension;

    /**
     * This <strong>optional</strong> parameter specifies a name for the
     * application that will be used by the framework in the statistical logs
     * performed during the application execution. The default value for this
     * parameter is <code>null</code>.
     */
    protected String appName = null;

    /**
     * This <strong>optional</strong> parameter specifies whether an error
     * should be thrown if during a transaction an object that was deleted
     * during the transaction is subsequently changed. The default value of
     * <code>true</code> will cause an <code>Error</code> to be thrown, whereas
     * a value of <code>false</code> will cause only a warning to be issued.
     */
    protected boolean errorIfChangingDeletedObject = true;

    /**
     * This <strong>optional</strong> parameter indicates whether the framework
     * should collect information about the data-access patterns of the
     * application.
     */
    protected boolean collectDataAccessPatterns = false;

    /**
     * This <strong>optional</strong> parameter indicates where the framework
     * will store the collected information about the data-access patterns of
     * the application. Must end with a path separator character.
     */
    protected String collectDataAccessPatternsPath = "";

    /**
     * This <strong>optional</strong> parameter indicates whether the framework
     * should throw an exception when a DomainObject that is still connected to
     * other objects is trying to be deleted or rather delete it.
     */
    protected boolean errorfIfDeletingObjectNotDisconnected = false;

    private static void checkRequired(Object obj, String fieldName) {
	if (obj == null) {
	    missingRequired(fieldName);
	}
    }

    private static void missingRequired(String fieldName) {
	throw new Error("The required field '" + fieldName + "' was not specified in the FenixFramework config.");
    }
    
    /**
     * Subclasses of this class can overwrite this method, but they should specifically call
     * <code>super()</code> to check the superclass's configuration.
     */
    protected void checkConfig() {
	if ((domainModelURLs == null) || (domainModelURLs.length == 0)) {
	    missingRequired("domainModelURLs");
	}

	checkRequired(configurationExtensionClass, "configurationExtensionClass");
    }
    
    /**
     * This method is invoked by the <code>FenixFramework.initialize(Config)</code>
     */
    protected void initialize() {
        checkConfig();

        try {
            configurationExtension = configurationExtensionClass.newInstance();
            configurationExtension.initialize(this);
        } catch (Exception e) {
            throw new Error("Could not instantiante a ConfigurationExtension '" + configurationExtensionClass + "'. ", e);
        }
    }

    public URL[] getDomainModelURLs() {
	return domainModelURLs;
    }

    // public List<URL> getDomainModelURLs() {
    //     final List<URL> urls = new ArrayList<URL>();
    //     for (final String domainModelPath : getDomainModelPaths()) {
    //         URL url = this.getClass().getResource(domainModelPath);
    //         if (url == null) {
    //     	try {
    //     	    url = new File(domainModelPath).toURI().toURL();
    //     	} catch (MalformedURLException mue) {
    //     	    throw new Error("FenixFramework config error: wrong domainModelPath '" + domainModelPath + "'");
    //     	}
    //         }
    //         urls.add(url);
    //     }
    //     return urls;
    // }

    public String getAppName() {
	return appName;
    }

    public ConfigurationExtension getConfigExtension() {
        return configurationExtension;
    }

    public boolean isErrorIfChangingDeletedObject() {
	return errorIfChangingDeletedObject;
    }

    public boolean isErrorfIfDeletingObjectNotDisconnected() {
	return errorfIfDeletingObjectNotDisconnected;
    }

    public boolean getCollectDataAccessPatterns() {
	return collectDataAccessPatterns;
    }

    public String getCollectDataAccessPatternsPath() {
	return collectDataAccessPatternsPath;
    }

    /* UTILITY METHODS TO CONVERT DIFFERENT FORMATS TO URL - BEGIN */

    // REGARDING RESOURCES

    private static URL internalResourceToURL(String resource) {
        URL url = Config.class.getResource(resource);
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
