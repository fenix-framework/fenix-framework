package pt.ist.fenixframework.example.bankbench.hib;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.DomainFactory;

public class HibFactory implements DomainFactory {
    public Client makeClient(String name) {
        return new HClient(name);
    }

    public Account makeAccount(Client owner) {
        return new HAccount(owner);
    }
}
