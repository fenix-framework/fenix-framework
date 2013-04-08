package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.NestedTransaction;
import jvstm.PerTxBox;
import jvstm.ReadWriteTransaction;
import pt.ist.fenixframework.core.WriteOnReadError;

public class NonPersistentNestedTransaction extends NestedTransaction implements JvstmInFenixTransaction {

    private boolean readOnly;

    public NonPersistentNestedTransaction(ReadWriteTransaction parent) {
        super(parent);
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
