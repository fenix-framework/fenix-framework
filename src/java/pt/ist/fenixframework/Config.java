package pt.ist.fenixframework;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * An instance of the <code>Config</code> class bundles together the
 * initialization parameters used by the Fenix Framework.  Therefore,
 * before initializing the framework (via the call to the
 * <code>FenixFramework.initialize(Config)</code> method), the
 * programmer should create an instance of <code>Config</code> with
 * the correct values for each of the parameters.
 * 
 * No constructor is provided for this class (other than the default
 * constructor), because the <code>Config</code> class has several
 * parameters, some of which are optional.  But, whereas optional
 * parameters do not need to be specified, the parameters that are
 * required must be specified by the programmer before calling the
 * <code>FenixFramework.initialize</code> method.
 *
 * To create an instance of this class with the proper values for its
 * parameters, programmers should generally use code like this:
 *
 * <blockquote>
 * <pre>
 * 
 * Config config = new Config() {{
 *   this.appName = "MyAppName";
 *   this.domainModelPath = "path/to/domain.dml";
 *   this.dbAlias = "//somehost:3306/databaseName";
 *   this.dbUsername = "dbuser";
 *   this.dbPassword = "dpPass";
 * }};
 *
 * </pre>
 * </blockquote>
 *
 * Note the use of the double <pre>{{ }}</pre> to delimit an instance
 * initializer block for the anonymous inner class being created.
 * 
 * Each of the parameters of the <code>Config</code> class is
 * represented as a protected class field.  Look at the documentation
 * of each field to see what is it for, whether it is optional or
 * required, and in the former case, what is its default value.
 *
 * The current set of required parameters are the following:
 * <ul>
 * <li>domainModelPath</li>
 * <li>dbAlias</li>
 * <li>dbUsername</li>
 * <li>dbPassword</li>
 * </ul>
 */
public class Config {

    /**
     * This <strong>required</strong> parameter specifies the path to
     * the file (or resource) containing the DML code that corresponds
     * to the domain model of the application.  A non-null value
     * must be specified for this parameter.
     */
    protected String[] domainModelPaths = null;

    /**
     * This <strong>required</strong> parameter specifies the JDBC
     * alias that will be used to access the database where domain
     * entities are stored.  The value of this parameter should not
     * contain neither the protocol, nor the sub-protocol, but may
     * contain any parameters that configure the connection to the
     * MySQL database (e.g., a possible value for this parameter is
     * "//localhost:3306/mydb?useUnicode=true&amp;characterEncoding=latin1").
     * A non-null value must be specified for this parameter.
     */
    protected String dbAlias = null;

    /**
     * This <strong>required</strong> parameter specifies the username
     * that will be used to access the database where domain entities
     * are stored.  A non-null value must be specified for this
     * parameter.
     */
    protected String dbUsername = null;

    /**
     * This <strong>required</strong> parameter specifies the password
     * that will be used to access the database where domain entities
     * are stored.  A non-null value must be specified for this
     * parameter.
     */
    protected String dbPassword = null;

    /**
     * This <strong>optional</strong> parameter specifies a name for
     * the application that will be used by the framework in the
     * statistical logs performed during the application execution.
     * The default value for this parameter is <code>null</code>.
     */
    protected String appName = null;

    /**
     * This <strong>optional</strong> parameter specifies whether an
     * error should be thrown if during a transaction an object that
     * was deleted during the transaction is subsequently changed.
     * The default value of <code>true</code> will cause an
     * <code>Error</code> to be thrown, whereas a value of
     * <code>false</code> will cause only a warning to be issued.
     */
    protected boolean errorIfChangingDeletedObject = true;

    /**
     * This <strong>optional</strong> parameter specifies whether the
     * framework should initialize the persistent store if it detects
     * that the store was not properly initialized (e.g., the database
     * has no tables).  The default value for this parameter is
     * <code>true</code>, but a programmer may want to specify a value
     * of <code>false</code>, if she wants to have control over the
     * initialization of the persistent store.  In this latter case,
     * however, if the store is not properly initialized before
     * initializing the framework, probably a runtime exception will
     * be thrown during the framework initialization.
     */
    protected boolean initializeStoreIfNeeded = true;

    private static void checkRequired(Object obj, String fieldName) {
        if (obj == null) {
            throw new Error("The required field '" + fieldName + "' was not specified in the FenixFramework config.");
        }
    }

    public void checkConfig() {
        checkRequired(domainModelPaths, "domainModelPaths");
        checkRequired(dbAlias, "dbAlias");
        checkRequired(dbUsername, "dbUsername");
        checkRequired(dbPassword, "dbPassword");
    }

    public String[] getDomainModelPaths() {
        return domainModelPaths;
    }

    public List<URL> getDomainModelURLs() {
	final List<URL> urls = new ArrayList<URL>();
	for (final String domainModelPath : domainModelPaths) {
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

    public boolean getInitializeStoreIfNeeded() {
        return initializeStoreIfNeeded;
    }
}
