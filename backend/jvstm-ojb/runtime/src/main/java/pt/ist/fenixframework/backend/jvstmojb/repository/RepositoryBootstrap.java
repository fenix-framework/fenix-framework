package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;

/**
 * This class is used when the fenix-framework starts up. It is responsible for initializing or updating the repository
 * structure if needed. There are two optional configuration parameters that govern the behaviour of this class:
 * <ul>
 * <li><code>createRepositoryStructureIfNonExistent</code> - controls whether to create the repository structure if it doesn't
 * exist (default = true)</li>
 * <li><code>updateRepositoryStructureIfNeeded</code> - controls whether an existing repository structure should be updated to
 * match changes in the domain classes(default = false)</li>
 * </ul>
 * 
 * @See {@link JvstmOJBConfig}
 */
public class RepositoryBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryBootstrap.class);

    public static boolean updateDataRepositoryStructureIfNeeded(JvstmOJBConfig config, Connection connection) {
        try {
            boolean infrastructureExists = infrastructureExists(connection);
            if (!infrastructureExists) {
                logger.info("Updating Repository Infrastructure");
                createInfrastructure(connection);
            }
            if (!infrastructureExists || config.getUpdateRepositoryStructureIfNeeded()) {
                logger.info("Updating Repository Structure");
                final String updates =
                        SQLUpdateGenerator.generateSqlUpdates(FenixFramework.getDomainModel(), connection, null, false);
                executeSqlInstructions(connection, updates);
                logger.info("Repository Structure update completed");
            }
            return !infrastructureExists;
        } catch (SQLException | IOException ex) {
            logger.error("Could not update data repository", ex);
            throw new Error(ex);
        }
    }

    private static void executeSqlInstructions(final Connection connection, final String sqlInstructions) throws SQLException {
        for (final String instruction : sqlInstructions.split(";")) {
            final String trimmed = instruction.trim();
            if (trimmed.length() > 0) {
                try (Statement statement = connection.createStatement()) {
                    logger.trace("Executing SQL instruction: '{}'", instruction);
                    statement.execute(instruction);
                }
            }
        }
    }

    private static void executeSqlStream(final Connection connection, final String streamName) throws IOException, SQLException {
        final InputStream inputStream = RepositoryBootstrap.class.getResourceAsStream(streamName);
        final String sqlInstructions = readFile(new InputStreamReader(inputStream));
        executeSqlInstructions(connection, sqlInstructions);
    }

    private static void createInfrastructure(final Connection connection) throws SQLException, IOException {
        executeSqlStream(connection, "/transactional-system-ddl.sql");
        executeSqlStream(connection, "/ojb-ddl.sql");
    }

    private static boolean infrastructureExists(final Connection connection) throws SQLException {
        return tableExists(connection, "FF$TX_CHANGE_LOGS");
    }

    private static boolean tableExists(final Connection connection, String tableName) throws SQLException {
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

    private static String readFile(final InputStreamReader fileReader) throws IOException {
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

}
