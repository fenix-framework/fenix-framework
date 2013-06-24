package pt.ist.fenixframework.backend.jvstm.lf;

import pt.ist.fenixframework.backend.jvstm.pstm.AbstractLockFreeTransaction;

public class LocalLockFreeTransaction extends AbstractLockFreeTransaction {

    private final CommitRequest commitRequest;

    public LocalLockFreeTransaction(CommitRequest commitRequest, AbstractLockFreeTransaction tx) {
        super(tx.getActiveTxRecord());
        this.commitRequest = commitRequest;
    }

}
