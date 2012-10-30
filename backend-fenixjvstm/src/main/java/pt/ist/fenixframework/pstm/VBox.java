package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;
import pt.ist.fenixframework.DomainObject;

public class VBox<E> extends jvstm.VBox<E> implements VersionedSubject,dml.runtime.FenixVBox<E> {
    static final Object NOT_LOADED_VALUE = new Object();

    //initialized in the constructor
    private final DomainObject ownerObj;
    private final String slotName;

    public static <T> T notLoadedValue() {
        return (T)NOT_LOADED_VALUE;
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

    public E get(Object obj, String attrName) {
        return Transaction.currentFenixTransaction().getBoxValue(this, obj, attrName);
    }

    public void put(Object obj, String attrName, E newValue) {
        // TODO: eventually remove this 
        if (! (attrName.equals("idInternal") || attrName.equals("ackOptLock"))) {
            // the set of the idInternal or ackOptLock is performed by OJB and should not be logged
            Transaction.storeObject((DomainObject)obj, attrName);
        }
        put(newValue);
    }
    
    public boolean hasValue() {
	return Transaction.currentFenixTransaction().isBoxValueLoaded(this);
    }

    public void putNotLoadedValue() {
	this.put(VBox.<E>notLoadedValue());
    }

    protected void persistentLoad(E value) {
	int txNumber = Transaction.current().getNumber();
        persistentLoad(value, txNumber);
    }

    public void persistentLoad(Object value, int txNumber) {
	// find appropriate body
	VBoxBody<E> body = this.body.getBody(txNumber);
	if (body.value == NOT_LOADED_VALUE) {
	    body.value = (E)value;
	}
    }

    public VBoxBody addNewVersion(String attr, int txNumber) {
	if (body.version < txNumber) {
            return commit(VBox.<E>notLoadedValue(), txNumber);
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

    public Object getCurrentValue(Object obj, String attrName) {
        return this.get(obj, attrName);
    }

    boolean reload(Object obj, String attr) {
	try {
	    doReload(obj, attr);
	    return true;
	} catch (Throwable e) {
	    // what to do?
	    System.err.println("Couldn't reload attribute '" + attr + "': " + e.getMessage());
	    //e.printStackTrace();
	    return false;
	}
    }

    protected void doReload(Object obj, String attr) {
	throw new Error("Can't reload a simple VBox.  Use a PrimitiveBox or a ReferenceBox instead.");
    }

    public static <T> VBox<T> makeNew(DomainObject ownerObj, String slotName, boolean allocateOnly, boolean isReference) {
	if (isReference) {
	    if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
		return new ReferenceBox<T>(ownerObj, slotName, makeNewBody((T)NOT_LOADED_VALUE, 0, null));
	    } else {
		return new ReferenceBox<T>(ownerObj, slotName);
	    }
	} else {
	    if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
		return new PrimitiveBox<T>(ownerObj, slotName, makeNewBody((T)NOT_LOADED_VALUE, 0, null));
	    } else {
		return new PrimitiveBox<T>(ownerObj, slotName);
	    }
	}
    }

    public void setFromOJB(Object obj, String attr, E value) {
        persistentLoad(value);
    }
}
