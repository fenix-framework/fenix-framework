package pt.ist.fenixframework.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;
import jvstm.cps.ConsistentTransaction;

import org.apache.ojb.broker.PersistenceBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;

public final class TransactionSupport {

    private static final Logger logger = LoggerFactory.getLogger(TransactionSupport.class);

    public final static TransactionStatistics STATISTICS = new TransactionStatistics();

    public static void setupJVSTM() {
        jvstm.Transaction.setTransactionFactory(new jvstm.TransactionFactory() {
            @Override
            public jvstm.Transaction makeTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new TopLevelTransaction(record);
            }

            @Override
            public jvstm.Transaction makeReadOnlyTopLevelTransaction(jvstm.ActiveTransactionsRecord record) {
                return new ReadOnlyTopLevelTransaction(record);
            }
        });

        // initialize transaction system
        int maxTx = TransactionChangeLogs.initializeTransactionSystem();
        if (maxTx >= 0) {
            logger.info("Setting the last committed TX number to {}", maxTx);
            Transaction.setMostRecentActiveRecord(new ActiveTransactionsRecord(maxTx, null));
        } else {
            throw new Error("Couldn't determine the last transaction number");
        }
    }

    private TransactionSupport() {
        // this is never to be used!!!
    }

    public static FenixTransaction currentFenixTransaction() {
        return (FenixTransaction) Transaction.current();
    }

    protected static DBChanges currentDBChanges() {
        return currentFenixTransaction().getDBChanges();
    }

    public static DomainObject readDomainObject(String classname, Integer oid) {
        return (oid == null) ? null : currentFenixTransaction().readDomainObject(classname, oid);
    }

    public static void logAttrChange(AbstractDomainObject obj, String attrName) {
        currentDBChanges().logAttrChange(obj, attrName);
    }

    public static void storeNewObject(AbstractDomainObject obj) {
        currentDBChanges().storeNewObject(obj);
        ((ConsistentTransaction) Transaction.current()).registerNewObject(obj);
    }

    public static void storeObject(AbstractDomainObject obj, String attrName) {
        currentDBChanges().storeObject(obj, attrName);
    }

    public static void deleteObject(Object obj) {
        currentDBChanges().deleteObject(obj);
    }

    public static void addRelationTuple(String relation, AbstractDomainObject obj1, String colNameOnObj1,
            AbstractDomainObject obj2, String colNameOnObj2) {
        currentDBChanges().addRelationTuple(relation, obj1, colNameOnObj1, obj2, colNameOnObj2);
    }

    public static void removeRelationTuple(String relation, AbstractDomainObject obj1, String colNameOnObj1,
            AbstractDomainObject obj2, String colNameOnObj2) {
        currentDBChanges().removeRelationTuple(relation, obj1, colNameOnObj1, obj2, colNameOnObj2);
    }

    public static PersistenceBroker getOJBBroker() {
        return currentFenixTransaction().getOJBBroker();
    }

}
