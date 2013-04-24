package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.PerTxBox;
import jvstm.TopLevelTransaction;
import jvstm.Transaction;
import pt.ist.fenixframework.core.WriteOnReadError;

public class NonPersistentTopLevelTransaction extends TopLevelTransaction implements JvstmInFenixTransaction {

    private boolean readOnly = false;

    public NonPersistentTopLevelTransaction(ActiveTransactionsRecord activeRecord) {
        super(activeRecord);
    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        if (!txAllowsWrite() && !readOnly) {
            throw new WriteOnReadError();
        }

        // create a RW nested transaction, because we need its read-set
        NonPersistentNestedTransaction nested = new NonPersistentNestedTransaction(this);

        if (readOnly) {
            nested.setReadOnly();
        }
        return nested;
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
        return true;
    }

    @Override
    public <T> void setBoxValue(jvstm.VBox<T> vbox, T value) {
        if (!txAllowsWrite()) {
            throw new WriteOnReadError();
        } else {
            super.setBoxValue(vbox, value);
        }
    }

    @Override
    public <T> void setPerTxValue(PerTxBox<T> box, T value) {
        if (!txAllowsWrite()) {
            throw new WriteOnReadError();
        } else {
            super.setPerTxValue(box, value);
        }
    }

}
