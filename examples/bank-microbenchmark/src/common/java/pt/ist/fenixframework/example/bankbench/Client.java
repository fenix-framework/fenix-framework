package pt.ist.fenixframework.example.bankbench;

import java.util.Set;

public interface Client {
    public Money getTotalBalance();
    public Set<? extends Account> getAccounts();
}
