package pt.ist.fenixframework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.pstm.dml.FenixDomainModelWithOCC;
import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

/**
 * An instance of the <code>Config</code> class bundles together the
 * initialization parameters used by the Fenix Framework. Therefore, before
 * initializing the framework (via the call to the
 * <code>FenixFramework.initialize(Config)</code> method), the programmer should
 * create an instance of <code>Config</code> with the correct values for each of
 * the parameters.
 * 
 * No constructor is provided for this class (other than the default
 * constructor), because the <code>Config</code> class has several parameters,
 * some of which are optional. But, whereas optional parameters do not need to
 * be specified, the parameters that are required must be specified by the
 * programmer before calling the <code>FenixFramework.initialize</code> method.
 * 
 * To create an instance of this class with the proper values for its
 * parameters, programmers should generally use code like this:
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * Config config = new Config() {
 *     {
 * 	this.appName = &quot;MyAppName&quot;;
 * 	this.domainModelPath = &quot;path/to/domain.dml&quot;;
 * 	this.dbAlias = &quot;//somehost:3306/databaseName&quot;;
 * 	this.dbUsername = &quot;dbuser&quot;;
 * 	this.dbPassword = &quot;dpPass&quot;;
 *     }
 * };
 * 
 * </pre>
 * 
 * </blockquote>
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
 * The current set of required parameters are the following:
 * <ul>
 * <li>domainModelPath XOR domainModelPaths</li>
 * <li>dbAlias</li>
 * <li>dbUsername</li>
 * <li>dbPassword</li>
 * </ul>
 */
public class Config {

    /**
     * This <strong>required</strong> parameter specifies the path to the file
     * (or resource) containing the DML code that corresponds to the domain
     * model of the application. A non-null value must be specified for this
     * parameter (or else, to the domainModelPaths, in case more than one DML
     * file is used by the project).
     */
    protected String domainModelPath = null;

    /**
     * This <strong>required</strong> parameter specifies the path to each file
     * (or resource) containing the DML code that corresponds to the domain
     * model of the application. A non-null value must be specified for this
     * parameter (or else, to the domainModelPath parameter).
     */
    protected String[] domainModelPaths = null;

    /**
     * This <strong>required</strong> parameter specifies the JDBC alias that
     * will be used to access the database where domain entities are stored. The
     * value of this parameter should not contain neither the protocol, nor the
     * sub-protocol, but may contain any parameters that configure the
     * connection to the MySQL database (e.g., a possible value for this
     * parameter is
     * "//localhost:3306/mydb?useUnicode=true&amp;characterEncoding=latin1"). A
     * non-null value must be specified for this parameter.
     */
    protected String dbAlias = null;

    /**
     * This <strong>required</strong> parameter specifies the username that will
     * be used to access the database where domain entities are stored. A
     * non-null value must be specified for this parameter.
     */
    protected String dbUsername = null;

    /**
     * This <strong>required</strong> parameter specifies the password that will
     * be used to access the database where domain entities are stored. A
     * non-null value must be specified for this parameter.
     */
    protected String dbPassword = null;

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
     * This <strong>optional</strong> parameter specifies whether the framework
     * should initialize the persistent store if it detects that the store was
     * not properly initialized (e.g., the database has no tables). The default
     * value for this parameter is <code>true</code>, but a programmer may want
     * to specify a value of <code>false</code>, if she wants to have control
     * over the initialization of the persistent store. In this latter case,
     * however, if the store is not properly initialized before initializing the
     * framework, probably a runtime exception will be thrown during the
     * framework initialization.
     */
    protected boolean createRepositoryStructureIfNotExists = true;

    /**
     * This <strong>optional</strong> parameter indicates whether the database
     * structure should be automatically updated with missing structure entries
     * when the framework is initialized. Defaults to false. This is only
     * relevant if some database structure already exists.
     */
    protected boolean updateRepositoryStructureIfNeeded = false;

    /**
     * This <strong>optional</strong> parameter indicates the class of the Root
     * object. If specified, the framework will initialize and persist an
     * instance of this class whenever the application starts, if one does not
     * exist already. Moreover, after initilization, the persistent instance of
     * this class may be accessed using the FenixFramework.getRoot() method.
     */
    protected Class rootClass = null;

