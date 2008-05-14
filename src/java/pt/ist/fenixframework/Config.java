package pt.ist.fenixframework;

public class Config {

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
