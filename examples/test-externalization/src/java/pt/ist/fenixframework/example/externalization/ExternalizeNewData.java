package pt.ist.fenixframework.example.externalization;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import pt.ist.fenixframework.example.externalization.domain.*;


public class ExternalizeNewData {
    
    public static void main(final String[] args) {
	Configuration.initializeFenixFramework();

        Transaction.withTransaction(new TransactionalCommand() {
                public void doIt() {
		    Root root = Configuration.ensureRootObject();

		    root.setDataStore(Configuration.createTestDataStore());
		    System.out.println("Created a new DataStore.");
                }
            });
    }
}
