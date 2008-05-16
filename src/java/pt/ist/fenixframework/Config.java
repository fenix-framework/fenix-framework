package pt.ist.fenixframework;

/**
 * An instance of the Config class is needed to initialize the FenixFramework.
 * No constructor is provided for this class, because the Config class has numerous options, many of which are optional.
 * Therefore, the intended use for this class is to create an instance of an anonymous class that initializes in an instance initializer block all the parameters needed.
 * For instance, something like this:
 * 
 * Config config = new Config() {{
 *   this.appName = "MyAppName";
 *   this.domainModelPath = "path/to/domain.dml";
 *   this.dbAlias = "//somehost:3306/databaseName";
 *   this.dbUsername = "dbuser";
 *   this.dbPassword = "dpPass";
 * }};
 *
 * Note the use of the double {{ }}.
 * 
 * Some of the Config options are mandatory.  Therefore, if those options are not setup correctly before calling the FenixFramework.initialize(Config) method, an Error will occur during the call to this method.
 * The current set of required options are the following:
 * - domainModelPath
 * - dbAlias
 * - dbUsername
 * - dbPassword
 */
public class Config {

    /**
     * The path to the file (or resource) containing the DML code that corresponds to the domain model.
     */
    protected String domainModelPath = null;
    protected String dbAlias = null;
    protected String dbUsername = null;
    protected String dbPassword = null;
    protected String appName = null;
    protected boolean errorIfChangingDeletedObject = true;

    private static void checkRequired(Object obj, String fieldName) {
        if (obj == null) {
            throw new Error("The required field '" + fieldName + "' was not specified in the FenixFramework config.");
        }
    }

    public void checkConfig() {
        checkRequired(domainModelPath, "domainModelPath");
        checkRequired(dbAlias, "dbAlias");
        checkRequired(dbUsername, "dbUsername");
        checkRequired(dbPassword, "dbPassword");
    }

    public String getDomainModelPath() {
        return domainModelPath;
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
}
