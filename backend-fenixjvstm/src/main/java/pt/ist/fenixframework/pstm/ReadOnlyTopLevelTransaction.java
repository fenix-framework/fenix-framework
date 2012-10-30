package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;
import jvstm.ResumeException;

class ReadOnlyTopLevelTransaction extends TopLevelTransaction {

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

    protected void initDbChanges() {
	// do nothing
    }

    public ReadSet getReadSet() {
	throw new Error("ReadOnly txs don't record their read sets...");
    }

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
			System.out.println("Couldn't load the attribute " + attr + " for class " + obj.getClass());
			throw new VersionNotAvailableException();
		    }
		}
	    }
	}

	return body.value;
    }

    public boolean isWriteTransaction() {
	return false;
    }
}
