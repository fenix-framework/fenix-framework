package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstmojb.pstm.Util;
import pt.ist.fenixframework.util.FenixFrameworkThread;

public class ServerId {

    private static final Logger logger = LoggerFactory.getLogger(ServerId.class);

    private static int serverId = -1;

    public static void ensureServerId() {
        try (Connection connection = DbUtil.openConnection()) {
            Statement query = connection.createStatement();
            ResultSet rs =
                    query.executeQuery("select ID from FF$SERVER_ID_LEASE"
                            + " where EXPIRATION is null or EXPIRATION < now() order by EXPIRATION limit 1 FOR UPDATE;");

            String updateStr;
            // leases not refreshed since leaseLimit will be considered available
            if (rs.next()) {
                serverId = rs.getInt("ID");
                updateStr =
                        "update FF$SERVER_ID_LEASE set EXPIRATION = now() + interval " + DbUtil.getServerIdLeaseTime()
                                + " minute, SERVER = '" + Util.getServerName() + "' where ID = " + serverId;
            } else { // No available record, search for Max
                try (ResultSet maxRs = query.executeQuery("select max(ID) from FF$SERVER_ID_LEASE")) {
                    maxRs.first();
                    serverId = maxRs.getInt(1) + 1;
                    updateStr =
                            "INSERT INTO FF$SERVER_ID_LEASE (ID, SERVER, EXPIRATION) VALUES (" + serverId + ", '"
                                    + Util.getServerName() + "', now() + interval " + DbUtil.getServerIdLeaseTime() + " minute)";
                }
            }

            rs.close();

            try (Statement update = connection.createStatement()) {
                update.executeUpdate(updateStr);
            }

            query.close();
            connection.commit();

            logger.info("Obtained server id: {}", serverId);
        } catch (SQLException e) {
            throw new Error("Could not obtain server id!", e);
        }

        new ServerIdRenewalThread().start();
    }

    public static int getServerId() {
        if (serverId == -1) {
            throw new RuntimeException("Server ID is not defined. Cannot create new objects!");
        }
        return serverId;
    }

    public static long getServerOidBase() {
        return (long) getServerId() << 48;
    }

    private static class ServerIdRenewalThread extends FenixFrameworkThread {

        private long interval;

        private DateTime validUntil;

        public ServerIdRenewalThread() {
            super("ServerIdRenewalThread");
            updateValidity();
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(interval);

                    /*
                     * If the lease has expired, the only safe thing to do
                     * is to shut down the application server.
                     *
                     */
                    if (validUntil.isBeforeNow()) {
                        serverId = -1;
                        logger.error(
                                "Server ID lease has expired! Was valid until {}. Object creation is not possible. "
                                        + "Shutting down the system",
                                validUntil);
                        System.exit(-1);
                    }

                    renewLease();
                    updateValidity();
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
            try (Connection connection = DbUtil.openConnection(); Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("update FF$SERVER_ID_LEASE set EXPIRATION = now() + interval "
				        + DbUtil.getServerIdLeaseTime() + " minute where ID = " + serverId);
                connection.commit();
            }
        }

        private void clearLease() throws SQLException {
            logger.info("Clearing server id lease.");
            try (Connection connection = DbUtil.openConnection(); Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("update FF$SERVER_ID_LEASE set EXPIRATION = null where ID = " + serverId);
                connection.commit();
            }
        }

        /*
         * Update the in-memory timestamp of the last successful
         * lease renewal. The leases last X minutes, so set the
         * timeout to X-1 minutes, to give a one minute margin.
         */
        private void updateValidity() {
            this.validUntil = new DateTime().plusMinutes(DbUtil.getServerIdLeaseTime() - 1);

            // Renew the lease after ten minutes
            this.interval = 10 * 60 * 1000;
            logger.debug("Server ID Lease is now valid until {}", this.validUntil);
        }

        @Override
        protected void shutdown() {
            try {
                clearLease();
            } catch (SQLException e) {
                logger.warn("Exception while closing connection", e);
            }
            super.shutdown();
        }

    }

}
