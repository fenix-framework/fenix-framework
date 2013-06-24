package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.TransactionUtils;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;

public class RemoteLockFreeTransaction extends AbstractLockFreeTransaction {

    private final CommitRequest commitRequest;

    public RemoteLockFreeTransaction(CommitRequest commitRequest) {
        super(TransactionUtils.getRecordForRemoteTransaction(commitRequest.getTxVersion()));
        this.commitRequest = commitRequest;
    }

}
