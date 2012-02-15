package pt.ist.fenixframework.example.bankconsistency;

public class ClientInfo extends ClientInfo_Base {

    public ClientInfo() {
	super();
    }

    public ClientInfo(Client client, String idNumber) {
	super();
	setClient(client);
	setIdNumber(idNumber);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [" + getIdInternal() + "] " + getIdNumber();
    }

    public void delete() {
	deleteDomainObject();
    }
}
