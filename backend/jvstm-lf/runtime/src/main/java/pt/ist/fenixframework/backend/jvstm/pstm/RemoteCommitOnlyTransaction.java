package pt.ist.fenixframework.backend.jvstm.pstm;

import static jvstm.UtilUnsafe.UNSAFE;
import jvstm.TopLevelTransaction;
import jvstm.TransactionUtils;
import jvstm.UtilUnsafe;
import jvstm.WriteSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.RemoteWriteSet;

public class RemoteCommitOnlyTransaction extends CommitOnlyTransaction {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCommitOnlyTransaction.class);

    private static final long writeSetOffset = UtilUnsafe.objectFieldOffset(RemoteCommitOnlyTransaction.class, "writeSet");

    private final WriteSet writeSet = RemoteWriteSet.EMPTY;

    public RemoteCommitOnlyTransaction(CommitRequest commitRequest) {
        super(TransactionUtils.getRecordForRemoteTransaction(commitRequest.getTxVersion()), commitRequest);
    }

    @Override
    public TopLevelTransaction getUnderlyingTransaction() {
        return this;
    }

    @Override
    protected WriteSet getWriteSet() {
        WriteSet thisWriteSet = this.writeSet;
        if (thisWriteSet == RemoteWriteSet.EMPTY) {
            thisWriteSet = makeWriteSet();
            if (UNSAFE.compareAndSwapObject(this, this.writeSetOffset, RemoteWriteSet.EMPTY, thisWriteSet)) {
                logger.debug("set writeSet for request {}", this.commitRequest.getId());
            } else {
                logger.debug("writeSet was already set for request {}", this.commitRequest.getId());
                thisWriteSet = this.writeSet;
            }
        }
        return thisWriteSet;
    }

    @Override
    public WriteSet makeWriteSet() {
        return new RemoteWriteSet(this.commitRequest.getWriteSet());
    }

    @Override
    public void updateOrecVersion() {
        // no-op
    }

}
