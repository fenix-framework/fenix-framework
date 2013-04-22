package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;

public class NonPersistentTopLevelReadOnlyTransaction extends NonPersistentReadOnlyTransaction {

    private ActiveTransactionsRecord activeTxRecord;

    public NonPersistentTopLevelReadOnlyTransaction(ActiveTransactionsRecord activeRecord) {
        super(activeRecord.transactionNumber);
        this.activeTxRecord = activeRecord;
    }

    protected NonPersistentTopLevelReadOnlyTransaction(int number) {
        super(number);
    }

    protected NonPersistentTopLevelReadOnlyTransaction(Transaction parent) {
        super(parent);
    }

    @Override
    protected ActiveTransactionsRecord getSameRecordForNewTransaction() {
        this.activeTxRecord.incrementRunning();
        return this.activeTxRecord;
    }

    @Override
    protected void finish() {
        super.finish();
        activeTxRecord.decrementRunning();
    }

}
