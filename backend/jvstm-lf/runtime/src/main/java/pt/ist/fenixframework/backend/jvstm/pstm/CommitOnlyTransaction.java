package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jvstm.ActiveTransactionsRecord;
import jvstm.TopLevelTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommitOnlyTransaction extends TopLevelTransaction {

    private static final Logger logger = LoggerFactory.getLogger(CommitOnlyTransaction.class);

    /**
     * Maps all node local {@link LockFreeTransaction}s using their id as key. Whoever completes the processing is
     * required to remove the entry from this map, lest it grow indefinitely with the number of transactions.
     * 
     */
    public final static ConcurrentHashMap<UUID, LockFreeTransaction> commitsMap =
            new ConcurrentHashMap<UUID, LockFreeTransaction>();

//    private boolean readOnly = false;

//    // for statistics
//    protected int numBoxReads = 0;
//    protected int numBoxWrites = 0;

    public CommitOnlyTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    public boolean isWriteTransaction() {
        return true;
    }

//    @Override
//    public void setReadOnly() {
//        this.readOnly = true;
//    }
//
//    @Override
//    public boolean txAllowsWrite() {
//        return !this.readOnly;
//    }
//
//    @Override
//    public <T> T getBoxValue(VBox<T> vbox) {
//        return super.getBoxValue(vbox);
//    }
//
//    @Override
//    public boolean isBoxValueLoaded(VBox vbox) {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    @Override
//    public int getNumBoxReads() {
//        return numBoxReads;
//    }
//
//    @Override
//    public int getNumBoxWrites() {
//        return numBoxWrites;
//    }

//    @Override
    /**
     * This is the commit algorithm that each CommitOnlyTransaction performs on each node, regardless of whether it is a
     * {@link LocalCommitOnlyTransaction} or a {@link RemoteCommitOnlyTransaction}. Note that {@link LockFreeTransaction}s
     * are decorated by {@link CommitOnlyTransaction}s.
     */
    public void localCommit() {
        this.commitTx(false); // smf TODO: double-check whether we want to mess with ActiveTxRecords, thread-locals, etc.  I guess 'false' is the way to go...
    }
}
