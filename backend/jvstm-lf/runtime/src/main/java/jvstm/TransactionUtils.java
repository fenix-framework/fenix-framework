package jvstm;

public class TransactionUtils {

    /* This operation needs to be performed atomically.  Currently, it is invoked
    during the bootstrap of Fenix Framework, so there is no other transaction
    running that might be changing the ActiveTransactionsRecord. */
    public static void initializeTxNumber(int maxTx) {
        ActiveTransactionsRecord initialRecord = new ActiveTransactionsRecord(maxTx, WriteSet.empty());
        boolean success = Transaction.mostRecentCommittedRecord.trySetNext(initialRecord);
        if (!success) {
            throw new AssertionError("Impossible condition: Failed to initializeTxNumber.");
        }
        Transaction.setMostRecentCommittedRecord(initialRecord);
    }

}
