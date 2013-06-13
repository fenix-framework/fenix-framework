package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentReadOnlyTransaction extends PersistentTransaction {

    private static final Logger logger = LoggerFactory.getLogger(PersistentReadOnlyTransaction.class);

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
            // if not, then something went wrong and it's better to abort
            // body = vbox.body.getBody(number);
            body = vbox.getBody(number);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                logger.error("Couldn't load the VBox: {}", vbox.getId());
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
