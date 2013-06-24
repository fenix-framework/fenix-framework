package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jvstm.ActiveTransactionsRecord;
import jvstm.cps.ConsistentTopLevelTransaction;

public abstract class AbstractLockFreeTransaction extends ConsistentTopLevelTransaction implements StatisticsCapableTransaction,
        LockFreeTransaction {

    private enum ValidationStatus {
        UNSET, VALID, INVALID;
    };

    // smf: may need to add here a ref to the commitrecord.  It'd similar to the commitTxRecord in TLTransaction, except that this would be for txs not yet validaded :-) 

    /**
     * Maps all {@link DistributedLockFreeTransaction}s using their id as key. Whoever completes the processing is required to
     * remove
     * the entry from this map, lest it grow indefinitely with the number of transactions.
     * 
     */
//    /**
//     * Maps all {@link CommitRequest}s using their id as key. Whoever completes the processing is required to remove
//     * the entry from this map, lest it grow indefinitely with the number of transactions.
//     * 
//     */
    public final static ConcurrentHashMap<UUID, AbstractLockFreeTransaction> commitsMap =
            new ConcurrentHashMap<UUID, AbstractLockFreeTransaction>();

    /* for any transaction instance this will always change deterministically
    from UNSET to either VALID or INVALID, i.e. if concurrent helpers try to
    decide, they will conclude the same and the value will never revert back to
    UNSET. */
    private volatile ValidationStatus validationStatus = ValidationStatus.UNSET;

    private boolean readOnly = false;

    // for statistics
    protected int numBoxReads = 0;
    protected int numBoxWrites = 0;

    public AbstractLockFreeTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    public void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public boolean txAllowsWrite() {
        return !this.readOnly;
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        return super.getBoxValue(vbox);
    }

    @Override
    public boolean isBoxValueLoaded(VBox vbox) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public int getNumBoxReads() {
        return numBoxReads;
    }

    @Override
    public int getNumBoxWrites() {
        return numBoxWrites;
    }

    @Override
    public void localCommit() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("not yet implemented");
    }
}
