package pt.ist.fenixframework.pstm;

import java.sql.SQLException;
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
import jvstm.VBoxBody;
import jvstm.cps.ConsistencyCheckTransaction;
import jvstm.cps.ConsistentTopLevelTransaction;
import jvstm.cps.DependenceRecord;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.accesslayer.LookupException;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.TxIntrospector;
import pt.ist.fenixframework.pstm.DBChanges.AttrChangeLog;

public class TopLevelTransaction extends ConsistentTopLevelTransaction implements FenixTransaction, TxIntrospector {

    private static final Logger logger = LoggerFactory.getLogger(TopLevelTransaction.class);
    private static int NUM_READS_THRESHOLD = 10000000;
    private static int NUM_WRITES_THRESHOLD = 100000;

    private static final Object COMMIT_LISTENERS_LOCK = new Object();
    private static volatile Cons<CommitListener> COMMIT_LISTENERS = Cons.empty();

    public static void addCommitListener(CommitListener listener) {
	synchronized (COMMIT_LISTENERS_LOCK) {
	    COMMIT_LISTENERS = COMMIT_LISTENERS.cons(listener);
	}
    }

    public static void removeCommitListener(CommitListener listener) {
	synchronized (COMMIT_LISTENERS_LOCK) {
	    COMMIT_LISTENERS = COMMIT_LISTENERS.removeFirst(listener);
	}
    }

    private static void notifyBeforeCommit(TopLevelTransaction tx) {
	for (CommitListener cl : COMMIT_LISTENERS) {
	    cl.beforeCommit(tx);
	}
    }

    private static void notifyAfterCommit(TopLevelTransaction tx) {
	for (CommitListener cl : COMMIT_LISTENERS) {
	    cl.afterCommit(tx);
	}
    }

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

    // used by the DataAccessPatterns module
    private String contextURI = "";

    TopLevelTransaction(ActiveTransactionsRecord record) {
	super(record);

	initDbConnection(false);
	initDbChanges();
	initContext();
    }

    // initialize the information necessary for the identification of
    // the surrounding context for the acquisition of statistical data
    protected void initContext() {
	String uri = RequestInfo.getRequestURI();
	if (uri != null) {
	    this.contextURI = uri;
	}
    }

    protected String getContext() {
	return contextURI;
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
    public DomainObject readDomainObject(String classname, int oid) {
	return TransactionChangeLogs.readDomainObject(broker, classname, oid);
    }

    @Override
    public void setReadOnly() {
	// a null dbChanges indicates a read-only tx
	this.dbChanges = null;
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
	    broker.close();
	    broker = null;
	}
	dbChanges = null;
    }

    @Override
    protected void doCommit() {
	if (isWriteTransaction()) {
	    Transaction.STATISTICS.incWrites(this);
	} else {
	    Transaction.STATISTICS.incReads(this);
	}

	if ((numBoxReads > NUM_READS_THRESHOLD) || (numBoxWrites > NUM_WRITES_THRESHOLD)) {
	    logger.warn(String.format("WARN: Very-large transaction (reads = %d, writes = %d, uri = %s)", numBoxReads,
		    numBoxWrites, RequestInfo.getRequestURI()));
	}

	// reset statistics counters
	numBoxReads = 0;
	numBoxWrites = 0;

	notifyBeforeCommit(this);
	super.doCommit();
	notifyAfterCommit(this);
    }

    @Override
    protected boolean validateCommit() {
	boolean result = super.validateCommit();

	if (!result) {
	    Transaction.STATISTICS.incConflicts();
	}

	return result;
    }

    public ReadSet getReadSet() {
	return new ReadSet(bodiesRead);
    }

    @Override
    public <T> void setBoxValue(jvstm.VBox<T> vbox, T value) {
	if (dbChanges == null) {
	    throw new IllegalWriteException();
	} else {
	    numBoxWrites++;
	    super.setBoxValue(vbox, value);
	}
    }

    @Override
    public <T> void setPerTxValue(jvstm.PerTxBox<T> box, T value) {
	if (dbChanges == null) {
	    throw new IllegalWriteException();
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
			    System.out.println("Couldn't load the attribute " + attr + " for class " + obj.getClass());
			    throw new VersionNotAvailableException();
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
	    throw new IllegalWriteException();
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
			    System.out.println("Invalid commit. Restarting.");
			    throw new jvstm.CommitException();
			}
		    }
		} catch (SQLException sqlex) {
		    System.out.println("SqlException: " + sqlex.getMessage());
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
		    System.out.println("Error while commiting exception. Terminating server.");
		    System.err.flush();
		    System.out.flush();
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

    @Override
    protected void checkConsistencyPredicates() {
	// check all the consistency predicates for the objects modified in this
	// transaction
	for (Object obj : getDBChanges().getModifiedObjects()) {
	    checkConsistencyPredicates(obj);
	}

	super.checkConsistencyPredicates();
    }

    @Override
    protected void checkConsistencyPredicates(Object obj) {
	if (getDBChanges().isDeleted(obj)) {
	    // don't check deleted objects
	    return;
	} else {
	    super.checkConsistencyPredicates(obj);
	}
    }

    @Override
    protected ConsistencyCheckTransaction makeConsistencyCheckTransaction(Object obj) {
	return new FenixConsistencyCheckTransaction(this, obj);
    }

    @Override
    protected Iterator<DependenceRecord> getDependenceRecordsToRecheck() {
	// for now, just return an empty iterator
	return Util.emptyIterator();
    }

    // implement the TxIntrospector interface

    @Override
    public Set<DomainObject> getNewObjects() {
	Set<DomainObject> emptySet = Collections.emptySet();
	return isWriteTransaction() ? getDBChanges().getNewObjects() : emptySet;
    }

    @Override
    public Set<DomainObject> getModifiedObjects() {
	Set<DomainObject> emptySet = Collections.emptySet();
	return isWriteTransaction() ? getDBChanges().getModifiedObjects() : emptySet;
    }

    public boolean isDeleted(Object obj) {
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
	    AbstractDomainObject obj = (AbstractDomainObject) log.obj;
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
