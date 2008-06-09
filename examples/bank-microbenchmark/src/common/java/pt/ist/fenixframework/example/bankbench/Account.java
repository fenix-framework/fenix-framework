package pt.ist.fenixframework.example.bankbench;

public interface Account {
    public Money getBalance();
    public Client getOwner();
    public void withdraw(Money amount);
    public void deposit(Money amount);
    public void transfer(Account dst, Money amount);
}
