package pt.ist.fenixframework.example.bankbench.ff;

import java.util.Set;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.Money;


public class FFClient extends FFClient_Base implements Client {
    
    public FFClient(String name) {
        super();
        setName(name);
    }
    
    public Set<? extends Account> getAccounts() {
        return getAccountSet();
    }

    public Money getTotalBalance() {
        Money result = Money.euros(0);

        for (Account acc : getAccounts()) {
            result = result.add(acc.getBalance());
        }

        return result;
    }
}
