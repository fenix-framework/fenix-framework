package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jvstm.ActiveTransactionsRecord;
import jvstm.cps.ConsistentTopLevelTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLockFreeTransaction extends ConsistentTopLevelTransaction implements StatisticsCapableTransaction,
        LockFreeTransaction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLockFreeTransaction.class);

    // smf: may need to add here a ref to the commitrecord.  It'd be similar to the commitTxRecord in TLTransaction, except that this would be for txs not yet validaded :-) 

    /**
     * Maps all {@link DistributedLockFreeTransaction}s using their id as key. Whoever completes the processing is required to
     * remove the entry from this map, lest it grow indefinitely with the number of transactions.
     * 
     */
    public final static ConcurrentHashMap<UUID, DistributedLockFreeTransaction> commitsMap =
            new ConcurrentHashMap<UUID, DistributedLockFreeTransaction>();

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
        this.commitTx(false); // smf TODO: double-check whether we want to mess with ActiveTxRecords, thread-locals, etc.  I guess 'false' is the way to go...
    }
}
