package pt.ist.fenixframework.pstm;

import java.util.concurrent.ConcurrentLinkedQueue;


/*
 * This class is a generalization of what already existed for keeping
 * a record of AlienTransactions (that is, transactions committed on
 * another server) so that we keep a record of objects changed that
 * should not be garbage-collected.
 *
 * The same should be done for locally committed transactions,
 * because, otherwise, objects changed on a transaction Tn that are
 * garbage collected and then read by a later transaction may result
 * in having older transactions seeing values from the future.
 *
 * To see why this may happen, consider the case of three
 * transactions, T1, T2, and T3, from oldest to newer, where T2
 * changes some object O before T1 accesses it.  If O is GCed and T3
 * needs to access it, T3 will allocate the object and eventually read
 * it, putting the values read in version 0 (that's what VBox.makeNew
 * does).  If after this T1 accesses O, it will see the value written
 * by T2, which is not correct.
 *
 * By having T2 add a CommitRecord to this class, we will maintain a
 * strong reference to O, meaning that it will not be garbage
 * collected.
 */
class TransactionCommitRecords {
    // CommitRecords that were not GCed yet
    private static final ConcurrentLinkedQueue<CommitRecord> COMMIT_RECORDS = new ConcurrentLinkedQueue<CommitRecord>();

    static {
	Transaction.addTxQueueListener(new jvstm.TxQueueListener() {
		public void noteOldestTransaction(int newOldest) {
                    cleanOldCommitRecords(newOldest);
		}
	    });
    }

    public static void cleanOldCommitRecords(int txNumber) {
        synchronized (COMMIT_RECORDS) {
            while ((! COMMIT_RECORDS.isEmpty()) && (COMMIT_RECORDS.peek().txNumber <= txNumber)) {
                COMMIT_RECORDS.poll();
            }
        }
    }

    public static void addCommitRecord(int txNumber, Object objects) {
        COMMIT_RECORDS.offer(new CommitRecord(txNumber, objects));
    }

    static class CommitRecord {
        final int txNumber;
        final Object objects;

        CommitRecord(int txNumber, Object objects) {
            this.txNumber = txNumber;
            this.objects = objects;  // this is where we keep the strong reference
        }
    }
}
