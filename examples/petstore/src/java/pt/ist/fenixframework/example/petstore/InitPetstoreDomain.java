
package pt.ist.fenixframework.example.petstore;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import pt.ist.fenixframework.example.petstore.domain.*;


public class InitPetstoreDomain {

    public static void main(final String[] args) {
        Config config = new Config() {{ 
            domainModelPath = args[0];
            dbAlias = "//localhost:3306/xpto"; 
            dbUsername = "root";
            dbPassword = "";
        }};
        FenixFramework.initialize(config);

        Transaction.withTransaction(new TransactionalCommand() {
                public void doIt() {
                    PetStore petStore = (PetStore)Transaction.getDomainObject(PetStore.class.getName(), 1);
                    System.out.println("Current PetStore instance: " + petStore);
                    System.out.println("Current PetStore accounts: " + petStore.getAccountSet());
                }
            });
    }
}
