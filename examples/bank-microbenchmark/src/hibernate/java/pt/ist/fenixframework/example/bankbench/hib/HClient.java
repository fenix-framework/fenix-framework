package pt.ist.fenixframework.example.bankbench.hib;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import pt.ist.fenixframework.example.bankbench.Account;
import pt.ist.fenixframework.example.bankbench.Client;
import pt.ist.fenixframework.example.bankbench.Money;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HClient extends PersistentObject implements Client {

    private String name;

    @OneToMany(mappedBy = "owner")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<HAccount> accounts = new HashSet<HAccount>();

    private HClient() {
    }

    public HClient(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<? extends Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account act) {
        accounts.add((HAccount)act);
    }

    public Money getTotalBalance() {
        Money result = Money.euros(0);

        for (Account acc : accounts) {
            result = result.add(acc.getBalance());
        }

        return result;
    }
}
