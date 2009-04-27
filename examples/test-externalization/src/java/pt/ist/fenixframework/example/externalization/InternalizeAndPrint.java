package pt.ist.fenixframework.example.externalization;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import pt.ist.fenixframework.example.externalization.domain.*;



import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;



public class InternalizeAndPrint {

    public static void main(final String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT+10"));
	Configuration.initializeFenixFramework();

        Transaction.withTransaction(new TransactionalCommand() {
                public void doIt() {
		    Root root = (Root)Transaction.getDomainObject(Root.class.getName(), 1);
                    DataStore actualDataStore = root.getDataStore();

		    DataStore expectedDataStore = Configuration.createTestDataStore();
 		    Configuration.checkDataStoreEquality(actualDataStore, expectedDataStore);
                }
            });
    }
}

