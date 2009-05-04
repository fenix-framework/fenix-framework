package pt.ist.fenixframework.example.bankbench.ff;

import pt.ist.fenixframework.FenixFramework;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.DomainFactory;

public class FFFactory implements DomainFactory {
    public Client makeClient(String name) {
        BankBenchApp bank = FenixFramework.getRoot();
        return bank.createClient(name);
    }

    public Account makeAccount(Client owner) {
        return new FFAccount(owner);
    }
}
