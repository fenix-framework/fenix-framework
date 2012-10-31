package pt.ist.fenixframework.project.persistence;

import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import pt.ist.fenixframework.project.persistence.exception.PersistenceInfoException;

public class MySQLPersistenceInfo extends PersistenceInfo {

    private static final String DATABASE_KEY = "db";
    private static final String USERNAME_KEY = "db-username";
    private static final String PASSWORD_KEY = "db-password";
    private static final String HOSTNAME_KEY = "db-hostname";
    private static final String PORT_KEY = "db-port";

    private static final String DEFAULT_HOSTNAME = "localhost";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final int DEFAULT_PORT = 3306;

    private String database;
    private String hostname = DEFAULT_HOSTNAME;
    private String username = DEFAULT_USERNAME;
    private String password = DEFAULT_PASSWORD;
    private int port = DEFAULT_PORT;

    public String getUsername() {
	return username;
    }

    public String getPassword() {
	return password;
    }

    public String getConnectionURL() {
	return "//"+hostname+":"+port+"/"+database;
    }

    public boolean isEngine(PersistenceEngine persistenceEngine) {
	return persistenceEngine != null && persistenceEngine.equals(PersistenceEngine.MySQL);
    }

    public MySQLPersistenceInfo withDatabase(String database) {
	this.database = database;
	return this;
    }

    public MySQLPersistenceInfo withUsername(String username) {
	if(!StringUtils.isBlank(username)) {
	    this.username = username;
	}
	return this;
    }

    public MySQLPersistenceInfo withPassword(String password) {
	if(!StringUtils.isBlank(password)) {
	    this.password = password;
	}
	return this;
    }

    public MySQLPersistenceInfo withHostname(String hostname) {
	if(!StringUtils.isBlank(hostname)) {
	    this.hostname = hostname;
	}
	return this;
    }

    public MySQLPersistenceInfo withPort(String port) {
	if(!StringUtils.isBlank(port)) {
	    this.port = Integer.parseInt(port);
	}
	return this;
    }

    protected static MySQLPersistenceInfo load(Properties properties) throws PersistenceInfoException {
	MySQLPersistenceInfo persistenceInfo = new MySQLPersistenceInfo()
		.withDatabase(properties.getProperty(DATABASE_KEY))
		.withUsername(properties.getProperty(USERNAME_KEY))
		.withPassword(properties.getProperty(PASSWORD_KEY))
		.withHostname(properties.getProperty(HOSTNAME_KEY))
		.withPort(properties.getProperty(PORT_KEY));
	persistenceInfo.validate();
	return persistenceInfo;
    }

    public void validate() throws PersistenceInfoException {
	StringBuilder errorMessageBuilder = new StringBuilder();
	if(StringUtils.isBlank(database))
	    errorMessageBuilder.append("Missing parameter: "+DATABASE_KEY+"\n");
	if(errorMessageBuilder.length() > 0) {
	    throw new PersistenceInfoException(errorMessageBuilder.toString());
	}
    }

}
