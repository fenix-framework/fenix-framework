package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.TopLevelTransaction;
import jvstm.TransactionUtils;
import jvstm.WriteSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;

public class RemoteCommitOnlyTransaction extends CommitOnlyTransaction {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCommitOnlyTransaction.class);

    // TODO: move to super class
//    private final WriteSet writeSet;

    public RemoteCommitOnlyTransaction(CommitRequest commitRequest) {
        super(TransactionUtils.getRecordForRemoteTransaction(commitRequest.getTxVersion()), commitRequest,
                makeRemoteWriteSet(commitRequest));
//        this.writeSet = makeWriteSet();
    }

    public static WriteSet makeRemoteWriteSet(CommitRequest commitRequest) {
        // TODO make a nice write set to use in the constructor:-) 
        throw new UnsupportedOperationException("not yet implemented");
    }

//    @Override
//    public int getNumber() {
//        // could this code be moved to CommitOnlyTransaction? 
//        return ((TopLevelTransaction) this).getNumber();
//    }

    @Override
    public TopLevelTransaction getUnderlyingTransaction() {
        return this;
    }

    @Override
    public void updateOrecVersion() {
        // no-op
    }

}
