package pt.ist.fenixframework.core;

import pt.ist.fenixframework.DomainObject;

/**
 * This is the top-level class for every DomainObject. Each backend should
 * provide a subclass of this class, with a backend-specific implementation of
 * both {@link DomainObject#getExternalId()} and {@link getOid()}.  In this
 * class, they simply throw an {@link UnsupportedOperationException}.  The
 * method {@link DomainObject#getExternalId()} should be used by the user of the
 * framework, whereas the method {@link getOid()} should be used by code within
 * the framework.  This allows for a more efficient implementation of the
 * object's internal identifier, other than the String type imposed on the
 * external identifier.
 */
public abstract class AbstractDomainObject implements DomainObject {
    public Object getOid() {
        throw new UnsupportedOperationException("Must be redefined in concrete subclasses.");
    }

    public String getExternalId() {
        throw new UnsupportedOperationException("Must be redefined in concrete subclasses.");
    }

    /**
     * Default, no-arg constructor.
     *
     * @see #ensureOid
     */
    protected AbstractDomainObject() {
        super();
        ensureOid();
    }
    
    /**
     * Set the identifier (<code>oid</code>) of the object that is being created.  This method is
     * already invoked by the no-arg constructor.  It is only intented to be invoked from within a
     * constructor (with the exception of {@link AbstractDomainObject(DomainObjectAllocator.OID)},
     * which uses {@link restoreOid(Object)} instead).
     */
    protected abstract void ensureOid();

    /**
     * This constructor exists only as part of the allocate-instance protocol and should never be
     * explicitly invoked by the programmer.  Each subclass must define the {@link
     * restoreOid(Object)} method.
     *
     * @see #restoreOid
     */
    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
        restoreOid(oid.oid);
    }

    /** Overrides of this method should set the <code>oid</code> of the object.  This method should
     * only be invoked as part of the allocate-instance protocol, i.e. from within the {@link
     * AbstractDomainObject(DomainObjectAllocator.OID)} constructor.
     *
     * @param oid This object's identifier
     */
    protected abstract void restoreOid(Object oid);

    @Override
    public final int hashCode() {
	return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
	return super.equals(obj);
    }

    // public static <T extends DomainObject> T fromOid(long oid) {
    //     return null;
    // }

    // public static <T extends DomainObject> T fromOID(long oid) {
    //     DomainObject obj = Transaction.getCache().lookup(oid);

    //     if (obj == null) {
    //         obj = DomainObjectAllocator.allocateObject(oid);

    //         // cache object and return the canonical object
    //         obj = Transaction.getCache().cache(obj);
    //     }

    //     return (T) obj;
    // }

    // VersionedSubject getSlotNamed(String attrName) {
    //     Class myClass = this.getClass();
    //     while (myClass != Object.class) {
    //         try {
    //     	Field f = myClass.getDeclaredField(attrName);
    //     	f.setAccessible(true);
    //     	return (VersionedSubject) f.get(this);
    //         } catch (NoSuchFieldException nsfe) {
    //     	myClass = myClass.getSuperclass();
    //         } catch (IllegalAccessException iae) {
    //     	throw new Error("Couldn't find attribute " + attrName + ": " + iae);
    //         } catch (SecurityException se) {
    //     	throw new Error("Couldn't find attribute " + attrName + ": " + se);
    //         }
    //     }

    //     return null;
    // }

    // Object getCurrentValueFor(String attrName) {
    //     return getSlotNamed(attrName).getCurrentValue(this, attrName);
    // }

    // jvstm.VBoxBody addNewVersion(String attrName, int txNumber) {
    //     VersionedSubject vs = getSlotNamed(attrName);
    //     if (vs != null) {
    //         return vs.addNewVersion(attrName, txNumber);
    //     }

    //     System.out.println("!!! WARNING !!!: addNewVersion couldn't find the appropriate slot");
    //     return null;
    // }

    // public void readFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
    //     int txNumber = Transaction.current().getNumber();
    //     readSlotsFromResultSet(rs, txNumber);
    // }

    // protected abstract void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException;

    // public boolean isDeleted() {
    //     throw new UnsupportedOperationException();
    // }

    // protected void checkDisconnected() {
    // }

    // protected void handleAttemptToDeleteConnectedObject() {
    //     if (FenixFramework.getConfig().isErrorfIfDeletingObjectNotDisconnected()) {
    //         throw new Error("Trying to delete a DomainObject that is still connected to other objects: " + this);
    //     } else {
    //         System.out.println("!!! WARNING !!!: Deleting a DomainObject that is still connected to other objects: " + this);
    //     }	
    // }

    // protected void deleteDomainObject() {
    //     checkDisconnected();
    //     Transaction.deleteObject(this);
    // }

    // protected int getColumnIndex(final ResultSet resultSet, final String columnName, final Integer[] columnIndexes,
    //         final int columnCount) throws SQLException {
    //     if (columnIndexes[columnCount] == null) {
    //         synchronized (columnIndexes) {
    //     	if (columnIndexes[columnCount] == null) {
    //     	    int columnIndex = Integer.valueOf(resultSet.findColumn(columnName));
    //     	    columnIndexes[columnCount] = columnIndex;
    //     	}
    //         }
    //     }
    //     return columnIndexes[columnCount].intValue();
    // }

    // // serialization code
    // protected Object writeReplace() throws ObjectStreamException {
    //     return new SerializedForm(this);
    // }

    // private static class SerializedForm implements Serializable {
    //     private static final long serialVersionUID = 1L;

    //     // use string to allow future expansion of an OID
    //     private final String oid;

    //     SerializedForm(AbstractDomainObject obj) {
    //         this.oid = String.valueOf(obj.getOid());
    //     }

    //     Object readResolve() throws ObjectStreamException {
    //         long objOid = Long.parseLong(this.oid);
    //         return AbstractDomainObject.fromOID(objOid);
    //     }
    // }

    // public static <T extends DomainObject> T fromExternalId(String extId) {
    //     if (extId == null) {
    //         return null;
    //     } else {
    //         return AbstractDomainObject.<T> fromOID(Long.valueOf(extId));
    //     }
    // }

    // protected void doCheckDisconnectedAction(java.util.List<String> relationList) {
    //     for(String relation : relationList) {
    //         System.out.println("Relation not disconnected" + relation);
    //     }
    // }

    @Override
    public String toString() {
	return getClass().getName() + ":" + getExternalId();
    }
}
