package pt.ist.fenixframework.backend.jvstm.pstm;

import org.joda.time.DateTime;

import pt.ist.fenixframework.backend.jvstm.pstm.TransactionStatistics.CounterStats;

// this class is used to store the statistics concerning the execution
// of the transactions during a certain amount of time. See StatisticsThread 
// for more details.

public class TransactionStatisticsEntry extends TransactionStatisticsEntry_Base {

    public TransactionStatisticsEntry(String server, int numReport, long numReads, long numWrites, long numAborts,
            long numConflicts, long secondsBetweenReports, DateTime when, CounterStats readOnlyReads,
            CounterStats readWriteReads, CounterStats readWriteWrites) {
        super();
        setServer(server);
        setNumReport(numReport);
        setNumReads(numReads);
        setNumWrites(numWrites);
        setNumAborts(numAborts);
        setNumConflicts(numConflicts);
        setSecondsBetweenReports(secondsBetweenReports);
        setWhen(when);
        setReadOnlyReads(readOnlyReads);
        setReadWriteReads(readWriteReads);
        setReadWriteWrites(readWriteWrites);
    }

    final public boolean isInPeriod(long from, long until) {
        long thisTime = this.getWhen().getMillis();
        return from <= thisTime && thisTime <= until;
    }

}
