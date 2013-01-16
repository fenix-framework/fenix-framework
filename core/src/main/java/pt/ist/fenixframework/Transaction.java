package pt.ist.fenixframework;

import pt.ist.fenixframework.txintrospector.TxIntrospector;

/**
 * Fenix Framework's Interface for {@link Transaction}s. This interface mostly
 * extends JTA's {@link javax.transaction.Transaction}, and adds the capability
 * to introspect the changes performed by this transaction.
 * 
 * Please refer to the documentation in each individual backend for the list of
 * supported operations.
 * 
 * @see javax.transaction
 * 
 */
public interface Transaction extends javax.transaction.Transaction {

    /**
     * Get the TxIntrospector object that contains the changes caused by this
     * transaction. Multiple calls to this method will return the same object.
     * 
     * @return TxIntrospector The TxIntrospector object associated with this
     *         transaction.
     */
    public TxIntrospector getTxIntrospector();

}
