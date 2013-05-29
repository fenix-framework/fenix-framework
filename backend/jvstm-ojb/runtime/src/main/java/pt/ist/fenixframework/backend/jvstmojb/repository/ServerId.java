package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstmojb.pstm.Util;
import pt.ist.fenixframework.util.FenixFrameworkThread;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class ServerId {

    private static final Logger logger = LoggerFactory.getLogger(ServerId.class);

    private static int serverId = -1;

    private static final long SERVER_ID_LEASE_SECS = 600;

    public static void ensureServerId() {
        Connection connection = DbUtil.getNewConnection();

        try {
            boolean done = false;
            while (!done) {
                Statement query = connection.createStatement();
                ResultSet rs =
                        query.executeQuery("select ID from FF$SERVER_ID_LEASE"
                                + " where EXPIRATION is null or EXPIRATION < now() order by EXPIRATION limit 1;");

                String updateStr;
                // leases not refreshed since leaseLimit will be considered available           
                if (rs.next()) {
                    serverId = rs.getInt("ID");
                    updateStr =
                            "update FF$SERVER_ID_LEASE set EXPIRATION = now() + interval 20 minute, SERVER = '"
                                    + Util.getServerName() + "' where ID = " + serverId;
                } else { // No available record, search for Max
                    ResultSet maxRs = query.executeQuery("select max(ID) from FF$SERVER_ID_LEASE");
                    maxRs.next();
                    serverId = maxRs.getInt(1) + 1;
                    updateStr =
                            "INSERT INTO FF$SERVER_ID_LEASE (ID, SERVER, EXPIRATION) VALUES (" + serverId + ", '"
                                    + Util.getServerName() + "', now() + interval 20 minute)";
                    maxRs.close();
                }

                rs.close();

                Statement update = connection.createStatement();
                try {
                    update.executeUpdate(updateStr);
                    done = true;
                    connection.commit();
                } catch (MySQLIntegrityConstraintViolationException ex) {
                    logger.warn("Server Id conflict, retrying");
                }
                query.close();
                update.close();
            }

            logger.info("Obtained server id: {}", serverId);
        } catch (SQLException e) {
            throw new Error("Could not obtain server id!", e);
        }

        new ServerIdRenewalThread(connection).start();
    }

    public static int getServerId() {
        if (serverId == -1) {
            throw new RuntimeException("Cannot call getServerId before ensureServerId is called");
        }
        return serverId;
    }

    private static class ServerIdRenewalThread extends FenixFrameworkThread {

        private final Connection connection;

        private long interval = SERVER_ID_LEASE_SECS * 1000;

        public ServerIdRenewalThread(Connection connection) {
            super("ServerIdRenewalThread");
            this.connection = connection;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(interval);
                    renewLease();
                    interval = SERVER_ID_LEASE_SECS * 1000;
                } catch (InterruptedException e) {
                    return;
                } catch (SQLException e) {
                    interval = 10 * 1000;
                    logger.warn("Cannot renew server lease, retrying in 10 seconds", e);
                }
            }
        }

        private void renewLease() throws SQLException {
            logger.info("Renewing server id lease.");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("update FF$SERVER_ID_LEASE set EXPIRATION = now() + interval 20 minute where ID = " + serverId);
            connection.commit();
            stmt.close();
        }

        private void clearLease() throws SQLException {
            logger.info("Clearing server id lease.");
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("update FF$SERVER_ID_LEASE set EXPIRATION = null where ID = " + serverId);
            connection.commit();
            stmt.close();
        }

        @Override
        protected void shutdown() {
            try {
                clearLease();
                connection.close();
            } catch (SQLException e) {
                logger.warn("Exception while closing connection", e);
            }
            super.shutdown();
        }

    }

}
