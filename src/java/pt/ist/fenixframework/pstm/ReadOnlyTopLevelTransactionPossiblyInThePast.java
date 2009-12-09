package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

class ReadOnlyTopLevelTransactionPossiblyInThePast extends ReadOnlyTopLevelTransaction {
    
    ReadOnlyTopLevelTransactionPossiblyInThePast(jvstm.ActiveTransactionsRecord record) {
	super(record);
    }

    @Override
    protected void initDbConnection(boolean resuming) {
	// do nothing
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr) {
	numBoxReads++;
	VBoxBody<T> body = vbox.body.getBody(number);
	if (body.value == VBox.NOT_LOADED_VALUE) {
            throw new LoadException();
	}

	return body.value;
    }
}
