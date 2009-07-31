package pt.ist.fenixframework.pstm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;

import pt.ist.fenixframework.DomainObject;

public abstract class AbstractDomainObject implements DomainObject, dml.runtime.FenixDomainObject, Serializable {
    // this should be final, but the ensureIdInternal method prevents it
    private long oid;

    public class UnableToDetermineIdException extends RuntimeException {
	public UnableToDetermineIdException(Throwable cause) {
	    super("Unable to determine id Exception", cause);
	}
    }

    protected AbstractDomainObject() {
	super();
	// All domain objects become persistent upon their creation.
	// Ensure that this object gets an idInternal
	// jcachopo: This should be changed in the future...
	ensureIdInternal();
	Transaction.storeNewObject(this);
    }

    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
	// this constructor exists only as part of the allocate-instance
	// protocol
	this.oid = oid.oid;
    }

    public final Integer getIdInternal() {
	return (int) (this.oid & 0x7FFFFFFF);
    }

    private Integer get$idInternal() {
	return getIdInternal();
    }

    protected void ensureIdInternal() {
	try {
	    PersistenceBroker broker = Transaction.getOJBBroker();
	    Class myClass = this.getClass();
	    ClassDescriptor cld = broker.getClassDescriptor(myClass);

	    long cid = ((long) DomainClassInfo.mapClassToId(myClass) << 32);

	    // find successive ids until one is available
	    while (true) {
		Integer id = (Integer) broker.serviceSequenceManager().getUniqueValue(cld.getFieldDescriptorByName("idInternal"));
		this.oid = cid + id;
		Object cached = Transaction.getCache().cache(this);
		if (cached == this) {
		    // break the loop once we got this instance cached
		    return;
		}
	    }
	} catch (Exception e) {
	    throw new UnableToDetermineIdException(e);
	}
    }

    @Override
    public final int hashCode() {
	return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
	return super.equals(obj);
    }

    public long getOID() {
	return getOid();
    }

    // duplicate method (see getOID()). This is the name that should stick.
    // the other is to go away
    public long getOid() {
	return oid;
    }

    private long get$oid() {
	return getOid();
    }

    public static <T extends DomainObject> T fromOID(long oid) {
	DomainObject obj = Transaction.getCache().lookup(oid);

	if (obj == null) {
	    obj = DomainObjectAllocator.allocateObject(oid);

	    // cache object and return the canonical object
	    obj = Transaction.getCache().cache(obj);
	}

	return (T) obj;
    }

    // This method should not be used. It is temporary only until we
    // get rid of the DomainReference class in the Fenix web
    // application.
    public static <T extends DomainObject> T fromClassAndID(Class objClass, int id) {
	long cid = ((long) DomainClassInfo.mapClassToId(objClass) << 32);
	return (T) fromOID(cid + id);
    }

    VersionedSubject getSlotNamed(String attrName) {
	Class myClass = this.getClass();
	while (myClass != Object.class) {
	    try {
		Field f = myClass.getDeclaredField(attrName);
		f.setAccessible(true);
		return (VersionedSubject) f.get(this);
	    } catch (NoSuchFieldException nsfe) {
		myClass = myClass.getSuperclass();
	    } catch (IllegalAccessException iae) {
		throw new Error("Couldn't find attribute " + attrName + ": " + iae);
	    } catch (SecurityException se) {
		throw new Error("Couldn't find attribute " + attrName + ": " + se);
	    }
	}

	return null;
    }

    Object getCurrentValueFor(String attrName) {
	return getSlotNamed(attrName).getCurrentValue(this, attrName);
    }

    jvstm.VBoxBody addNewVersion(String attrName, int txNumber) {
	VersionedSubject vs = getSlotNamed(attrName);
	if (vs != null) {
	    return vs.addNewVersion(attrName, txNumber);
	}

	System.out.println("!!! WARNING !!!: addNewVersion couldn't find the appropriate slot");
	return null;
    }

    public final void readFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
	int txNumber = Transaction.current().getNumber();
	readSlotsFromResultSet(rs, txNumber);
    }

    protected abstract void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException;

    public boolean isDeleted() {
	throw new UnsupportedOperationException();
    }

    protected int getColumnIndex(final ResultSet resultSet, final String columnName, final Integer[] columnIndexes,
	    final int columnCount) throws SQLException {
	if (columnIndexes[columnCount] == null) {
	    synchronized (columnIndexes) {
		if (columnIndexes[columnCount] == null) {
		    int columnIndex = Integer.valueOf(resultSet.findColumn(columnName));
		    columnIndexes[columnCount] = columnIndex;
		}
	    }
	}
	return columnIndexes[columnCount].intValue();
    }

    // serialization code
    protected Object writeReplace() throws ObjectStreamException {
	return new SerializedForm(this);
    }

    private static class SerializedForm implements Serializable {
	private static final long serialVersionUID = 1L;

	// use string to allow future expansion of an OID
	private String oid;

	SerializedForm(AbstractDomainObject obj) {
	    this.oid = String.valueOf(obj.getOid());
	}

	Object readResolve() throws ObjectStreamException {
	    long objOid = Long.parseLong(this.oid);
	    return AbstractDomainObject.fromOID(objOid);
	}
    }

    public final String getExternalId() {
	return String.valueOf(getOID());
    }

    public static <T extends DomainObject> T fromExternalId(String extId) {
	if (extId == null) {
	    return null;
	} else {
	    return AbstractDomainObject.<T> fromOID(Long.valueOf(extId));
	}
    }
}
