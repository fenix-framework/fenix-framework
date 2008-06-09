package pt.ist.fenixframework.example.bankbench.ff;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.Money;
import pt.ist.fenixframework.example.bankbench.InsufficientFundsException;

public class FFAccount extends FFAccount_Base implements Account {

    private static final Money ZERO = Money.euros(0);
    
    public FFAccount(Client owner) {
        super();
        setOwner((FFClient) owner);
        setBalance(ZERO);
    }
    
    public void withdraw(Money amount) {
        if (getBalance().lessThan(amount)) {
            throw new InsufficientFundsException();
        }

        setBalance(getBalance().subtract(amount));
    }

    public void deposit(Money amount) {
        setBalance(getBalance().add(amount));
    }

    public void transfer(Account dst, Money amount) {
        this.withdraw(amount);
        dst.deposit(amount);
    }
}
