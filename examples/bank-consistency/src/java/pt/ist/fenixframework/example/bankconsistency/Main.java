package pt.ist.fenixframework.example.bankconsistency;

import jvstm.Atomic;
import jvstm.cps.ConsistencyException;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.example.bankconsistency.BankConsistencyApplication.DomainPrinter;

public class Main {

    public static void main(final String[] args) {
	Config config = new Config() {
	    {
		domainModelPath = "/bank-consistency.dml";
		dbAlias = "//localhost:3306/bankconsistency";
		dbUsername = "root";
		dbPassword = "";
		updateRepositoryStructureIfNeeded = true;
		createRepositoryStructureIfNotExists = true;
		rootClass = BankConsistencyApplication.class;
	    }
	};
	FenixFramework.initialize(config);

	try {
	    populateDomainConsistent();

	    // createDOsInconsistent();

	    //makeDomainInconsistent();

	    // createConsistentDOThatMakesOtherDOsInconsistent();

	    // createNewClientConsistent();

	    // changeClientNameConsistent();

	    // removeAccountConsistent();

	    // removeDOThatMakesOtherDOsInconsistent();

	} catch (Error ex) {
	    if (ex.getCause() instanceof ConsistencyException) {
		System.out.println("Error caused by a ConsistencyException:");
		ex.getCause().printStackTrace();
	    } else {
		System.out.println("Error:");
		ex.printStackTrace();
	    }
	} catch (ConsistencyException ex) {
	    System.out.println("ConsistencyException:");
	    ex.printStackTrace();
	} finally {
	    printDomain();
	}
    }

    @Atomic
    private static void populateDomainConsistent() {
	Client clientGordon = new Client("Gordon");
	Account accountGordon = new Account();
	accountGordon.setBalance(70);
	accountGordon.setClosed(false);
	accountGordon.setDescription("blah");
	clientGordon.addAccounts(accountGordon);
	clientGordon.getAccounts().size();

	/*Client clientZoey = new Client("Zoey");
	Account accountZoeyA = new Account(clientZoey, 0);
	accountZoeyA.close();
	accountZoeyA.setDescription("This account is closed but consistent.");
	Account accountZoeyB = new Account(clientZoey, 30);

	Client clientBill = new Client("Bill");
	Account accountBillA = new Account(clientBill, 0);
	Account accountBillB = new Account(clientBill, 2000);
	Account accountBillC = new Account(clientBill, -1999);
	accountBillA.setDescription("0 account");
	accountBillB.setDescription("2k account");
	accountBillC.setDescription("negative account");*/
    }

    @Atomic
    private static void createDOsInconsistent() {
	Client clientFrancis = new Client("Francis");
	new ClientInfo(clientFrancis, "4");
	Account accountFrancis = new Account(clientFrancis, 1);
	accountFrancis.close();
	accountFrancis.setDescription("This account is inconsistent!");
    }

    @Atomic
    private static void makeDomainInconsistent() {
	BankConsistencyApplication app = BankConsistencyApplication.getInstance();
	for (Client client : app.getClients()) {
	    if (client.getName().equals("Gordon")) {
		for (Account account : client.getAccounts()) {
		    account.setDescription(null);
		}
	    }
	}

	for (Client client : app.getClients()) {
	    if (client.getName().equals("Bill")) {
		for (Account account : client.getAccounts()) {
		    String description = account.getDescription();
		    if (description != null && description.equals("negative account")) {
			//account.close();
		    }
		}
	    }
	}
    }

    @Atomic
    private static void createConsistentDOThatMakesOtherDOsInconsistent() {
	BankConsistencyApplication app = BankConsistencyApplication.getInstance();
	for (Client client : app.getClients()) {
	    if (client.getName().equals("Bill")) {
		/*
		 * client.setClientInfo(null); new ClientInfo(client, "");
		 */

		// new Account(client, -2);
	    }
	}
    }

    @Atomic
    private static void removeDOThatMakesOtherDOsInconsistent() {
	BankConsistencyApplication app = BankConsistencyApplication.getInstance();
	for (Client client : app.getClients()) {
	    if (client.getName().equals("Bill")) {
		ClientInfo billClientInfo = client.getClientInfo();
		/*
		 * client.setClientInfo(null); billClientInfo.delete();
		 */

		for (Account account : client.getAccounts()) {
		    String description = account.getDescription();
		    if (description != null && description.equals("2k account")) {
			/*
			 * account.removeClient(); account.removeApplication();
			 * account.delete();
			 */
		    }

		    /*
		     * account.removeApplication(); account.removeClient();
		     * account.delete();
		     */
		}

		for (Company company : client.getCompanies()) {
		    // company.removeClients(client);
		}

		/*
		 * billClientInfo.removeClient(); billClientInfo.delete();
		 * client.removeApplication(); client.delete();
		 */
	    }
	}
    }

    @Atomic
    private static void createNewClientConsistent() {
	Client clientFrancis = new Client("Francis");
	new ClientInfo(clientFrancis, "6");
    }

    @Atomic
    private static void changeClientNameConsistent() {
	BankConsistencyApplication app = BankConsistencyApplication.getInstance();
	for (Client client : app.getClients()) {
	    if (client.getName().equals("Zoey")) {
		client.setName("Zoeyy");
	    }
	}
    }

    @Atomic
    private static void removeAccountConsistent() {
	BankConsistencyApplication app = BankConsistencyApplication.getInstance();
	for (Client client : app.getClients()) {
	    if (client.getName().equals("Zoey")) {
		for (Account account : client.getAccounts()) {
		    account.removeApplication();
		    account.removeClient();
		    account.delete();
		}
	    }
	}
    }

    @Atomic
    private static void printDomain() {
	DomainPrinter.printDomain();
    }
}
