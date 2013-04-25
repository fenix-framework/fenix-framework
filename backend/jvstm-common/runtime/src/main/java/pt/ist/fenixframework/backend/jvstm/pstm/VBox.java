package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.List;
import java.util.ListIterator;

import jvstm.Transaction;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstm.FenixVBox;

public class VBox<E> extends jvstm.VBox<E> implements VersionedSubject, FenixVBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(VBox.class);

    static final Object NOT_LOADED_VALUE = new Object();
    static final VBoxBody NOT_LOADED_BODY = new VBoxBody(notLoadedValue(), 0, null);

    //initialized in the constructor
    protected final DomainObject ownerObj;
    protected final String slotName;

    public static <T> T notLoadedValue() {
        return (T) NOT_LOADED_VALUE;
    }

    private VBox() {
        throw new UnsupportedOperationException(
                "this constructor should not be used. FenixFramework VBoxes require an owner and a slotName.");
    }

    private VBox(E initial) {
        throw new UnsupportedOperationException(
                "this constructor should not be used. FenixFramework VBoxes require an owner and a slotName.");
    }

    private VBox(VBoxBody<E> body) {
        throw new UnsupportedOperationException(
                "this constructor should not be used. FenixFramework VBoxes require an owner and a slotName.");
    }

    public VBox(DomainObject ownerObj, String slotName) {
        super();
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    public VBox(DomainObject ownerObj, String slotName, E initial) {
        super(initial);
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    protected VBox(DomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(body);
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    public DomainObject getOwnerObject() {
        return this.ownerObj;
    }

    public String getSlotName() {
        return this.slotName;
    }

    @Override
    public E get(/*Object obj, String attrName*/) {
        return ((JvstmInFenixTransaction) Transaction.current()).getBoxValue(this);
    }

    @Override
    public void put(/*Object obj, String attrName, */E newValue) {
//        // TODO: eventually remove this 
//        if (!(attrName.equals("idInternal") || attrName.equals("ackOptLock"))) {
//            // the set of the idInternal or ackOptLock is performed by OJB and should not be logged
//            Transaction.storeObject((DomainObject) obj, attrName);
//        }
        super.put(newValue);
    }

//    public boolean hasValue() {
//        throw new UnsupportedOperationException("not yet implemented");
////        return ((JvstmInFenixTransaction)Transaction.current()).isBoxValueLoaded(this);
//    }

    public void putNotLoadedValue() {
        this.put(VBox.<E> notLoadedValue());
    }

//    protected void persistentLoad(E value) {
//        throw new UnsupportedOperationException("not yet implemented");
////        int txNumber = Transaction.current().getNumber();
////        persistentLoad(value, txNumber);
//    }

//    public void persistentLoad(Object value, int txNumber) {
//        // find appropriate body
//        VBoxBody<E> body = this.body.getBody(txNumber);
//        if (body.value == NOT_LOADED_VALUE) {
//            body.value = (E) value;
//        }
//    }

    @Override
    public synchronized VBoxBody addNewVersion(/*String attr, */int txNumber) {
        if (body.version < txNumber) {
            return commit(VBox.<E> notLoadedValue(), txNumber);
        } else {
            // when adding a new version to a box it may happen that a
            // version with the same number exists already, if we are
            // processing the change logs in the same server that
            // committed those changelogs, between the time when the
            // changelog were written to the database and the commit
            // finishes setting the committed tx number
            //
            // so, do nothing and just return null

            //System.out.println("!!! WARNING !!!: adding older version for a box attr " + attr + " -> " + body.version + " not < " + txNumber);
            return null;
        }
    }

    @Override
    public Object getCurrentValue(/*Object obj, String attrName*/) {
        return this.get(/*obj, attrName*/);
    }

    // synchronized here processes reloads of the same box one at a time, thus avoiding concurrent accesses to the persistence to
    // load the same box
    synchronized boolean reload(/*Object obj, String attr*/) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reload VBox: {} for {}", this.slotName, this.ownerObj.getExternalId());
        }

        try {
            //Re-test to see whether some other thread already did the job for us.
            //This also requires the body's value slot to be final.

            //VBoxBody<E> body = this.body.getBody(Transaction.current().getNumber());
            VBoxBody<E> body = getBody(Transaction.current().getNumber());
            if (body.value == VBox.NOT_LOADED_VALUE) {
                doReload(/*obj, attr*/);
            }
            return true;
        } catch (Throwable e) {
            // what to do?
            logger.warn("Couldn't reload attribute '" + slotName + "' for '" + ownerObj.getExternalId() + "': " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    public final VBoxBody<E> getBody(int maxVersion) {
        VBoxBody<E> current = body;

        while (current != null && current.version > maxVersion) {
            current = current.next;
        }
        if (current == null) {
            return NOT_LOADED_BODY; // VBox.NOT_LOADED_VALUE;
        }

        return current;
    }

    protected void doReload(/*Object obj, String attr*/) {
        throw new Error("Can't reload a simple VBox.  Use a PrimitiveBox or a ReferenceBox instead.");
    }

    public static <T> VBox<T> makeNew(DomainObject ownerObj, String slotName, boolean allocateOnly, boolean isReference) {
        if (isReference) {
            if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
                return new ReferenceBox<T>(ownerObj, slotName, makeNewBody((T) NOT_LOADED_VALUE, 0, null));
            } else {
                return new ReferenceBox<T>(ownerObj, slotName);
            }
        } else {
            if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
                return new PrimitiveBox<T>(ownerObj, slotName, makeNewBody((T) NOT_LOADED_VALUE, 0, null));
            } else {
                return new PrimitiveBox<T>(ownerObj, slotName);
            }
        }
    }

//    public void setFromOJB(Object obj, String attr, E value) {
//        persistentLoad(value);
//    }

    // merge the versions kept in this vbox with those stored in vvalues. There might
    // be duplicates.
    // synchronized because we need to ensure that the list of bodies does no change during merge
    synchronized public final void mergeVersions(List<VersionedValue> vvalues) {
        mergeBodiesIntoLoadedVersions(this.body, vvalues, 0);
        this.body = convertVersionedValuesToVBoxBodies(vvalues);
    }

    protected void mergeBodiesIntoLoadedVersions(VBoxBody<E> boxBody, List<VersionedValue> vvalues, int pos) {
        if (boxBody != null && pos < vvalues.size()) {
            if (boxBody.version > vvalues.get(pos).getVersion()) {
                vvalues.add(pos, new VersionedValue(boxBody.value, boxBody.version));
                mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
            } else if (boxBody.version < vvalues.get(pos).getVersion()) {
                mergeBodiesIntoLoadedVersions(boxBody, vvalues, pos + 1);
            } else {
                mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
            }
        } else if (boxBody == null) {
            return;  // we're done
        } else {
            vvalues.add(new VersionedValue(boxBody.value, boxBody.version));
            mergeBodiesIntoLoadedVersions(boxBody.next, vvalues, pos + 1);
        }
    }

    protected VBoxBody<E> convertVersionedValuesToVBoxBodies(List<VersionedValue> vvalues) {
        ListIterator<VersionedValue> iter = vvalues.listIterator(vvalues.size());

        VBoxBody<E> result = null;
        while (iter.hasPrevious()) {
            VersionedValue vvalue = iter.previous();
            result = VBox.makeNewBody((E) vvalue.getValue(), vvalue.getVersion(), result);
        }
        return result;
    }

//     private static void print(List<VersionedValue> vvalues) {
// 	for (VersionedValue v : vvalues) {
// 	    System.out.println("<" + v.getValue() + "," + v.getVersion() + ">");
// 	}
// 	System.out.println("DONE");
//     }

//     private static void print(VBoxBody<String> b) {
// 	while (b != null) {
// 	    System.out.println("<" + b.value + "," + b.version + ">");
// 	    b = b.next;
// 	}
// 	System.out.println("DONE");
//     }
}
