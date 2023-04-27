package pt.ist.fenixframework.backend.jvstm.pstm;

public class TransactionStatistics {

    public final static TransactionStatistics STATISTICS = new TransactionStatistics();

    private int numReadTxs = 0;
    private int numWriteTxs = 0;
    private int numAborts = 0;
    private int numConflicts = 0;

    private final CounterStats readOnlyReads = new CounterStats();
    private final CounterStats readWriteReads = new CounterStats();
    private final CounterStats readWriteWrites = new CounterStats();

    TransactionStatistics() {
    }

    public synchronized void incReads(StatisticsCapableTransaction tx) {
        // don't count empty transactions
        if (tx.getNumBoxReads() == 0) {
            return;
        }

        numReadTxs++;

        readOnlyReads.addNewValue(tx.getNumBoxReads());
    }

    public synchronized void incWrites(StatisticsCapableTransaction tx) {
        numWriteTxs++;

        readWriteReads.addNewValue(tx.getNumBoxReads());
        readWriteWrites.addNewValue(tx.getNumBoxWrites());
    }

    public synchronized void incAborts() {
        numAborts++;
    }

    public synchronized void incConflicts() {
        numConflicts++;
    }

    public synchronized Report getReportAndReset() {
        Report report = new Report(numReadTxs, numWriteTxs, numAborts, numConflicts, readOnlyReads.getAndReset(),
                readWriteReads.getAndReset(), readWriteWrites.getAndReset());
        numReadTxs = 0;
        numWriteTxs = 0;
        numAborts = 0;
        numConflicts = 0;

        return report;
    }

    public static class Report {
        public final int numReads;
        public final int numWrites;
        public final int numAborts;
        public final int numConflicts;

        public final CounterStats readOnlyReads;
        public final CounterStats readWriteReads;
        public final CounterStats readWriteWrites;

        public Report(int numReads, int numWrites, int numAborts, int numConflicts, CounterStats readOnlyReads,
                CounterStats readWriteReads, CounterStats readWriteWrites) {
            this.numReads = numReads;
            this.numWrites = numWrites;
            this.numAborts = numAborts;
            this.numConflicts = numConflicts;
            this.readOnlyReads = readOnlyReads;
            this.readWriteReads = readWriteReads;
            this.readWriteWrites = readWriteWrites;
        }
    }

    public static class CounterStats {
        int minValue = Integer.MAX_VALUE;
        int maxValue = 0;
        long valueSum = 0;

        // ctor and access methods added due to support for externalization

        public CounterStats() {
        }

        public CounterStats(int minValue, int maxValue, long valueSum) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.valueSum = valueSum;

        }

        // used only when externalizing as one element.  To delete when no longer needed
        public CounterStats(String externalizedForm) {
            String tokens[] = externalizedForm.split(":");

            this.minValue = Integer.parseInt(tokens[0]);
            this.maxValue = Integer.parseInt(tokens[1]);
            this.valueSum = Long.parseLong(tokens[2]);
        }

        public final int getMinValue() {
            return this.minValue;
        }

        public final int getMaxValue() {
            return this.maxValue;
        }

        public final long getValueSum() {
            return this.valueSum;
        }

        public void addNewValue(int value) {
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
            valueSum += value;
        }

        public CounterStats getAndReset() {
            CounterStats snapshot = new CounterStats();
            snapshot.minValue = minValue;
            snapshot.maxValue = maxValue;
            snapshot.valueSum = valueSum;

            minValue = Integer.MAX_VALUE;
            maxValue = 0;
            valueSum = 0;

            return snapshot;
        }

        public String externalizeInOneElement() {
            return getMinValue() + ":" + getMaxValue() + ":" + getValueSum();
        }
    }
}
