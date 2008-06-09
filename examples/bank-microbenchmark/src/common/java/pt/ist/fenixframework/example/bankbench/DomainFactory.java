package pt.ist.fenixframework.example.bankbench;

public interface DomainFactory {
    public Client makeClient(String name);
    public Account makeAccount(Client owner);
}
