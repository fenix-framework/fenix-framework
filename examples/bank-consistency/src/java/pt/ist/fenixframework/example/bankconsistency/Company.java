package pt.ist.fenixframework.example.bankconsistency;


public class Company extends Company_Base {

    public Company() {
	super();
    }

    public Company(String companyName) {
	super();
	setApplication(BankConsistencyApplication.getInstance());
	setCompanyName(companyName);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [" + getIdInternal() + "] " + getCompanyName();
    }
}
