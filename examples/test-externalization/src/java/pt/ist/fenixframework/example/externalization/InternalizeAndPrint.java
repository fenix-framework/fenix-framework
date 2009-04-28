package pt.ist.fenixframework.example.externalization;

import jvstm.Atomic;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import pt.ist.fenixframework.example.externalization.domain.*;



import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;



public class InternalizeAndPrint {

    public static void main(final String[] args) {
	Configuration.initializeFenixFramework();

        doIt();
    }

    @Atomic
    private static void doIt() {
        Root root = FenixFramework.getRoot();
        DataStore actualDataStore = root.getDataStore();
        
        DataStore expectedDataStore = Configuration.createTestDataStore();
        Configuration.checkDataStoreEquality(actualDataStore, expectedDataStore);
    }
}

