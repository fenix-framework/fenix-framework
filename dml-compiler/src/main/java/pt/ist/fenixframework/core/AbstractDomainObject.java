package pt.ist.fenixframework.core;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import eu.cloudtm.LocalityHints;

/**
 * This is the top-level class for every DomainObject. Each backend should provide a subclass of
 * this class, with a backend-specific implementation of both {@link DomainObject#getExternalId()} and {@link #getOid()}. In this
 * class, they simply throw an {@link UnsupportedOperationException}. The method {@link DomainObject#getExternalId()} should be
 * used
 * by the user of the framework, whereas the method {@link #getOid()} should be used by code within
 * the framework. This allows for a more efficient implementation of the object's internal
 * identifier, other than the String type imposed on the external identifier.
 * 
 * Additionally, the subclass must also implement {@link #ensureOid()}, {@link #makeSerializedForm()}, and
 * {@link SerializedForm#fromExternalId(String)}. See their
 * documentation for further explanation.
 */
public abstract class AbstractDomainObject implements DomainObject {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDomainObject.class);

    @Override
    public String getExternalId() {
        throw new UnsupportedOperationException("getExternalId not implemented at this level");
    }

    /**
     * Returns an internal global representation of this object. Although, it can be implemented
     * simply as a call to {@link #getExternalId()}, this method enables code **within** the
     * framework to leverage on the knowledge of the concrete identifier, thus being more efficient.
     * <strong>This method should only be used by code internal to the framework</strong>.
     */
    public Comparable getOid() {
        throw new UnsupportedOperationException("getOid not implemented at this level");
    }

    /**
     * Each BackEnd should provide an implementation for this method. No LocalityHints at this level.
     * 
     * @see DomainObject#getLocalityHints()
     */
    @Override
    public LocalityHints getLocalityHints() {
        return null;
    }

    /**
     * Default, no-arg constructor. Calls {@link #ensureOid()} to set the object's identifier.
     * Every {@link DomainObject} constructor (except for the special allocate-instance constructor)
     * should ensure that {@link #ensureOid()} is called once during object creation.
     * 
     * @see #ensureOid
     */
    protected AbstractDomainObject() {
        this((LocalityHints) null);
    }

    /**
     * This constructor exists only as part of the allocate-instance protocol and should never be
     * explicitly invoked by the programmer. Each backend must implement this constructor, and
     * decide how the OID gets restored to the object. Note that the classes modelled in DML are
     * automatically injected with this constructor, which simply delegates the operation to their
     * superclass's constructor.
     * 
     * In this class, this constructor is empty. It is here just as a placeholder for this
     * documentation.
     */
    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
    }

    /**
     * Creates a {@link DomainObject} with {@link LocalityHints}, which may be used by the underlying platform to perform
     * optimizations related to the node(s) in which this object is located. The object's internal identifier will be built
     * using the information obtained from {@link LocalityHints#hints2String()}.
     * This constructor was originally added specifically for the CloudTM platform, but may be used by other platforms as well.
     * 
     * @param hints
     */
    protected AbstractDomainObject(LocalityHints hints) {
        super();
        ensureOid(hints);
    }

    /**
     * Set the identifier (<code>oid</code>) of the object that is being created. This method is
     * invoked by the default constructor. It is only intented to be invoked from within a
     * constructor (with the exception of {@link #AbstractDomainObject(DomainObjectAllocator.OID)},
     * which has its own way of restoring the object's id.
     */
    protected void ensureOid(LocalityHints hints) {
        throw new UnsupportedOperationException("each BackEnd should provide an implementation for this method");
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

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

    // Serialization Code

    /* This method must be in the format:
     *
     * protected Object writeReplace() throws ObjectStreamException;
     *
     * to allow subclasses to have this writeReplace action when serialization occurs.
     */
    protected Object writeReplace() throws ObjectStreamException {
        if (logger.isTraceEnabled()) {
            logger.trace("Serializing " + this.getClass().getName() + ":" + this.getExternalId());
        }
        return makeSerializedForm();
    }

    /**
     * Creates the concrete instance of SerializedForm for this DomainObject. This method is
     * invoked when serialization of the DomainObject occurs. Final users of this framework should
     * never invoke this method explicitly. Backend developers should provide an implementation for
     * this method in their subclasses of AbstractDomainObject.
     * 
     * @return The corresponding SerializedForm
     */
    protected SerializedForm makeSerializedForm() {
        throw new UnsupportedOperationException("makeSerializedForm not implemented at this level");
    }

    protected static abstract class SerializedForm implements Serializable {
        private static final long serialVersionUID = 1L;

        // the external serialized form of any domain object only needs to keep its external ID
        private final String externalId;

        protected SerializedForm(AbstractDomainObject obj) {
            externalId = obj.getExternalId();
        }

        /* This method must be in the format:
         *
         * protected Object readResolve() throws ObjectStreamException;
         *
         * to allow subclasses to have this readResolve action when de-serialization occurs.
         */
        protected Object readResolve() throws ObjectStreamException {
            if (logger.isTraceEnabled()) {
                logger.trace("Deserializing " + this.externalId);
            }
            return fromExternalId(externalId);
        }

        /**
         * Returns a DomainObject given its externalId. This method is invoked when
         * de-serialization of a previously serialized DomainObject occurs.
         * 
         * Final users of this framework should not invoke this method explicitly. Backend
         * developers should provide an implementation for this method in their subclasses of
         * AbstractDomainObject.SerializedForm.
         * 
         * @param externalId The object's external identifier
         * @return The corresponding DomainObject
         */
        protected DomainObject fromExternalId(String externalId) {
            throw new UnsupportedOperationException("fromExternalID not implemented at this level");
        }
    }

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
