package pt.ist.fenixframework.example.dbinit;

import jvstm.Atomic;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import pt.ist.fenixframework.example.dbinit.domain.*;

public class Main {

    public static void main(final String[] args) {
	Configuration.initializeFenixFramework();

        doIt();
    }

    @Atomic
    private static void doIt() {
        Root root = FenixFramework.getRoot();

        DataStore ds = new DataStore();
        ds.setValor(54);
        ds.setAString("olá");
        root.setDataStore(ds);
        System.out.println("Created a new DataStore.");
    }
}
