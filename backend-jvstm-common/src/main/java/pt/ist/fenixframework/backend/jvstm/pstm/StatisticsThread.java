package pt.ist.fenixframework.backend.jvstm.pstm;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.FenixFramework;

public class StatisticsThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsThread.class);

    private static final long SECONDS_BETWEEN_REPORTS = 5 * 60;

    private final String server;
    private int numReport = 0;

    public StatisticsThread() {
        this.server = Util.getServerName();

        setDaemon(true);
    }

    @Override
    public void run() {
        while (true) {
            try {
                sleep(SECONDS_BETWEEN_REPORTS * 1000);
            } catch (InterruptedException ie) {
                // ignore exception
            }
            reportStatistics();
        }
    }

    private void reportStatistics() {
        final TransactionStatistics.Report stats = TransactionStatistics.STATISTICS.getReportAndReset();
        numReport++;
        doAtomicReporting(stats);
    }

    @Atomic(mode = TxMode.WRITE)
    private void doAtomicReporting(final TransactionStatistics.Report stats) {
        TransactionStatisticsEntry entry;
        entry =
                new TransactionStatisticsEntry(server, numReport, stats.numReads, stats.numWrites, stats.numAborts,
                        stats.numConflicts, SECONDS_BETWEEN_REPORTS, new DateTime(), stats.readOnlyReads, stats.readWriteReads,
                        stats.readWriteWrites);
        FenixFrameworkData ffData = FenixFramework.getDomainRoot().getFenixFrameworkData();
        ffData.addFFTxStatsEntry(entry);
    }

}
