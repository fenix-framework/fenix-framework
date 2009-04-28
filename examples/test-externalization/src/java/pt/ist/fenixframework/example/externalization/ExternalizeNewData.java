package pt.ist.fenixframework.example.externalization;

import jvstm.Atomic;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import pt.ist.fenixframework.example.externalization.domain.*;


public class ExternalizeNewData {
    
    public static void main(final String[] args) {
	Configuration.initializeFenixFramework();

        doIt();
    }

    @Atomic
    private static void doIt() {
        Root root = FenixFramework.getRoot();
        
        root.setDataStore(Configuration.createTestDataStore());
        System.out.println("Created a new DataStore.");
    }
}
