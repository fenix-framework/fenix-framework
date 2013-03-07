package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;

/**
 * This class is used when the fenix-framework starts up. It is responsible for
 * initializing or updating the repository structure if needed. There are two
 * optional configuration parameters that govern the behaviour of this class: <code>createRepositoryStructureIfNonExistent</code>
 * and <code>updateRepositoryStructureIfNeeded</code>. The former defaults to <code>true</code> and controls whether to create the
 * repository structure if
 * it doesn't exist. The latter defaults to <code>false</code> and controls
 * whether an existing repository structure should be updated to match changes
 * in the domain classes.
 */
public class RepositoryBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryBootstrap.class);

    private final JvstmOJBConfig config;

    public RepositoryBootstrap(JvstmOJBConfig config) {
        this.config = config;
    }

    public static String getDbLockName() {
        return "FenixFrameworkInit." + FenixFramework.<JvstmOJBConfig> getConfig().getDbName();
    }

    public void updateDataRepositoryStructureIfNeeded() {
        Connection connection = null;
        try {
            connection = getConnection(config);

            Statement statement = null;
            ResultSet resultSet = null;
            try {
                int iterations = 0;
                while (true) {
                    iterations++;
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery("SELECT GET_LOCK('" + getDbLockName() + "', 60)");
                    if (resultSet.next() && (resultSet.getInt(1) == 1)) {
                        break;
                    }
                    if ((iterations % 10) == 0) {
                        logger.warn("Could not yet obtain the " + getDbLockName() + " lock. Number of retries: " + iterations);
                    }
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
                if (config.getCreateRepositoryStructureIfNotExists() || config.getUpdateRepositoryStructureIfNeeded()) {
                    boolean newInfrastructureCreated = false;
                    if (!infrastructureExists(connection) && config.getCreateRepositoryStructureIfNotExists()) {
                        logger.trace("Updating Repository Infrastructure");
                        if (infrastructureNeedsUpdate(connection)) {
                            updateInfrastructure(connection);
                        } else {
                            createInfrastructure(connection);
                            newInfrastructureCreated = true;
                        }
                    }
                    if (newInfrastructureCreated || config.getUpdateRepositoryStructureIfNeeded()) {
                        logger.trace("Updating Repository Structure");
                        final String updates =
                                SQLUpdateGenerator.generateSqlUpdates(FenixFramework.getDomainModel(), connection, null, false);
                        executeSqlInstructions(connection, updates);
                    }
                }
            } finally {
                Statement statementUnlock = null;
                try {
                    statementUnlock = connection.createStatement();
                    statementUnlock.executeUpdate("DO RELEASE_LOCK('" + getDbLockName() + "')");
                } finally {
                    if (statementUnlock != null) {
                        statementUnlock.close();
                    }
                }
            }

            connection.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Error(ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // nothing can be done.
                }
            }
            logger.trace("Repository Structure update completed");
        }
    }

    private void executeSqlInstructions(final Connection connection, final String sqlInstructions) throws SQLException {
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

    public static Connection getConnection(JvstmOJBConfig config) throws ClassNotFoundException, SQLException {
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
        return tableExists(connection, "FF$TX_CHANGE_LOGS");
    }

    private boolean tableExists(final Connection connection, String tableName) throws SQLException {
        final DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet resultSet = null;
        try {
            final String dbName = connection.getCatalog();
            resultSet = databaseMetaData.getTables(dbName, "", tableName, new String[] { "TABLE" });

            while (resultSet.next()) {
                final String existingTableName = resultSet.getString(3);
                // we need to use the equalsIgnoreCase here because on
                // MS Windows (at least), the name of the tables
                // change case
                if (tableName.equalsIgnoreCase(existingTableName)) {
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
            for (int n = 0; (n = fileReader.read(buffer)) != -1; fileContents.append(buffer, 0, n)) {
                ;
            }
            return fileContents.toString();
        } finally {
            fileReader.close();
        }
    }

    private boolean infrastructureNeedsUpdate(final Connection connection) throws SQLException {
        return tableExists(connection, "TX_CHANGE_LOGS");
    }

    private void updateInfrastructure(final Connection connection) throws SQLException, IOException {
        executeSqlStream(connection, "/rename-system-tables.sql");
    }
}
