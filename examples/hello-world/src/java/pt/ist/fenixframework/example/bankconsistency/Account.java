package pt.ist.fenixframework.example.bankconsistency;

import jvstm.cps.ConsistencyPredicate;

public class Account extends Account_Base {

    public Account(Client client) {
	this(client, 0);
    }

    public Account(Client client, int balance) {
	super();
	setApplication(BankConsistencyApplication.getInstance());
	setClient(client);
	setBalance(balance);
	setClosed(false);
    }

    @Override
    public String toString() {
	return getBalance() + " Eur" + (getClosed() ? " - CLOSED!" : "") + " [" + getOid() + "] ";
    }

    public boolean isClosed() {
	return getClosed();
    }

    public void close() {
	setClosed(true);
    }

    public void open() {
	setClosed(false);
    }

    public int withdraw(int amount) {
	setBalance(getBalance() - amount);
	return amount;
    }

    public void deposit(int amount) {
	setBalance(getBalance() + amount);
    }

    @ConsistencyPredicate
    public boolean checkClosedAccountHasNoMoney() {
	return (!isClosed() || getBalance() == 0);
    }
}
