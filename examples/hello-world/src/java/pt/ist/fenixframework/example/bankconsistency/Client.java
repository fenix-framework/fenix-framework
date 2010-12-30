package pt.ist.fenixframework.example.bankconsistency;

import jvstm.cps.ConsistencyPredicate;

public class Client extends Client_Base {

    public Client(String name) {
	super();
	setApplication(BankConsistencyApplication.getInstance());
	setName(name);
    }

    @Override
    public String toString() {
	return getName() + " [" + getOid() + "] ";
    }

    @ConsistencyPredicate
    public boolean checkTotalBalancePositive() {
	int totalBalance = 0;
	for (Account account : getAccounts()) {
	    totalBalance += account.getBalance();
	}
	return totalBalance >= 0;
    }
}
