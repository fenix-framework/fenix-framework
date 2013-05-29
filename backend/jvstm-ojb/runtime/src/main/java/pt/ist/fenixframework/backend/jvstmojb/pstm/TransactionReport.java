package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionReport implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(TransactionReport.class);

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDate startOfReport;
    private LocalDate endOfReport;
    private String server;
    private TransactionAction transactionAction;

    public TransactionReport(final LocalDate startOfReport, final LocalDate endOfReport,
            final TransactionAction transactionAction, final String server) {
        setStartOfReport(startOfReport);
        setEndOfReport(endOfReport);
        setTransactionAction(transactionAction);
        setServer(server);
    }

    public LocalDate getEndOfReport() {
        return endOfReport;
    }

    public void setEndOfReport(LocalDate endOfReport) {
        this.endOfReport = endOfReport;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public LocalDate getStartOfReport() {
        return startOfReport;
    }

    public void setStartOfReport(LocalDate startOfReport) {
        this.startOfReport = startOfReport;
    }

    public TransactionAction getTransactionAction() {
        return transactionAction;
    }

    public void setTransactionAction(TransactionAction transactionAction) {
        this.transactionAction = transactionAction;
    }

    public void report() {
        PersistenceBroker broker = null;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            broker = PersistenceBrokerFactory.defaultPersistenceBroker();
            final Connection connection = broker.serviceConnectionManager().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(mekeQueryString());
            process(resultSet);
        } catch (Exception ex) {
            throw new Error(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // nothing can be done at this point
                }
            }
            if (broker != null) {
                if (broker.isInTransaction()) {
                    broker.abortTransaction();
                }
                broker.close();
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // nothing can be done at this point
                }
            }
        }
    }

    private List<ReportEntry> process(final ResultSet resultSet) throws SQLException {

        List<ReportEntry> entries = new ArrayList<ReportEntry>();

        while (resultSet.next()) {
            final String server = resultSet.getString(1);
            if (this.server == null || this.server.equals(server)) {
                final long reads = resultSet.getLong(2);
                final long writes = resultSet.getLong(3);
                final long aborts = resultSet.getLong(4);
                final long conflicts = resultSet.getLong(5);
                final DateTime when = new DateTime(resultSet.getTimestamp(6));

                entries.add(new ReportEntry(when, reads, writes, aborts, conflicts));
            }
        }

        Collections.sort(entries);

        return entries;
    }

    private String mekeQueryString() {
        return "select left(SERVER, locate(':', SERVER) - 1), NUM_READS, NUM_WRITES, NUM_ABORTS, NUM_CONFLICTS, STATS_WHEN from FF$TRANSACTION_STATISTICS "
                + "where STATS_WHEN >='"
                + dateTimeFormatter.print(startOfReport.toDateMidnight())
                + "' and STATS_WHEN < '"
                + dateTimeFormatter.print(endOfReport.toDateMidnight()) + "'";
    }

    public TreeSet<String> getServers() {
        final TreeSet<String> servers = new TreeSet<String>();
        PersistenceBroker broker = null;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            broker = PersistenceBrokerFactory.defaultPersistenceBroker();
            final Connection connection = broker.serviceConnectionManager().getConnection();
            statement = connection.createStatement();
            final String query =
                    "select distinct(left(FF$TRANSACTION_STATISTICS.SERVER, locate(':', FF$TRANSACTION_STATISTICS.SERVER) - 1)) from FF$TRANSACTION_STATISTICS";
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                servers.add(resultSet.getString(1));
            }
        } catch (Exception ex) {
            throw new Error(ex);
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // nothing can be done at this point
                }
            }
            if (broker != null) {
                if (broker.isInTransaction()) {
                    broker.abortTransaction();
                }
                broker.close();
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // nothing can be done at this point
                }
            }
        }
        return servers;
    }

    public class ReportEntry implements Comparable<ReportEntry> {

        private final DateTime when;
        private final long reads;
        private final long writes;
        private final long aborts;
        private final long conflicts;

        public ReportEntry(DateTime when, long reads, long writes, long aborts, long conflicts) {
            super();
            this.when = when;
            this.reads = reads;
            this.writes = writes;
            this.aborts = aborts;
            this.conflicts = conflicts;
        }

        public DateTime getWhen() {
            return when;
        }

        public long getReads() {
            return reads;
        }

        public long getWrites() {
            return writes;
        }

        public long getAborts() {
            return aborts;
        }

        public long getConflicts() {
            return conflicts;
        }

        @Override
        public int compareTo(ReportEntry other) {
            return when.compareTo(other.when);
        }
    }

}
