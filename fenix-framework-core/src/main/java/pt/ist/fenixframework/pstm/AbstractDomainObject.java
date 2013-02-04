package pt.ist.fenixframework.pstm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

public abstract class AbstractDomainObject implements DomainObject, dml.runtime.FenixDomainObject, Serializable {
    // this should be final, but the ensureIdInternal method prevents it
    private long oid;

    static final public Comparator<DomainObject> COMPARATOR_BY_ID = new Comparator<DomainObject>() {
        @Override
        public int compare(DomainObject o1, DomainObject o2) {
            return new Long(o1.getOid()).compareTo(o2.getOid());
        }
    };

    private VBox<DomainMetaObject> domainMetaObject;

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
        // ensureOid();
        Transaction.storeNewObject(this);

        initMetaObject(false);

        if ((!getClass().isAnnotationPresent(NoDomainMetaObjects.class)) && FenixFramework.canCreateDomainMetaObjects()) {
            DomainMetaObject metaObject = new DomainMetaObject();
            metaObject.setDomainObject(this);

            getDomainMetaClass().addExistingDomainMetaObject(getDomainMetaObject());
        }
    }

    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
        // this constructor exists only as part of the allocate-instance
        // protocol
        this.oid = oid.oid;

        initMetaObject(true);
    }

    private void initMetaObject(boolean allocateOnly) {
        domainMetaObject = VBox.makeNew(this, "domainMetaObject", allocateOnly, false);
    }

    @Override
    public final Integer getIdInternal() {
        return (int) (this.oid & 0x7FFFFFFF);
    }

    private Integer get$idInternal() {
        return getIdInternal();
    }

    protected void ensureOid() {
        try {
            // find successive ids until one is available
            while (true) {
                this.oid = DomainClassInfo.getNextOidFor(this.getClass());
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

    protected void ensureIdInternal() {
        try {
            PersistenceBroker broker = Transaction.getOJBBroker();
            Class myClass = this.getClass();
            ClassDescriptor cld = broker.getClassDescriptor(myClass);

            long cid = (long) DomainClassInfo.mapClassToId(myClass) << 32;

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

    @Override
    @Deprecated
    public long getOID() {
        return getOid();
    }

    // duplicate method (see getOID()). This is the name that should stick.
    // the other is to go away
    @Override
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

    public DomainMetaObject getDomainMetaObject() {
        return domainMetaObject.get(this, "domainMetaObject");
    }

    public void justSetMetaObject(DomainMetaObject domainMetaObject) {
        this.domainMetaObject.put(this, "domainMetaObject", domainMetaObject);
    }

    private void setMetaObject(DomainMetaObject domainMetaObject) {
        domainMetaObject.setDomainObject(this);
    }

    private void removeMetaObject() {
        getDomainMetaObject().removeDomainObject();
    }

    /**
     * This should be invoked only when this DO is being deleted.
     */
    private void deleteDomainMetaObject() {
        if (getDomainMetaObject() != null) {
            getDomainMetaObject().delete();
        }
    }

    private Long get$oidDomainMetaObject() {
        pt.ist.fenixframework.pstm.AbstractDomainObject value = getDomainMetaObject();
        return (value == null) ? null : value.getOid();
    }

    public void readFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
        int txNumber = Transaction.current().getNumber();
        readSlotsFromResultSet(rs, txNumber);
        readMetaObjectFromResultSet(rs, txNumber);
    }

    protected void readMetaObjectFromResultSet(java.sql.ResultSet rs, int txNumber) throws SQLException {
        DomainMetaObject metaObject = pt.ist.fenixframework.pstm.ResultSetReader.readDomainObject(rs, "OID_DOMAIN_META_OBJECT");
        this.domainMetaObject.persistentLoad(metaObject, txNumber);
    }

    protected abstract void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException;

    @Override
    public boolean isDeleted() {
        throw new UnsupportedOperationException();
    }

    protected void checkDisconnected() {
    }

    protected void handleAttemptToDeleteConnectedObject(final String roleName) {
        if (FenixFramework.getConfig().isErrorfIfDeletingObjectNotDisconnected()) {
            throw new Error("Trying to delete a DomainObject that is still connected to other objects: " + this + " by role: "
                    + roleName);
        } else {
            System.out.println("!!! WARNING !!!: Deleting a DomainObject that is still connected to other objects: " + this
                    + " by role: " + roleName);
        }
    }

    protected void deleteDomainObject() {
        checkDisconnected();
        deleteDomainMetaObject();
        Transaction.deleteObject(this);
    }

    private DomainMetaClass getDomainMetaClass() {
        return DomainFenixFrameworkRoot.getInstance().getDomainMetaClass(this.getClass());
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
        private final String oid;

        SerializedForm(AbstractDomainObject obj) {
            this.oid = String.valueOf(obj.getOid());
        }

        Object readResolve() throws ObjectStreamException {
            long objOid = Long.parseLong(this.oid);
            return AbstractDomainObject.fromOID(objOid);
        }
    }

    @Override
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

    protected void doCheckDisconnectedAction(java.util.List<String> relationList) {
        for (String relation : relationList) {
            System.out.println("Relation not disconnected" + relation);
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" + getExternalId();
    }
}
