package pt.ist.fenixframework;

import pt.ist.fenixframework.pstm.Transaction;

public class TxNumber {
    private Long number;

    public TxNumber() {
        this.number = null;
    }

    // This constructor is to be used only by the internalize method
    private TxNumber(long value) {
        this.number = value;
    }

    public long getNumber() {
        if (number == null) {
            throw new IllegalStateException();
        } else {
            return number;
        }
    }

    public int hashCode() {
        return (number == null) ? 0 : number.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if ((o != null) && (o.getClass() == this.getClass())) {
            TxNumber on = (TxNumber) o;
            // If this number or the other number is still null, then
            // we don't know for sure whether both objects will be
            // equals or not (it depends on the final numbers that
            // they get).
            // So, throw an IllegalStateException
            if ((this.number == null) || (on.number == null)) {
                throw new IllegalStateException();
            }

            return (this.number.longValue() == on.number.longValue());
        }
        return false;
    }

    public static long externalize(TxNumber num) {
        if (num.number == null) {
            num.number = Long.valueOf(Transaction.current().getNumber());
        }
        return num.number;
    }
        
    public static TxNumber internalize(long value) {
        return new TxNumber(value);
    }
}
