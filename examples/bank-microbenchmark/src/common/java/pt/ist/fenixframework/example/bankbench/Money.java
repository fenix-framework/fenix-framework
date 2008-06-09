package pt.ist.fenixframework.example.bankbench;

import java.math.BigDecimal;
import javax.persistence.*;

@Embeddable
public class Money {
    private static final BigDecimal TWO = new BigDecimal(2);

    private final BigDecimal amount;

    public static Money euros(long amount) {
        return new Money(amount);
    }

    private Money() {
        this.amount = new BigDecimal(0);
    }

    private Money(long amount) {
        this.amount = new BigDecimal(amount);
    }

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public Money getHalfMoney() {
        BigDecimal newAmount = amount.divide(TWO);
        return new Money(newAmount);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean lessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public String toString() {
        return amount.toString() + " EUR";
    }


    // this is needed by the fenix-framework
    public String exportAsString() {
        return amount.toString();
    }

    // this is needed by the fenix-framework
    public static Money fromString(String txt) {
        return new Money(new BigDecimal(txt));
    }
}
