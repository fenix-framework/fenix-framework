package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import jvstm.ActiveTransactionsRecord;
import jvstm.CommitException;
import jvstm.ResumeException;
import jvstm.Transaction;
import jvstm.VBoxBody;
import jvstm.cps.ChainedIterator;
import jvstm.cps.ConsistencyCheckTransaction;
import jvstm.cps.ConsistencyException;
import jvstm.cps.ConsistentTopLevelTransaction;
import jvstm.cps.Depended;
import jvstm.cps.DependenceRecord;
import jvstm.util.Cons;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;
import pt.ist.fenixframework.backend.jvstmojb.pstm.DBChanges.AttrChangeLog;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicate;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicateSystem;
import pt.ist.fenixframework.consistencyPredicates.DomainConsistencyPredicate;
import pt.ist.fenixframework.consistencyPredicates.DomainDependenceRecord;
import pt.ist.fenixframework.core.WriteOnReadError;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

public class TopLevelTransaction extends ConsistentTopLevelTransaction implements FenixTransaction, TxIntrospector {

    private static final Logger logger = LoggerFactory.getLogger(TopLevelTransaction.class);

    private static int NUM_READS_THRESHOLD = 10000000;
    private static int NUM_WRITES_THRESHOLD = 100000;

    public static Lock getCommitlock() {
        return COMMIT_LOCK;
    }

    // The following variable is not updated atomically (with a CAS,
    // or something similar), because the accuracy of its value is not
    // critical. It may happen that a starting transaction puts a
    // lower value into this variable, when racing to update it, but
    // that means that a new transaction may open a new dbConnection
    // when it was not needed, but that is OK.
    protected volatile static long lastDbConnectionTimestamp = 0;

    static boolean lastDbConnectionWithin(long millis) {
        return (System.currentTimeMillis() - lastDbConnectionTimestamp) < millis;
    }

    // Each TopLevelTx has its DBChanges
    // If this slot is changed to null, it is an indication that the
    // transaction does not allow more changes
    private DBChanges dbChanges = null;

    private PersistenceBroker broker;

    // for statistics
    protected int numBoxReads = 0;
    protected int numBoxWrites = 0;

    TopLevelTransaction(ActiveTransactionsRecord record) {
        super(record);

        initDbConnection(false);
        initDbChanges();
    }

    protected void initDbConnection(boolean resuming) {
        // first, get a new broker that will give access to the DB connection
        this.broker = PersistenceBrokerFactory.defaultPersistenceBroker();

        // update the lastDbConnectionTimestamp with the current time
        long now = System.currentTimeMillis();
        if (now > lastDbConnectionTimestamp) {
            lastDbConnectionTimestamp = now;
        }

        // open a connection to the database and set this tx number to the
        // number that
        // corresponds to that connection number. The connection number should
        // always be
        // greater than the current number, because the current number is
        // obtained from
        // Transaction.getCommitted, which is set only after the commit to the
        // database
        ActiveTransactionsRecord newRecord = updateFromTxLogsOnDatabase(this.activeTxRecord);
        if (newRecord != this.activeTxRecord) {
            // if a new record is returned, that means that this transaction
            // will belong
            // to that new record, so we must take it off from its current
            // record and set
            // it properly

            // but, if we are resuming, we must ensure first that the
            // transaction is still valid for the new transaction
            // record
            if (resuming) {
                checkValidity(newRecord);
            }

            newRecord.incrementRunning();
            this.activeTxRecord.decrementRunning();
            this.activeTxRecord = newRecord;
            setNumber(newRecord.transactionNumber);
        }
    }

    protected void checkValidity(ActiveTransactionsRecord record) {
        // we must see whether any of the boxes read by this
        // transaction was changed by some transaction upto the one
        // corresponding to the new record (newer ones don't matter)

        int newTxNumber = record.transactionNumber;

        for (Map.Entry<jvstm.VBox, VBoxBody> entry : this.bodiesRead.entrySet()) {
            if (entry.getKey().body.getBody(newTxNumber) != entry.getValue()) {
                throw new ResumeException("Transaction is no longer valid for resuming");
            }
        }
    }

