package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.TransactionUtils;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;

public class RemoteCommitOnlyTransaction extends CommitOnlyTransaction {

    private final CommitRequest commitRequest;

    public RemoteCommitOnlyTransaction(CommitRequest commitRequest) {
        super(TransactionUtils.getRecordForRemoteTransaction(commitRequest.getTxVersion()));
        this.commitRequest = commitRequest;
    }

}
