package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.HashMap;
import java.util.Map;

import jvstm.ActiveTransactionsRecord;
import jvstm.Transaction;
import jvstm.VBoxBody;
import jvstm.cps.ConsistentTopLevelTransaction;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;
import pt.ist.fenixframework.core.WriteOnReadError;

public class PersistentTransaction extends ConsistentTopLevelTransaction implements StatisticsCapableTransaction/*, TxIntrospector*/{

    private static final Logger logger = LoggerFactory.getLogger(PersistentTransaction.class);

    private static int NUM_READS_THRESHOLD = 10000000;
    private static int NUM_WRITES_THRESHOLD = 100000;

    private boolean readOnly = false;

    // for statistics
    protected int numBoxReads = 0;
    protected int numBoxWrites = 0;

    public PersistentTransaction(ActiveTransactionsRecord record) {
        super(record);
        this.readOnly = false;
    }

    @Override
    public void setReadOnly() {
        this.readOnly = true;
    }

    @Override
    public int getNumBoxReads() {
        return numBoxReads;
    }

    @Override
    public int getNumBoxWrites() {
        return numBoxWrites;

    }

    @Override
    public Transaction makeNestedTransaction(boolean readOnly) {
        throw new Error("Nested transactions not supported yet...");
    }

    @Override
    protected void doCommit() {
        if (isWriteTransaction()) {
            TransactionStatistics.STATISTICS.incWrites(this);
        } else {
            TransactionStatistics.STATISTICS.incReads(this);
        }

        if ((numBoxReads > NUM_READS_THRESHOLD) || (numBoxWrites > NUM_WRITES_THRESHOLD)) {
            logger.warn("Very-large transaction (reads = {}, writes = {})", numBoxReads, numBoxWrites);
        }

        // reset statistics counters
        numBoxReads = 0;
        numBoxWrites = 0;

        super.doCommit();
    }

    // Override the commit operation to propagate the changes to the persistent repository.
    @Override
    protected Cons<VBoxBody> doCommit(int newTxNumber) {
        Cons<VBoxBody> newBodies = Cons.empty();

        JVSTMBackEnd.getInstance().getRepository().persistChanges(boxesWritten.entrySet(), newTxNumber, NULL_VALUE);

        for (Map.Entry<jvstm.VBox, Object> entry : boxesWritten.entrySet()) {
            VBox vbox = (VBox) entry.getKey();
            Object newValue = entry.getValue();
            newValue = (newValue == NULL_VALUE) ? null : newValue;

            VBoxBody newBody;
            synchronized (vbox) {
                newBody = vbox.commit(newValue, newTxNumber);
            }
            newBodies = newBodies.cons(newBody);
        }

        return newBodies;
    }

    public void setInPast(int newTxNumber) {
        setNumber(newTxNumber);
    }

    @Override
    protected boolean validateCommit() {
        ActiveTransactionsRecord mostRecentRecord = Transaction.mostRecentRecord;

        boolean result = super.validateCommit();

        if (result) {
            // upgradeTx();
            setNumber(mostRecentRecord.transactionNumber);
            // the correct order is to increment first the
            // new, and only then decrement the old
            mostRecentRecord.incrementRunning();
            this.activeTxRecord.decrementRunning();
            this.activeTxRecord = mostRecentRecord;
        } else {
            TransactionStatistics.STATISTICS.incConflicts();
        }

        return result;
    }

    @Override
    public <T> void setBoxValue(jvstm.VBox<T> vbox, T value) {
        if (!txAllowsWrite()) {
            throw new WriteOnReadError();
        } else {
            numBoxWrites++;
            super.setBoxValue(vbox, value);
        }
    }

    @Override
    public <T> void setPerTxValue(jvstm.PerTxBox<T> box, T value) {
        if (!txAllowsWrite()) {
            throw new WriteOnReadError();
        } else {
            super.setPerTxValue(box, value);
        }
    }

