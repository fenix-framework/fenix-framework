package pt.ist.fenixframework.example.bankconsistency;

import pt.ist.fenixframework.FenixFramework;

public class BankConsistencyApplication extends BankConsistencyApplication_Base {

    public static BankConsistencyApplication getInstance() {
	return FenixFramework.getRoot();
    }

    public BankConsistencyApplication() {
	super();
	checkIfIsSingleton();
    }

    private void checkIfIsSingleton() {
	if (FenixFramework.getRoot() != null && FenixFramework.getRoot() != this) {
	    throw new Error("There can be only one instance of BankConsistencyApplication");
	}
    }

    public static class DomainPrinter {
	public static void printDomain() {
	    System.out.println();
	    System.out.println();
	    System.out.println();
	    System.out.println("=========================== CURRENT DOMAIN DATA ===========================");
	    System.out.println();
	    System.out.println();
	    System.out.println();
	    System.out.println("=========================== CLIENTS AND ACCOUNTS ===========================");
	    for (Client client : BankConsistencyApplication.getInstance().getClients()) {
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(client);
		System.out.println(client.getClientInfo());
		if (client.hasAnyAccounts()) {
		    System.out.println("Owned accounts:");
		    for (Account account : client.getAccounts()) {
			System.out.println(account);
		    }
		    System.out.println();
		}
	    }
	    System.out.println();
	    System.out.println();
	    System.out.println();
	    System.out.println("=========================== COMPANIES AND INVOLVED CLIENTS ===========================");
	    for (Company company : BankConsistencyApplication.getInstance().getCompanies()) {
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println(company);
		System.out.println("Associated Clients:");
		for (Client client : company.getClients()) {
		    System.out.println(client);
		}
		System.out.println();
	    }
	}
    }
}
