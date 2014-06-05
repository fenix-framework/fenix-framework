package pt.ist.fenixframework.backend.jvstmojb.pstm;

import jvstm.ResumeException;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReadOnlyTopLevelTransaction extends TopLevelTransaction {

    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyTopLevelTransaction.class);

    ReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    protected void checkValidity(jvstm.ActiveTransactionsRecord record) {
        // for read-only transactions, for which we do not store the
        // read-set, it is not possible to know that we will see a
        // consistent read after resuming, unless the new record is
        // exactly the same that we have

        if (record != this.activeTxRecord) {
            throw new ResumeException("Transaction may be no longer valid for resuming");
        }
    }

    @Override
    protected void initDbChanges() {
        // do nothing
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr) {
        numBoxReads++;
        VBoxBody<T> body = vbox.body.getBody(number);
        if (body.value == VBox.NOT_LOADED_VALUE) {
            synchronized (body) {
                if (body.value == VBox.NOT_LOADED_VALUE) {
                    vbox.reload(obj, attr);
                    // after the reload, the same body should have a new value
                    // if not, then something gone wrong and its better to abort
                    if (body.value == VBox.NOT_LOADED_VALUE) {
                        logger.error("Couldn't load the attribute {} for class {}", attr, obj.getClass());
                        throw new VersionNotAvailableException("Couldn't load the attribute " + attr + " for instance " + obj);
                    }
                }
            }
        }

        return body.value;
    }

    @Override
    public boolean isWriteTransaction() {
        return false;
    }
}
