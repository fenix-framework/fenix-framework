package pt.ist.fenixframework.example.bankconsistency;

import pt.ist.fenixframework.FenixFramework;

public class BankConsistencyApplication extends BankConsistencyApplication_Base {

    public static BankConsistencyApplication getInstance() {
	BankConsistencyApplication application = FenixFramework.getRoot();
	return application;
    }

    public void printDomain() {
	for (Client client : getClients()) {
	    System.out.println("Client: " + client);
	    int accountCounter = 0;
	    for (Account account : client.getAccounts()) {
		System.out.println("Account " + ++accountCounter + ": " + account);
	    }
	    System.out.println();
	}
    }
}
