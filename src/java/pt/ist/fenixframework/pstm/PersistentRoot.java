package pt.ist.fenixframework.pstm;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.DomainObject;

public class PersistentRoot extends AbstractDomainObject {

    static final String SLOT_NAME = "root";
    static final String DB_SLOT_NAME = SLOT_NAME.toUpperCase();

    private VBox<DomainObject> root;

    private PersistentRoot() {
        // This class will never be instantiated through this
        // constructor, because the single instance of this class will
        // always exist on the database

        throw new Error("Instantiating a PersistentRoot does not make sense.");
    }

    public PersistentRoot(DomainObjectAllocator.OID oid) {
        super(oid);

        // use a PrimitiveBox rather than a ReferenceBox.  This,
        // coupled with an OJB mapping that writes the OID of the
        // object is better than using a reference...
        root = VBox.makeNew(true, false);
    }

    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        Long oid = ResultSetReader.readLong(rs, DB_SLOT_NAME);
        this.root.persistentLoad(oid == null ? null : fromOID(oid), txNumber);
    }

    private Object get$root() {
        DomainObject obj = this.root.get(this, SLOT_NAME);
        if (obj != null) {
            return ToSqlConverter.getValueForlong(obj.getOID());
        } else {
            return null;
        }
    }


    public <T extends DomainObject> T getRoot() {
        return (T)root.get(this, SLOT_NAME);
    }

    public void setRoot(DomainObject obj) {
        this.root.put(this, SLOT_NAME, obj);
    }

    public static void initRootIfNeeded(final Config config) {
        if ((config != null) && (config.getRootClass() != null)) {
            Transaction.withTransaction(new TransactionalCommand() {
                    public void doIt() {
                        PersistentRoot persRoot = getInstance();
                        if (persRoot.getRoot() == null) {
                            try {
                                persRoot.setRoot((DomainObject)config.getRootClass().newInstance());
                            } catch (Exception exc) {
                                throw new Error(exc);
                            }
                        }
                    }
                });
        }
    }

    public static PersistentRoot getInstance() {
        return AbstractDomainObject.fromOID(1L);
    }
}
