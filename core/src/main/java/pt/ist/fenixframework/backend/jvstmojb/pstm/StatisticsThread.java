package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.util.FenixFrameworkThread;

class StatisticsThread extends FenixFrameworkThread {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsThread.class);

    private static final long SECONDS_BETWEEN_REPORTS = 5 * 60;

    private final static String server = Util.getServerName();

    private int numReport = 0;

    StatisticsThread() {
        super("StatisticsThread");
        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(SECONDS_BETWEEN_REPORTS * 1000);
            } catch (InterruptedException ie) {
                return;
            }
            reportStatistics();
        }
    }

    private void reportStatistics() {
        PersistenceBroker broker = null;
        Statement stmt = null;

        try {
            broker = PersistenceBrokerFactory.defaultPersistenceBroker();
            broker.beginTransaction();

            Connection conn = broker.serviceConnectionManager().getConnection();
            stmt = conn.createStatement();

            TransactionStatistics.Report stats = TransactionSupport.STATISTICS.getReportAndReset();
            numReport++;

            StringBuilder sqlStmtText = new StringBuilder();
            sqlStmtText.append("INSERT INTO FF$TRANSACTION_STATISTICS ");
            // column names -> keep them in sync with the values
            sqlStmtText.append("(SERVER,NUM_REPORT,NUM_READS,NUM_WRITES,NUM_ABORTS,NUM_CONFLICTS,SECONDS,STATS_WHEN,");
            sqlStmtText.append("READ_ONLY_READS_MIN,READ_ONLY_READS_MAX,READ_ONLY_READS_SUM,");
            sqlStmtText.append("READ_WRITE_READS_MIN,READ_WRITE_READS_MAX,READ_WRITE_READS_SUM,");
            sqlStmtText.append("READ_WRITE_WRITES_MIN,READ_WRITE_WRITES_MAX,READ_WRITE_WRITES_SUM)");
            sqlStmtText.append(" VALUES ('");
            sqlStmtText.append(server);
            sqlStmtText.append("',");
            sqlStmtText.append(numReport);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.numReads);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.numWrites);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.numAborts);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.numConflicts);
            sqlStmtText.append(",");
            sqlStmtText.append(SECONDS_BETWEEN_REPORTS);
            // this null value is for the timestamp);
            sqlStmtText.append(",null,");
            sqlStmtText.append(stats.readOnlyReads.minValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readOnlyReads.maxValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readOnlyReads.valueSum);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteReads.minValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteReads.maxValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteReads.valueSum);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteWrites.minValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteWrites.maxValue);
            sqlStmtText.append(",");
            sqlStmtText.append(stats.readWriteWrites.valueSum);
            sqlStmtText.append(")");

            // insert statistics for this server
            stmt.executeUpdate(sqlStmtText.toString());

            broker.commitTransaction();
        } catch (Exception e) {
            // the statistics are not crucial, if anything goes wrong, that's ok
            // issue just a warning
            e.printStackTrace();
            logger.warn("WARNING: Couldn't insert the statistics data");
            if ((broker != null) && (broker.isInTransaction())) {
                broker.abortTransaction();
            }
        } finally {
            if (broker != null) {
                if (broker.isInTransaction()) {
                    broker.abortTransaction();
                }
                broker.close();
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    // nothing can be done now.
                }
            }
        }
    }
}
