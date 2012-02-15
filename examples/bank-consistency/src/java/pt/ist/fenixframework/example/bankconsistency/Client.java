package pt.ist.fenixframework.example.bankconsistency;

import jvstm.cps.ConsistencyPredicate;

public class Client extends Client_Base {

    public Client() {
	super();
    }

    public Client(String name) {
	super();
	setApplication(BankConsistencyApplication.getInstance());
	setName(name);
    }

    public void delete() {
	deleteDomainObject();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [" + getIdInternal() + "] " + getName();
    }

    @ConsistencyPredicate
    public boolean checkMultiplicityOfAccount() {
	return hasAnyAccounts();
    }

    /*@ConsistencyPredicate
    public boolean allowChanges() {
    return false;
    }*/

    /*@ConsistencyPredicate
    public boolean performIllegalWrite() {
    setName("");
    return true;
    }*/
}
