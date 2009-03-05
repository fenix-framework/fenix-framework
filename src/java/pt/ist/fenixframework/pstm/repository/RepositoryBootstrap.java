package pt.ist.fenixframework.pstm.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import pt.ist.fenixframework.Config;

/** This class is used when the fenix-framework starts up.  It is responsible for initializing or updating the repository
 * structure if needed.  There are two optional configuration parameters that govern the behaviour of this class:
 * <code>createRepositoryStructureIfNonExistent</code> and <code>updateRepositoryStructureIfNeeded</code>.  The former defaults to
 * <code>true</code> and controls whether to create the repository structure if it doesn't exist.  The latter defaults to
 * <code>false</code> and controls whether an existing repository structure should be updated to match changes in the domain
 * classes.
 */
public class RepositoryBootstrap {

    final Config config;

    public RepositoryBootstrap(Config config) {
	this.config = config;
    }

    public void updateDataRepositoryStructureIfNeeded() {
	if (config.getCreateRepositoryStructureIfNotExists() || config.getUpdateRepositoryStructureIfNeeded()) {
	    Connection connection = null;
	    try {
		connection = getConnection();

		Statement statement = null;
		ResultSet resultSet = null;
		try {
		    statement = connection.createStatement();
		    resultSet = statement.executeQuery("SELECT GET_LOCK('FenixFrameworkInit', 100)");
		    if (!resultSet.next() || (resultSet.getInt(1) != 1)) {
			return;
		    }
		} finally {
		    if (resultSet != null) {
			resultSet.close();
		    }
		    if (statement != null) {
			statement.close();
		    }
		}

		try {
		    boolean newInfrastructureCreated = false;
		    if (!infrastructureExists(connection) && config.getCreateRepositoryStructureIfNotExists()) {
			createInfrastructure(connection);
			newInfrastructureCreated = true;
		    }
		    if (newInfrastructureCreated || config.getUpdateRepositoryStructureIfNeeded()) {
			final String updates = SQLUpdateGenerator.generateInMem(connection, null);
			executeSqlInstructions(connection, updates);
		    }
		} finally {
		    Statement statementUnlock = null;
		    try {
			statementUnlock = connection.createStatement();
			statementUnlock.executeUpdate("DO RELEASE_LOCK('FenixFrameworkInit')");
		    } finally {
			if (statementUnlock != null) {
			    statementUnlock.close();
			}
		    }
		}

		connection.commit();
	    } catch (Exception ex) {
		ex.printStackTrace();
	    } finally {
		if (connection != null) {
		    try {
			connection.close();
		    } catch (SQLException e) {
			// nothing can be done.
		    }
		}
	    }
	}
    }

    private void executeSqlInstructions(final Connection connection, final String sqlInstructions) throws IOException, SQLException {
	for (final String instruction : sqlInstructions.split(";")) {
	    final String trimmed = instruction.trim();
	    if (trimmed.length() > 0) {
		Statement statement = null;
		try {
		    statement = connection.createStatement();
		    statement.execute(instruction);
		} finally {
		    if (statement != null) {
			statement.close();
		    }
		}
	    }
	}
    }
 
    private void executeSqlStream(final Connection connection, final String streamName) throws IOException, SQLException {
	final InputStream inputStream = RepositoryBootstrap.class.getResourceAsStream(streamName);
	final String sqlInstructions = readFile(new InputStreamReader(inputStream));
	executeSqlInstructions(connection, sqlInstructions);
    }

   private Connection getConnection() throws ClassNotFoundException, SQLException {
	final String driverName = "com.mysql.jdbc.Driver";
	Class.forName(driverName);
	final String url = "jdbc:mysql:" + config.getDbAlias();
	final Connection connection = DriverManager.getConnection(url, config.getDbUsername(), config.getDbPassword());
	connection.setAutoCommit(false);
	return connection;
    }
    
    private void createInfrastructure(final Connection connection) throws SQLException, IOException {
	executeSqlStream(connection, "/transactional-system-ddl.sql");
	executeSqlStream(connection, "/ojb-ddl.sql");
    }

    private boolean infrastructureExists(final Connection connection) throws SQLException {
	final DatabaseMetaData databaseMetaData = connection.getMetaData();
	ResultSet resultSet = null;
	try {
	    final String dbName = connection.getCatalog();
	    resultSet = databaseMetaData.getTables(dbName, "", "TX_CHANGE_LOGS", new String[] {"TABLE"});

	    while (resultSet.next()) {
		final String tableName = resultSet.getString(3);
		if (tableName.equals("TX_CHANGE_LOGS")) {
		    return true;
		}
	    }
	    return false;
	} finally {
	    if (resultSet != null) {
		resultSet.close();
	    }
	}
    }
 
    private String readFile(final InputStreamReader fileReader) throws IOException {
	try {
	    char[] buffer = new char[4096];
	    final StringBuilder fileContents = new StringBuilder();
	    for (int n = 0; (n = fileReader.read(buffer)) != -1; fileContents.append(buffer, 0, n))
		;
	    return fileContents.toString();
	} finally {
	    fileReader.close();
	}
    }
  

}
