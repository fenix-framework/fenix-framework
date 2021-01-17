package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.lang.reflect.Field;
import java.sql.SQLException;

import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainFenixFrameworkRoot;
import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;
import pt.ist.fenixframework.backend.jvstmojb.repository.ResultSetReader;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.core.DomainObjectAllocator;
import pt.ist.fenixframework.core.SharedIdentityMap;

public abstract class AbstractDomainObject extends AbstractDomainObjectAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDomainObject.class);

    private long oid;

    private VBox<DomainMetaObject> domainMetaObject;

    public class UnableToDetermineIdException extends RuntimeException {
        private static final long serialVersionUID = 3085208774911959073L;

        public UnableToDetermineIdException(Throwable cause) {
            super("Unable to determine id Exception", cause);
        }
    }

    protected AbstractDomainObject() {
        super();
        TransactionSupport.storeNewObject(this);

        initMetaObject(false);

        if (JvstmOJBConfig.canCreateDomainMetaObjects() && !getClass().isAnnotationPresent(NoDomainMetaObjects.class)) {
            DomainMetaObject metaObject = new DomainMetaObject();
            metaObject.setDomainObject(this);

            getDomainMetaClass().addExistingDomainMetaObject(getDomainMetaObject());
        }
    }

    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
        super(oid);
        this.oid = ((Long) oid.oid).longValue();

        initMetaObject(true);
    }

    private void initMetaObject(boolean allocateOnly) {
        domainMetaObject = VBox.makeNew(this, "domainMetaObject", allocateOnly, false);
    }

    @Deprecated
    public Long getOID() {
        return getOid();
    }

    @Override
    protected void ensureOid() {
        try {
            // find successive ids until one is available
            while (true) {
                this.oid = DomainClassInfo.getNextOidFor(this.getClass());
                Object cached = SharedIdentityMap.getCache().cache(this);
                if (cached == this) {
                    // break the loop once we got this instance cached
                    return;
                }
            }
        } catch (SQLException e) {
            throw new UnableToDetermineIdException(e);
        }
    }

    @Override
    public Long getOid() {
        return oid;
    }

    private long get$oid() {
        return oid;
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

        logger.warn("!!! WARNING !!!: addNewVersion couldn't find the appropriate slot");
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

    /**
     * This should be invoked only when this DO is being deleted.
     */
    private void deleteDomainMetaObject() {
        if (getDomainMetaObject() != null) {
            getDomainMetaObject().delete();
        }
    }

    private Long get$oidDomainMetaObject() {
        DomainObject value = getDomainMetaObject();
        return (value == null) ? null : Long.valueOf(value.getExternalId());
    }

    public void readFromResultSet(java.sql.ResultSet rs) throws java.sql.SQLException {
        int txNumber = Transaction.current().getNumber();
        readSlotsFromResultSet(rs, txNumber);
        readMetaObjectFromResultSet(rs, txNumber);
    }

    protected void readMetaObjectFromResultSet(java.sql.ResultSet rs, int txNumber) throws SQLException {
        DomainMetaObject metaObject = ResultSetReader.readDomainObject(rs, "OID_DOMAIN_META_OBJECT");
        this.domainMetaObject.persistentLoad(metaObject, txNumber);
    }

    protected abstract void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException;

    protected void checkDisconnected() {
    }

    protected void handleAttemptToDeleteConnectedObject(String roleName) {
        throw new Error(
                "Trying to delete a DomainObject that is still connected to other objects: " + this + " by role: " + roleName);
    }

    @Override
    protected void deleteDomainObject() {
        invokeDeletionListeners();
        checkDisconnected();
        deleteDomainMetaObject();
        TransactionSupport.deleteObject(this);
    }

    private DomainMetaClass getDomainMetaClass() {
        return DomainFenixFrameworkRoot.getInstance().getDomainMetaClass(this.getClass());
    }

    @Override
    public final String getExternalId() {
        return Long.toString(oid);
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" + getExternalId();
    }
}