    /**
     * This <strong>optional</strong> parameter indicates which subclass of
     * FenixDomainModel should be created by the DmlCompiler when parsing a DML
     * file.
     */
    protected Class<? extends FenixDomainModel> domainModelClass = FenixDomainModelWithOCC.class;

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

    /**
     * This <strong>optional</strong> parameter indicates whether the framework
     * will load on startup additional plugins.
     */
    protected FenixFrameworkPlugin[] plugins;

    private static void checkRequired(Object obj, String fieldName) {
	if (obj == null) {
	    missingRequired(fieldName);
	}
    }

    private static void missingRequired(String fieldName) {
	throw new Error("The required field '" + fieldName + "' was not specified in the FenixFramework config.");
    }

    private void insertConnectionEncoding(String encoding) {
	StringBuilder encodingParams = new StringBuilder();
	encodingParams.append("useUnicode=true&characterEncoding=" + encoding + "&clobCharacterEncoding=" + encoding
		+ "&characterSetResults=" + encoding);

	int questionMarkIndex = this.dbAlias.indexOf('?');

	if (questionMarkIndex == -1) { // no parameters yet
	    this.dbAlias = this.dbAlias + '?' + encodingParams;
	} else {
	    String prefix = this.dbAlias.substring(0, questionMarkIndex + 1); // include
	    // the
	    // question
	    // mark
	    String rest = this.dbAlias.substring(questionMarkIndex + 1);

	    this.dbAlias = prefix + encodingParams + '&' + rest;
	}
    }

    public void checkConfig() {
	if ((domainModelPath == null) && (domainModelPaths == null)) {
	    missingRequired("domainModelPath or domainModelPaths");
	}

	if ((domainModelPath != null) && (domainModelPaths != null)) {
	    throw new Error("It is not possible to specify both the 'domainModelPath' "
		    + "and 'domainModelPaths' parameters in the FenixFramework config.");
	}

	checkRequired(dbAlias, "dbAlias");
	checkRequired(dbUsername, "dbUsername");
	checkRequired(dbPassword, "dbPassword");
	checkRequired(domainModelClass, "domainModelClass");
	insertConnectionEncoding("UTF-8");
    }

    public String[] getDomainModelPaths() {
	// either domainModelPaths or domainModelPath is null
	return (domainModelPath != null) ? new String[] { domainModelPath } : domainModelPaths;
    }

    public List<URL> getDomainModelURLs() {
	final List<URL> urls = new ArrayList<URL>();
	for (final String domainModelPath : getDomainModelPaths()) {
	    URL url = this.getClass().getResource(domainModelPath);
	    if (url == null) {
		try {
		    url = new File(domainModelPath).toURI().toURL();
		} catch (MalformedURLException mue) {
		    throw new Error("FenixFramework config error: wrong domainModelPath '" + domainModelPath + "'");
		}
	    }
	    urls.add(url);
	}
	return urls;
    }

    public String getDbAlias() {
	return dbAlias;
    }

    public String getDbUsername() {
	return dbUsername;
    }

    public String getDbPassword() {
	return dbPassword;
    }

    public String getAppName() {
	return appName;
    }

    public boolean isErrorIfChangingDeletedObject() {
	return errorIfChangingDeletedObject;
    }

    public boolean isErrorfIfDeletingObjectNotDisconnected() {
	return errorfIfDeletingObjectNotDisconnected;
    }

    public boolean getCreateRepositoryStructureIfNotExists() {
	return createRepositoryStructureIfNotExists;
    }

    public boolean getUpdateRepositoryStructureIfNeeded() {
	return updateRepositoryStructureIfNeeded;
    }

    public Class getRootClass() {
	return rootClass;
    }

    public boolean getCollectDataAccessPatterns() {
	return collectDataAccessPatterns;
    }

    public Class<? extends FenixDomainModel> getDomainModelClass() {
	return domainModelClass;
    }

    public FenixFrameworkPlugin[] getPlugins() {
	return plugins;
    }

    public String getCollectDataAccessPatternsPath() {
	return collectDataAccessPatternsPath;
    }

}