    @Override
    protected void suspendTx() {
        // close the broker to release the db connection on suspension
        if (broker != null) {
            if (broker.isInTransaction()) {
                broker.abortTransaction();
            }
            broker.close();
            broker = null;
        }

        super.suspendTx();
    }

    @Override
    protected void resumeTx() {
        super.resumeTx();
        initDbConnection(true);
    }

    protected void initDbChanges() {
        this.dbChanges = new DBChanges();
    }

    @Override
    public PersistenceBroker getOJBBroker() {
        return broker;
    }

    @Override
    public void setReadOnly() {
        // a null dbChanges indicates a read-only tx
        this.dbChanges = null;
    }

    public boolean isReadOnly() {
        // a null dbChanges indicates a read-only tx
        return this.dbChanges == null;
    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        throw new Error("Nested transactions not supported yet...");
    }

    private ActiveTransactionsRecord updateFromTxLogsOnDatabase(ActiveTransactionsRecord record) {
        try {
            return TransactionChangeLogs.updateFromTxLogsOnDatabase(getOJBBroker(), record);
        } catch (Exception sqle) {
            // sqle.printStackTrace();
            throw new Error("Error while updating from FF$TX_CHANGE_LOGS: Cannot proceed: " + sqle.getMessage(), sqle);
        }
    }

    @Override
    protected void finish() {
        super.finish();
        if (broker != null) {
            if (broker.isInTransaction()) {
                broker.abortTransaction();
            }
            broker.close();
            broker = null;
        }
        dbChanges = null;
    }

    @Override
    protected void doCommit() {
        if (isWriteTransaction()) {
            TransactionSupport.STATISTICS.incWrites(this);
        } else {
            TransactionSupport.STATISTICS.incReads(this);
        }

        if ((numBoxReads > NUM_READS_THRESHOLD) || (numBoxWrites > NUM_WRITES_THRESHOLD)) {
            logger.warn("Very-large transaction (reads = {}, writes = {})", numBoxReads, numBoxWrites);
        }

        // reset statistics counters
        numBoxReads = 0;
        numBoxWrites = 0;

        super.doCommit();
    }

    @Override
    protected boolean validateCommit() {
        boolean result = super.validateCommit();

        if (!result) {
            TransactionSupport.STATISTICS.incConflicts();
        }

        return result;
    }

    public ReadSet getReadSet() {
        return new ReadSet(bodiesRead);
    }

    @Override
    public <T> void setBoxValue(jvstm.VBox<T> vbox, T value) {
        if (dbChanges == null) {
            throw new WriteOnReadError();
        } else {
            numBoxWrites++;
            super.setBoxValue(vbox, value);
        }
    }

    @Override
    public <T> void setBoxValueInParent(VBox<T> vbox, T value) {
        // A TopLevelTransaction has no parent, so just set the box value.
        setBoxValue(vbox, value);
    }

    @Override
    public <T> void setPerTxValue(jvstm.PerTxBox<T> box, T value) {
        if (dbChanges == null) {
            throw new WriteOnReadError();
        } else {
            super.setPerTxValue(box, value);
        }
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox, Object obj, String attr) {
        numBoxReads++;
        T value = getLocalValue(vbox);

        if (value == null) {
            // no local value for the box

            VBoxBody<T> body = vbox.body.getBody(number);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                synchronized (body) {
                    if (body.value == VBox.NOT_LOADED_VALUE) {
                        vbox.reload(obj, attr);
                        // after the reload, the same body should have a new
                        // value
                        // if not, then something gone wrong and its better to
                        // abort
                        if (body.value == VBox.NOT_LOADED_VALUE) {
                            logger.error("Couldn't load the attribute {} for class {}", attr, obj.getClass());
                            throw new VersionNotAvailableException("Couldn't load the attribute " + attr + " for instance " + obj);
                        }
                    }
                }
            }

            if (bodiesRead == EMPTY_MAP) {
                bodiesRead = new HashMap<jvstm.VBox, VBoxBody>();
            }
            bodiesRead.put(vbox, body);
            value = body.value;
        }