    @Override
    public <T> T getBoxValue(VBox<T> vbox) {
        numBoxReads++;
        T value = getLocalValue(vbox);

        if (value == null) {
            // no local value for the box

            //VBoxBody<T> body = vbox.body.getBody(number);
            VBoxBody<T> body = vbox.getBody(number);
            if (body.value == VBox.NOT_LOADED_VALUE) {
                vbox.reload();
                // after the reload, the (new) body should have the required loaded value
                // if not, then something gone wrong and its better to abort
                // body = vbox.body.getBody(number);
                body = vbox.getBody(number);
                if (body.value == VBox.NOT_LOADED_VALUE) {
                    logger.error("Couldn't load the attribute {} for class {}", vbox.getSlotName(), vbox.getOwnerObject()
                            .getClass());
                    throw new VersionNotAvailableException();
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

//    public DBChanges getDBChanges() {
//	throw new UnsupportedOperationException();
//    }

    @Override
    public boolean txAllowsWrite() {
        return !this.readOnly;
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        try {
            Cons<VBoxBody> temp = super.performValidCommit();
            return temp;
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            logger.error("Error while commiting exception. Terminating server.");
            System.exit(-1);
            return null; // never reached, but required by the compiler
        }

    }

    // implement the TxIntrospector interface
//
//    public Collection<DomainObject> getNewObjects() {
//	HashSet<DomainObject> result = new HashSet<DomainObject>();
//
//	Cons newObjects = getNewObjectsRegister();
//	for (DomainObject domainObject : (Cons<DomainObject>)newObjects) {
//	    result.add(domainObject);
//	}
//	return result;
//    }
//
//    public Collection<DomainObject> getModifiedObjects() {
//        throw new UnsupportedOperationException("not yet implemented");
//
//        /* The following code is the previous implementation.  It is broken because it does not respect the requirements.
//         * Namely, it aggregates VBoxes with the same owner, and relations are modeled using DomainBasedMaps which cause e.g. the VBoxes in these objects to: 
//         * 
//         * 1) be shown.  should they? (consider that this is internal stuff)
//         * 2) not be aggregated to the correct owner
//         
//        HashSet<DomainObject> result = new HashSet<DomainObject>(boxesWritten.size());
//
//        for (jvstm.VBox vbox : boxesWritten.keySet()) {
//            result.add(((VBox) vbox).getOwnerObject());
//        }
//        // subtract the new objects, whose vboxes were also added to the result set
//        result.removeAll(getNewObjects());
//        return result;
//        */
//    }
//
//    public Collection<DomainObject> getDirectlyModifiedObjects() {
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    public Set<Entry> getReadSetLog() {
//        Set<Entry> entries = new HashSet<Entry>(bodiesRead.size());
//
//        for (Map.Entry<jvstm.VBox,VBoxBody> entry : bodiesRead.entrySet()) {
//	    VBox vbox = (VBox)entry.getKey();
//            entries.add(new Entry(vbox.getOwnerObject(), vbox.getSlotName(), entry.getValue().value));
//        }
//        return entries;
//    }
//
//    public Set<Entry> getWriteSetLog() {
//        Set<Entry> entries = new HashSet<Entry>(boxesWritten.size());
//
//        for (Map.Entry<jvstm.VBox,Object> entry : boxesWritten.entrySet()) {
//	    VBox vbox = (VBox)entry.getKey();
//            entries.add(new Entry(vbox.getOwnerObject(), vbox.getSlotName(), entry.getValue()));
//        }
//        return entries;
//    }

    // ---------------------------------------------------------------
    // keep a log of all relation changes, which are more
    // coarse-grained and semantically meaningfull than looking only
    // at changes made to the objects that implement the relations

//    private HashMap<RelationTupleChange,RelationTupleChange> relationTupleChanges = null;
//    
//    public void logRelationAdd(String relationName, DomainObject o1, DomainObject o2) {
//        logRelationTuple(relationName, o1, o2, false);
//    }
//
//    public void logRelationRemove(String relationName, DomainObject o1, DomainObject o2) {
//        logRelationTuple(relationName, o1, o2, true);
//    }
//    
//    private void logRelationTuple(String relationName, DomainObject o1, DomainObject o2, boolean remove) {
//	if (relationTupleChanges == null) {
//	    relationTupleChanges = new HashMap<RelationTupleChange, RelationTupleChange>();
//	}
//
//	RelationTupleChange log = new RelationTupleChange(relationName, o1, o2, remove);
//	relationTupleChanges.put(log, log);
//    }
//
//
//    public Set<RelationChangelog> getRelationsChangelog() {
//        Set<RelationChangelog> entries = new HashSet<RelationChangelog>();
//
//        if (relationTupleChanges != null) {
//            for (RelationTupleChange log : relationTupleChanges.values()) {
//                entries.add(new RelationChangelog(log.relationName, log.obj1, log.obj2, log.remove));
//            }
//        }
//
//        return entries;
//    }
//
//    private static class RelationTupleChange {
//	final String relationName;
//	final DomainObject obj1;
//	final DomainObject obj2;
//	final boolean remove;
//
//	RelationTupleChange(String relationName, DomainObject obj1, DomainObject obj2, boolean remove) {
//	    this.relationName = relationName;
//	    this.obj1 = obj1;
//	    this.obj2 = obj2;
//	    this.remove = remove;
//	}
//
//	public int hashCode() {
//	    return relationName.hashCode() + obj1.hashCode() + obj2.hashCode();
//	}
//
//	public boolean equals(Object obj) {
//	    if ((obj != null) && (obj.getClass() == this.getClass())) {
//		RelationTupleChange other = (RelationTupleChange) obj;
//		return this.relationName.equals(other.relationName) && this.obj1.equals(other.obj1) && this.obj2.equals(other.obj2);
//	    } else {
//		return false;
//	    }
//	}
//    }

}
