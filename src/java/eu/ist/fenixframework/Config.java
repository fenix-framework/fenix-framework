package eu.ist.fenixframework;

public class Config {

    protected String appName = null;
    protected boolean errorIfChangingDeletedObject = true;

    public String getAppName() {
        return appName;
    }

    public boolean isErrorIfChangingDeletedObject() {
        return errorIfChangingDeletedObject;
    }
}