        return (value == NULL_VALUE) ? null : value;
    }

    @Override
    public boolean isBoxValueLoaded(VBox vbox) {
        Object localValue = getLocalValue(vbox);

        if (localValue == VBox.NOT_LOADED_VALUE) {
            return false;
        }

        if (localValue != null) {
            return true;
        }

        VBoxBody body = vbox.body.getBody(number);
        return (body.value != VBox.NOT_LOADED_VALUE);
    }

    @Override
    public DBChanges getDBChanges() {
        if (dbChanges == null) {
            // if it is null, it means that the transaction is a read-only
            // transaction
            throw new WriteOnReadError();
        } else {
            return dbChanges;
        }
    }

    @Override
    public boolean isWriteTransaction() {
        return ((dbChanges != null) && dbChanges.needsWrite()) || super.isWriteTransaction();
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        // in memory everything is ok, but we need to check against the db
        PersistenceBroker pb = getOJBBroker();

        int currentPriority = Thread.currentThread().getPriority();
        try {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

            try {
                if (!pb.isInTransaction()) {
                    pb.beginTransaction();
                }
                try {
                    // the updateFromTxLogs is made with the txNumber minus 1 to
                    // ensure that the select
                    // for update will return at least a record, and, therefore,
                    // lock the record
                    // otherwise, the mysql server may allow the select for
                    // update to continue
                    // concurrently with other executing commits in other
                    // servers
                    ActiveTransactionsRecord myRecord = this.activeTxRecord;
                    if (TransactionChangeLogs.updateFromTxLogsOnDatabase(pb, myRecord, true) != myRecord) {
                        // the cache may have been updated, so perform the
                        // tx-validation again
                        if (!validateCommit()) {
                            logger.warn("Invalid commit. Restarting.");
                            throw new jvstm.CommitException();
                        }
                    }
                } catch (SQLException sqlex) {
                    logger.warn("SqlException: " + sqlex.getMessage());
                    throw new CommitException();
                } catch (LookupException le) {
                    throw new Error("Error while obtaining database connection", le);
                }

                Cons<VBoxBody> newBodies = super.performValidCommit();
                // ensure that changes are visible to other TXs before releasing
                // lock
                try {
                    pb.commitTransaction();
                } catch (Throwable t) {
                    t.printStackTrace();
                    logger.error("Error while commiting exception. Terminating server.");
                    System.exit(-1);
                }
                pb = null;
                return newBodies;
            } finally {
                if (pb != null) {
                    pb.abortTransaction();
                }
            }
        } finally {
            Thread.currentThread().setPriority(currentPriority);
        }
    }

    @Override
    protected Cons<VBoxBody> doCommit(int newTxNumber) {
        persistTransaction(newTxNumber);
        TransactionCommitRecords.addCommitRecord(newTxNumber, dbChanges.getModifiedObjects());
        return super.doCommit(newTxNumber);
    }

    protected void persistTransaction(int newTxNumber) {
        try {
            dbChanges.makePersistent(getOJBBroker(), newTxNumber);
        } catch (SQLException sqle) {
            throw new Error("Error while accessing database", sqle);
        } catch (LookupException le) {
            throw new Error("Error while obtaining database connection", le);
        }
        // calling the dbChanges.cache() method is no longer needed,
        // given that we are caching objects as soon as they are
        // instantiated
        // dbChanges.cache();
    }

    // consistency-predicates-system methods

    /**
     * Checks all the necessary consistency predicates for this transaction to commit
     */
    @Override
    protected void checkConsistencyPredicates() {
        // checks the consistency predicates declared by the objects modified in this transaction
        for (Object obj : getDBChanges().getModifiedObjects()) {
            checkConsistencyPredicates(obj);
        }

        // checks new objects and dependence records (if any)
        super.checkConsistencyPredicates();
    }

    /**
     * Checks the consistency predicates that depend on a changed object
     */
    @Override
    protected void recheckDependenceRecord(DependenceRecord dependence) {
        if (!JvstmOJBConfig.canCreateDomainMetaObjects()) {
            // This should never happen
            throw new Error("Cannot recheck dependence records unless the framework is allowed to create meta objects");
        }
        DomainDependenceRecord dependenceRecord = (DomainDependenceRecord) dependence;
        AbstractDomainObject object = (AbstractDomainObject) dependenceRecord.getDependent();
        Pair pair = checkPredicateForOneObject(object, dependenceRecord.getPredicate(), dependenceRecord.isConsistent());
        if (pair == null) {
            // a null return means that the predicate was already checked
            return;
        }
        // If an object is consistent and depends only on itself, the DomainDependenceRecord is not necessary.
        if (isConsistent(pair) && dependsOnlyOnItself(pair)) {
            dependenceRecord.delete();
            return;
        }

        Set<Depended> newDependedSet = (Set<Depended>) pair.first;

        for (Depended oldDepended : dependenceRecord.getDependedDomainMetaObjectSet()) {
            if (!newDependedSet.remove(oldDepended)) {
                // if we didn't find the oldDepended in the newDepended, it's
                // because it is no longer a depended, so remove the dependence
                oldDepended.removeDependence(dependenceRecord);
            }
        }

        // the elements remaining in the set newDepended are new and
        // should be added to the dependence record
        // likewise, the dependence record should be added to those depended
        for (Depended newDepended : newDependedSet) {
            newDepended.addDependence(dependenceRecord);
        }

        // update the consistent value of the dependence record
        dependenceRecord.setConsistent((Boolean) pair.second);
    }

    /**
     * Checks all the consistency predicates declared by an object.
     * 
     * @param object
     *            an object that was created or modified, and whose predicates
     *            must be checked
     */
    @Override
    protected void checkConsistencyPredicates(Object object) {
        if (getDBChanges().isDeleted(object)) {
            // Deleted objects no longer have to be consistent
            return;
        }
        if (object.getClass().isAnnotationPresent(NoDomainMetaObjects.class)) {
            return;
        }
        AbstractDomainObject ado = (AbstractDomainObject) object;
        if (!JvstmOJBConfig.canCreateDomainMetaObjects()) {
            for (Method predicate : ConsistencyPredicateSystem.getPredicatesFor(object)) {
                checkPredicateForOneObject(ado, predicate, true);
            }
            return;
        }

        // First, check for existing dependence records of modified objects
        // Nothing will happen for new objects
        DomainMetaObject metaObject = ado.getDomainMetaObject();
        for (DomainDependenceRecord dependenceRecord : metaObject.getOwnDependenceRecordSet()) {
            recheckDependenceRecord(dependenceRecord);
        }

        // Then, check the consistency predicates declared by the object itself
        DomainMetaClass metaClass = metaObject.getDomainMetaClass();
        for (DomainConsistencyPredicate domainPredicate : metaClass.getAllConsistencyPredicates()) {
            Method predicate = domainPredicate.getPredicate();
            Pair pair = checkPredicateForOneObject(ado, predicate, true);
            // Predicates that were already checked return null
            if (pair != null) {
                // If an object is consistent and depends only on itself, the DomainDependenceRecord is not necessary.
                if (!(isConsistent(pair) && dependsOnlyOnItself(pair))) {
                    new DomainDependenceRecord(object, domainPredicate, (Set<Depended>) pair.first, (Boolean) pair.second);
                }
            }
        }
    }

    public static boolean isConsistent(Pair pair) {
        return (Boolean) pair.second;
    }

    public static boolean dependsOnlyOnItself(Pair pair) {
        return ((Set<Depended>) pair.first).isEmpty();
    }

    @Override
    protected ConsistencyCheckTransaction makeConsistencyCheckTransaction(Object obj) {
        return new FenixConsistencyCheckTransaction(this, obj);
    }

    /**
     * @return an <code>Iterator</code> of {@link DependenceRecord}s that have
     *         to be rechecked, based on the boxes (objects) written by the
     *         current transaction.
     */
    @Override
    protected Iterator<DependenceRecord> getDependenceRecordsToRecheck() {
        if (!JvstmOJBConfig.canCreateDomainMetaObjects()) {
            // Dependence records are not used if the FenixFramework is not configured
            // to create meta objects
            return Util.emptyIterator();
        }

        Cons<Iterator<DependenceRecord>> iteratorsList = Cons.empty();

        // The body of the for may cause the consolidation of elements of RelationLists,
        // which in turn modifies the boxesWritten.
        // So, create a copy of the boxesWritten, to avoid ConcurrentModificationExceptions
        Set<jvstm.VBox> currentBoxesWritten = new HashSet<jvstm.VBox>(boxesWritten.keySet());
        for (jvstm.VBox box : currentBoxesWritten) {
            Depended dep = ((AbstractDomainObject) ((VBox) box).getOwnerObject()).getDomainMetaObject();
            if (dep != null) {
                iteratorsList = iteratorsList.cons(dep.getDependenceRecords().iterator());
            }
        }

        return new ChainedIterator<DependenceRecord>(iteratorsList.iterator());
    }

    /**
     * Checks one predicate for one {@link AbstractDomainObject}.
     * 
     * @return the set of other objects on which the check depended (excluding
     *         the object itself), or null, if this predicate was already
     *         checked for this object (in which case the check is skipped)
     **/
    protected Pair checkPredicateForOneObject(AbstractDomainObject obj, Method predicate, boolean wasConsistent) {
        Pair toCheck = new Pair(obj, predicate);

        if (getDBChanges().isDeleted(obj)) {
            // Deleted objects no longer have to be consistent
            return null;
        }

        if (alreadyChecked.contains(toCheck)) {
            // returning null means that no check was actually done, because it
            // is repeated
            return null;
        }

        alreadyChecked.add(toCheck);

        ConsistencyCheckTransaction tx = makeConsistencyCheckTransaction(obj);
        tx.start();

        boolean predicateOk = false;
        boolean finished = false;

        Class<? extends ConsistencyException> excClass = null;
        boolean inconsistencyTolerant = false;
        ConsistencyPredicate consistencyPredicateAnnotation = predicate.getAnnotation(ConsistencyPredicate.class);
        if (consistencyPredicateAnnotation != null) {
            excClass = consistencyPredicateAnnotation.value();
            inconsistencyTolerant = consistencyPredicateAnnotation.inconsistencyTolerant();
        } else {
            jvstm.cps.ConsistencyPredicate consistencyPredicateJVSTMAnnotation =
                    predicate.getAnnotation(jvstm.cps.ConsistencyPredicate.class);
            excClass = consistencyPredicateJVSTMAnnotation.value();
        }
        try {
            predicateOk = (Boolean) (predicate.invoke(obj));
            Transaction.commit();
            finished = true;
        } catch (InvocationTargetException ite) {
            if (inconsistencyTolerant && !wasConsistent) {
                // if the predicate is inconsistency-tolerant, and we are editing an object
                // whose previous state was also inconsistent, the transaction should still
                // proceed and commit.

                // Do not register the own object as a depended
                Set<Depended> depended = tx.getDepended();
                if (!depended.isEmpty()) {
                    depended.remove(obj.getDomainMetaObject());
                }
                return new Pair(depended, false);
            }
            Throwable cause = ite.getCause();

            ConsistencyException exc;

            // only wrap the cause if it is not a ConsistencyException already
            if (cause instanceof ConsistencyException) {
                exc = (ConsistencyException) cause;
            } else {
                try {
                    exc = excClass.newInstance();
                } catch (Throwable t) {
                    throw new Error(t);
                }
                exc.initCause(cause);
            }

            exc.init(obj, predicate);
            throw exc;
        } catch (Throwable t) {
            // any other kind of throwable is an Error in the framework that should
            // be fixed
            throw new Error(t);
        } finally {
            if (!finished) {
                Transaction.abort();
            }
        }

        if (predicateOk) {
            // Do not register the own object as a depended
            Set<Depended> depended = tx.getDepended();
            if (!depended.isEmpty()) {
                depended.remove(obj.getDomainMetaObject());
            }
            return new Pair(depended, true);
        } else if (inconsistencyTolerant && !wasConsistent) {
            // if the predicate is inconsistency-tolerant, and we are editing an object
            // whose previous state was also inconsistent, the transaction should still
            // proceed and commit.

            // Do not register the own object as a depended
            Set<Depended> depended = tx.getDepended();
            if (!depended.isEmpty()) {
                depended.remove(obj.getDomainMetaObject());
            }
            return new Pair(depended, false);
        }
        ConsistencyException exc;
        try {
            exc = excClass.newInstance();
        } catch (Throwable t) {
            throw new Error(t);
        }
        exc.init(obj, predicate);
        throw exc;
    }

    public static final class Pair {
        public final Object first;
        public final Object second;

        public Pair(Object first, Object second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode() {
            return first.hashCode() + second.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            if (other.getClass() != Pair.class) {
                return false;
            }

            Pair p2 = (Pair) other;

            return (p2.first == first) && (p2.second == second);
        }
    }

    // implement the TxIntrospector interface

    @Override
    public Collection<DomainObject> getNewObjects() {
        return isWriteTransaction() ? getDBChanges().getNewObjects() : Collections.<DomainObject> emptySet();
    }

    @Override
    public Collection<DomainObject> getModifiedObjects() {
        return isWriteTransaction() ? getDBChanges().getModifiedObjects() : Collections.<DomainObject> emptySet();
    }

    @Override
    public Collection<DomainObject> getDirectlyModifiedObjects() {
        // TODO Finish this method...
        return getModifiedObjects();
    }

    @Override
    public boolean isDeleted(DomainObject obj) {
        return isWriteTransaction() ? getDBChanges().isDeleted(obj) : false;
    }

    @Override
    public Set<Entry> getReadSetLog() {
        throw new Error("getReadSetLog not implemented yet");
        // Set<Entry> entries = new HashSet<Entry>(bodiesRead.size());

        // for (Map.Entry<VBox,VBoxBody> entry : bodiesRead.entrySet()) {
        // entries.add(new Entry());
        // }

        // return entries;
    }

    @Override
    public Set<Entry> getWriteSetLog() {
        Set<AttrChangeLog> attrChangeLogs = getDBChanges().getAttrChangeLogs();
        Set<Entry> entries = new HashSet<Entry>(attrChangeLogs.size());

        for (AttrChangeLog log : attrChangeLogs) {
            AbstractDomainObject obj = log.obj;
            entries.add(new Entry(obj, log.attr, obj.getCurrentValueFor(log.attr)));
        }

        return entries;
    }

    // ---------------------------------------------------------------
    // keep a log of all relation changes, which are more
    // coarse-grained and semantically meaningfull than looking only
    // at changes made to the objects that implement the relations

    private HashMap<RelationTupleChange, RelationTupleChange> relationTupleChanges = null;

    @Override
    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2) {
        logRelationTuple(relationName, o1, o2, false);
    }

    @Override
    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2) {
        logRelationTuple(relationName, o1, o2, true);
    }

    private void logRelationTuple(String relationName, DomainObject o1, DomainObject o2, boolean remove) {
        if (relationTupleChanges == null) {
            relationTupleChanges = new HashMap<RelationTupleChange, RelationTupleChange>();
        }

        RelationTupleChange log = new RelationTupleChange(relationName, o1, o2, remove);
        relationTupleChanges.put(log, log);
    }

    @Override
    public Set<RelationChangelog> getRelationsChangelog() {
        Set<RelationChangelog> entries = new HashSet<RelationChangelog>();

        if (relationTupleChanges != null) {
            for (RelationTupleChange log : relationTupleChanges.values()) {
                entries.add(new RelationChangelog(log.relationName, log.obj1, log.obj2, log.remove));
            }
        }

        return entries;
    }

    private static class RelationTupleChange {
        final String relationName;
        final DomainObject obj1;
        final DomainObject obj2;
        final boolean remove;

        RelationTupleChange(String relationName, DomainObject obj1, DomainObject obj2, boolean remove) {
            this.relationName = relationName;
            this.obj1 = obj1;
            this.obj2 = obj2;
            this.remove = remove;
        }

        @Override
        public int hashCode() {
            return relationName.hashCode() + obj1.hashCode() + obj2.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj.getClass() == this.getClass())) {
                RelationTupleChange other = (RelationTupleChange) obj;
                return this.relationName.equals(other.relationName) && this.obj1.equals(other.obj1)
                        && this.obj2.equals(other.obj2);
            } else {
                return false;
            }
        }
    }
}
