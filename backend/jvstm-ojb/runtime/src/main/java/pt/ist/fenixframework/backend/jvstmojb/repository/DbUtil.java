package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.cj.jdbc.NonRegisteringDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;

public class DbUtil {

    private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);

    private static final NonRegisteringDriver mysqlDriver;

    static {
        try {
            mysqlDriver = new NonRegisteringDriver();
            DriverManager.registerDriver(mysqlDriver);
        } catch (SQLException ex) {
            throw new Error(ex);
        }
    }

    public static void deregisterDriver() {
        try {
            DriverManager.deregisterDriver(mysqlDriver);
            logger.info("Successfully deregistered JDBC driver " + mysqlDriver);
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    public static String getFkName(String slotName) {
        return "OID_" + convertToDBStyle(slotName);
    }

    public static String convertToDBStyle(String string) {
        StringBuilder result = new StringBuilder(string.length() + 10);
        boolean first = true;
        for (char c : string.toCharArray()) {
            if (first) {
                first = false;
            } else if (Character.isUpperCase(c)) {
                result.append('_');
            }
            result.append(Character.toUpperCase(c));
        }

        return result.toString();
    }

    private static JvstmOJBConfig getConfig() {
        return FenixFramework.<JvstmOJBConfig> getConfig();
    }

    private static String getDBLockName() {
        return "FenixFrameworkInit." + getConfig().getDBName();
    }

    public static void runWithinDBLock(DBLockedCommand command) {
        Connection connection = null;
        try {
            connection = command.getConnection();

            Statement statementLock = null;
            ResultSet resultSet = null;
            try {
                int iterations = 0;
                while (true) {
                    iterations++;
                    statementLock = connection.createStatement();
                    resultSet = statementLock.executeQuery("SELECT GET_LOCK('" + getDBLockName() + "', 60)");
                    if (resultSet.next() && (resultSet.getInt(1) == 1)) {
                        break;
                    }
                    if ((iterations % 10) == 0) {
                        logger.warn("Could not yet obtain the " + getDBLockName() + " lock. Number of retries: " + iterations);
                    }
                }
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statementLock != null) {
                    statementLock.close();
                }
            }

            try {
                command.run();
            } finally {
                Statement statementUnlock = null;
                try {
                    statementUnlock = connection.createStatement();
                    statementUnlock.executeUpdate("DO RELEASE_LOCK('" + getDBLockName() + "')");
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
        }
    }

    /**
     * Opens a new Connection to the database,
     * wrapping any exception.
     * 
     * @see openConnection()
     * @return
     *         A new connection
     */
    static Connection getNewConnection() {
        try {
            return openConnection();
        } catch (SQLException ex) {
            throw new Error(ex);
        }
    }

    /**
     * Opens a NEW {@link Connection} to the database.
     * 
     * The caller must be responsible for manually closing
     * the Connection.
     * 
     * @return
     *         A new database Connection
     * @throws SQLException
     *             If it is not possible to establish a connection
     */
    static Connection openConnection() throws SQLException {
        final JvstmOJBConfig config = getConfig();
        final String url = "jdbc:mysql:" + config.getDbAlias();
        final Connection connection = DriverManager.getConnection(url, config.getDbUsername(), config.getDbPassword());
        connection.setAutoCommit(false);
        return connection;
    }

    static int getServerIdLeaseTime() {
        final JvstmOJBConfig config = getConfig();
        return config.getServerIdLeaseTime();
    }

    public static abstract class DBLockedCommand {

        public DBLockedCommand() {
            this.connection = getNewConnection();
        }

        public final Connection connection;

        public Connection getConnection() {
            return connection;
        }

        protected abstract void run();
    }
}
