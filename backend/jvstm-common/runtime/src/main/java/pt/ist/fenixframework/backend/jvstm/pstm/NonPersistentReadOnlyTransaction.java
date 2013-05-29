package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ReadTransaction;
import jvstm.Transaction;
import pt.ist.fenixframework.core.WriteOnReadError;

public class NonPersistentReadOnlyTransaction extends ReadTransaction implements JvstmInFenixTransaction {

    protected NonPersistentReadOnlyTransaction(int number) {
        super(number);
    }

    protected NonPersistentReadOnlyTransaction(Transaction parent) {
        super(parent);
    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        if (!readOnly) {
            throw new WriteOnReadError();
        }
        return new NonPersistentReadOnlyTransaction(this);
    }

    @Override
    public void setReadOnly() {
        // nothing to do, tx is already read-only :-)
    }

    @Override
    public boolean txAllowsWrite() {
        return false;
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        return super.getBoxValue(vbox);
    }

    @Override
    public boolean isBoxValueLoaded(VBox vbox) {
        return true;
    }

}
