package pt.ist.fenixframework.example.bankbench.ff;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.DomainFactory;

public class FFFactory implements DomainFactory {
    public Client makeClient(String name) {
        return new FFClient(name);
    }

    public Account makeAccount(Client owner) {
        return new FFAccount(owner);
    }
}
