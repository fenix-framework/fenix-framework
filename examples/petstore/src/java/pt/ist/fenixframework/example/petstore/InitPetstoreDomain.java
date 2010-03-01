
package pt.ist.fenixframework.example.petstore;

import jvstm.Atomic;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import pt.ist.fenixframework.example.petstore.domain.*;


public class InitPetstoreDomain {

    public static void main(final String[] args) {
        Config config = new Config() {{ 
            domainModelPath = "/petstore.dml";
            dbAlias = "//localhost:3306/test"; 
            dbUsername = "test";
            dbPassword = "test";
            rootClass = PetStore.class;
        }};
        FenixFramework.initialize(config);

        doIt();
    }

    @Atomic
    private static void doIt() {
        PetStore petStore = FenixFramework.getRoot();
        System.out.println("Current PetStore instance: " + petStore);
        System.out.println("Current PetStore accounts: " + petStore.getAccountSet());
    }
}
