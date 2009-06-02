package pt.ist.fenixframework.pstm;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;

import pt.ist.fenixframework.DomainObject;

public abstract class AbstractDomainObject implements DomainObject,dml.runtime.FenixDomainObject {

    private Integer idInternal;

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

    protected AbstractDomainObject(org.apache.ojb.odmg.OJB dummy) {
        // do nothing
        // this constructor exists only as part of the allocate-instance protocol
    }

    public Integer getIdInternal() {
        return idInternal;
    }
    
    private Integer get$idInternal() {
        return idInternal;
    }
    
    public void setIdInternal(Integer idInternal) {
        if ((this.idInternal != null) && (! this.idInternal.equals(idInternal))) {
            System.out.println("!!!!!! WARNING !!!!!! Changing the idInternal of an object: " + this);
            //throw new Error("Cannot change the idInternal of an object");
        }
        this.idInternal = idInternal;
    }
    
    protected void ensureIdInternal() {
        try {
            PersistenceBroker broker = Transaction.getOJBBroker();
            ClassDescriptor cld = broker.getClassDescriptor(this.getClass());
            
            // find successive ids until one is available
            while (true) {
                Integer id = (Integer)broker.serviceSequenceManager().getUniqueValue(cld.getFieldDescriptorByName("idInternal"));
                this.idInternal = id;
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

    // duplicate method (see get OID()).  This is the name that should stick.
    // the other is to go away
    public long getOid() {
        return Transaction.getOIDFor(this);
    }

    private long get$oid() {
        return getOid();
    }

    public static <T extends DomainObject> T fromOID(long oid) {
        return (T)Transaction.getObjectForOID(oid);
    }

    VersionedSubject getSlotNamed(String attrName) {
	Class myClass = this.getClass();
	while (myClass != Object.class) {
	    try {
		Field f = myClass.getDeclaredField(attrName);
		f.setAccessible(true);
		return (VersionedSubject)f.get(this);
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

    protected int getColumnIndex(final ResultSet resultSet, final String columnName, final Integer[] columnIndexes, final int columnCount)
    		throws SQLException {
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

}
