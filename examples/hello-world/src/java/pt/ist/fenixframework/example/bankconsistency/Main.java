package pt.ist.fenixframework.example.bankconsistency;

import jvstm.Atomic;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;

public class Main {
    public static void main(final String[] args) {
	Config config = new Config() {
	    {
		domainModelPath = "/bank-consistency.dml";
		dbAlias = "//localhost:3306/bankconsistency";
		dbUsername = "root";
		dbPassword = "";
		updateRepositoryStructureIfNeeded = true;
		rootClass = BankConsistencyApplication.class;
	    }
	};
	FenixFramework.initialize(config);

	//populateDomain();

	printDomain();
    }

    @Atomic
    private static void populateDomain() {
	BankConsistencyApplication app = FenixFramework.getRoot();
	Client clientGordon = new Client("Gordon");
	Account accountGordon = new Account(clientGordon, 70);

	Client clientZoey = new Client("Zoey");
	Account accountZoeyA = new Account(clientZoey, 0);
	accountZoeyA.close();
	Account accountZoeyB = new Account(clientZoey, 30);

	Client clientBill = new Client("Bill");
	Account accountBillA = new Account(clientBill, 0);
	Account accountBillB = new Account(clientBill, 2000);
	Account accountBillC = new Account(clientBill, -1999);

	Client clientFrancis = new Client("Francis");
	Account accountFrancis = new Account(clientFrancis, 1);
	accountFrancis.close();

	Client clientLouis = new Client("Louis");
	Account accountLouisA = new Account(clientLouis, -20);
	Account accountLouisB = new Account(clientLouis, 19);
    }

    @Atomic
    private static void printDomain() {
	BankConsistencyApplication app = FenixFramework.getRoot();
	app.printDomain();
    }
}
