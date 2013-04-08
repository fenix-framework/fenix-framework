package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.VBoxBody;

public class PersistentReadOnlyTransaction extends PersistentTransaction {

    public PersistentReadOnlyTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        numBoxReads++;
        VBoxBody<T> body = vbox.getBody(number);
        if (body.value == VBox.NOT_LOADED_VALUE) {
            vbox.reload();
            // after the reload, the (new) body should have the required loaded value
            // if not, then something gone wrong and its better to abort
            // body = vbox.body.getBody(number);
            body = vbox.getBody(number);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                System.out.println("Couldn't load the attribute " + vbox.getSlotName() + " for class "
                        + vbox.getOwnerObject().getClass());
                throw new VersionNotAvailableException();
            }
        }

        return body.value;
    }

    @Override
    public boolean txAllowsWrite() {
        return false;
    }

    @Override
    public void setReadOnly() {
        // nothing to do, PersistentReadOnlyTransaction is already read-only :-)
    }
}
