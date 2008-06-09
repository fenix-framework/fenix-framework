package pt.ist.fenixframework.example.bankbench.hib;

import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.Money;
import pt.ist.fenixframework.example.bankbench.InsufficientFundsException;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HAccount extends PersistentObject implements Account {

    private Money balance;

    @ManyToOne
    private HClient owner;

    private HAccount() {
    }

    public HAccount(Client owner) {
        this.balance = Money.euros(0);
        this.owner = (HClient)owner;
        ((HClient)owner).addAccount(this);
    }

    public Money getBalance() {
        return balance;
    }

    public Client getOwner() {
        return owner;
    }

    public void withdraw(Money amount) {
        if (balance.lessThan(amount)) {
            throw new InsufficientFundsException();
        }

        this.balance = this.balance.subtract(amount);
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    public void transfer(Account dst, Money amount) {
        this.withdraw(amount);
        dst.deposit(amount);
    }
}
