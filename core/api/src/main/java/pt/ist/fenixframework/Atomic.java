package pt.ist.fenixframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Syntactic sugar for enabling a programmer to specify that a given method should be executed within a transaction.
 */
@Target(ElementType.METHOD)
public @interface Atomic {

    enum TxMode {
        READ, WRITE, SPECULATIVE_READ
    };

    /**
     * Select the mode in which to execute the transaction.
     * 
     * <ol>
     * <li>TxMode.READ: will execute a read-only transaction and abort (terminate with rollback) if a write is attempted;</li>
     * <li>TxMode.WRITE: a normal read-write transaction is executed</li>
     * <li>TxMode.SPECULATIVE_READ: a read-only transaction is attempted. If a write occurs, the transaction will be upgraded to a
     * read-write transaction (possibly causing the original transaction to restart).</li>
     * </ol>
     * 
     * @return The mode in which the transaction will execute.
     */
    TxMode mode() default TxMode.SPECULATIVE_READ;

    /**
     * Whether to flatten the execution of transactions within transactions
     * 
     * @return <code>true</code> if any request for a new transaction within an existing transaction should simply reuse the
     *         calling transaction's execution context. <code>false</code> otherwise.
     */
    boolean flattenNested() default true;
}
