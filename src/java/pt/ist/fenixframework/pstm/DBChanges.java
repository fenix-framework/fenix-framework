package pt.ist.fenixframework.pstm;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialClob;

import jvstm.CommitException;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

import org.apache.ojb.broker.OptimisticLockException;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.core.ValueContainer;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.JdbcType;
import org.apache.ojb.broker.util.JdbcTypesHelper;
import org.apache.ojb.broker.util.ObjectModificationDefaultImpl;


class DBChanges {
    private static final String SQL_CHANGE_LOGS_CMD_PREFIX = "INSERT INTO FF$TX_CHANGE_LOGS VALUES ";
    // The following value is the approximate length of each tuple to add after
    // the VALUES
    private static final int PER_RECORD_LENGTH = 60;
    private static final int MIN_BUFFER_CAPACITY = 256;
    private static final int MAX_BUFFER_CAPACITY = 10000;
    private static final int BUFFER_THRESHOLD = 256;

    private Set<AttrChangeLog> attrChangeLogs = null;
    private Set<DomainObject> newObjs = null;
    private Set objsToStore = null;
    private Set objsToDelete = null;
    private Map<RelationTupleInfo, RelationTupleInfo> mToNTuples = null;

    protected Set<AttrChangeLog> getAttrChangeLogs() {
        Set<AttrChangeLog> set = attrChangeLogs;

        if (set == null) {
            set = new HashSet<AttrChangeLog>();
        }

        return Collections.unmodifiableSet(set);
    }

    protected Set<DomainObject> getNewObjects() {
        Set<DomainObject> set = newObjs;

        if (set == null) {
            set = new HashSet<DomainObject>();
        }

        return Collections.unmodifiableSet(set);
    }

    public Set<DomainObject> getModifiedObjects() {
	Set<DomainObject> modified = new HashSet<DomainObject>();

	if (attrChangeLogs != null) {
	    for (AttrChangeLog log : attrChangeLogs) {
                if (! isNewObject(log.obj)) {
                    modified.add(log.obj);
                }
	    }
	}

	return modified;
    }

    protected Set<RelationTupleInfo> getMtoNRelationTupleInfos() {
        Set<RelationTupleInfo> set;

        if (mToNTuples == null) {
            set = new HashSet<RelationTupleInfo>();
        } else {
            set = mToNTuples.keySet();
        }

        return Collections.unmodifiableSet(set);
    }

    public boolean isDeleted(Object obj) {
	return (objsToDelete != null) && objsToDelete.contains(obj);
    }

    public boolean needsWrite() {
	return ((newObjs != null) && (!newObjs.isEmpty())) || ((objsToStore != null) && (!objsToStore.isEmpty()))
		|| ((objsToDelete != null) && (!objsToDelete.isEmpty())) || ((mToNTuples != null) && (!mToNTuples.isEmpty()));
    }

    public boolean isNewObject(DomainObject obj) {
        return (newObjs != null) && newObjs.contains(obj);
    }

    public void logAttrChange(DomainObject obj, String attrName) {
	if (attrChangeLogs == null) {
	    attrChangeLogs = new HashSet<AttrChangeLog>();
	}
	attrChangeLogs.add(new AttrChangeLog(obj, attrName));
    }

    public void storeNewObject(DomainObject obj) {
	if (newObjs == null) {
	    newObjs = new HashSet<DomainObject>();
	}
	newObjs.add(obj);
	removeFromDeleted(obj);
    }

    public void storeObject(DomainObject obj, String attrName) {
	logAttrChange(obj, attrName);

        if (isNewObject(obj)) {
            // don't need to update new objects
            return;
        }

	if (objsToStore == null) {
	    objsToStore = new HashSet();
	}
	objsToStore.add(obj);
	removeFromDeleted(obj);
    }

    public void deleteObject(Object obj) {
	if (objsToDelete == null) {
	    objsToDelete = new HashSet();
	}
	objsToDelete.add(obj);
	if (newObjs != null) {
	    newObjs.remove(obj);
	}
	if (objsToStore != null) {
	    objsToStore.remove(obj);
	}
    }

    public void addRelationTuple(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2, String colNameOnObj2) {
	setRelationTuple(relation, obj1, colNameOnObj1, obj2, colNameOnObj2, false);
    }

    public void removeRelationTuple(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2, String colNameOnObj2) {
	setRelationTuple(relation, obj1, colNameOnObj1, obj2, colNameOnObj2, true);
    }

    private void removeFromDeleted(DomainObject obj) {
	if (objsToDelete != null) {
	    if (objsToDelete.remove(obj)) {
		if (FenixFramework.getConfig().isErrorIfChangingDeletedObject()) {
		    throw new Error("Changing object after it was deleted: " + obj);
		} else {
		    System.err.println("WARNING: Changing object after it was deleted: " + obj);
		}
	    }
	}
    }

    private void setRelationTuple(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2, String colNameOnObj2,
	    boolean remove) {
	if (mToNTuples == null) {
	    mToNTuples = new HashMap<RelationTupleInfo, RelationTupleInfo>();
	}

	RelationTupleInfo info = new RelationTupleInfo(relation, obj1, colNameOnObj1, obj2, colNameOnObj2, remove);
	RelationTupleInfo previous = mToNTuples.get(info);

	mToNTuples.put(info, info);
    }

    void cache() {
	FenixCache cache = Transaction.getCache();

	if (newObjs != null) {
	    for (DomainObject obj : newObjs) {
		cache.cache(obj);
	    }
	}
    }

    void makePersistent(PersistenceBroker pb, int txNumber) throws SQLException, LookupException {

	long time1 = System.currentTimeMillis();
	long time2 = 0;
	long time3 = 0;
	long time4 = 0;
	long time5 = 0;
	long time6 = 0;
	long time7 = 0;
	long time8 = 0;
	long time9 = 0;
	long time10 = 0;
	long time11 = 0;

	// store new objects
	if (newObjs != null) {
	    for (Object obj : newObjs) {
		pb.store(obj, ObjectModificationDefaultImpl.INSERT);
	    }
	}
	time2 = System.currentTimeMillis();

	boolean foundOptimisticException = false;

	// update objects
	if (objsToStore != null) {
	    for (Object obj : objsToStore) {
		try {
		    pb.store(obj, ObjectModificationDefaultImpl.UPDATE);
		} catch (OptimisticLockException ole) {
		    pb.removeFromCache(obj);
		    foundOptimisticException = true;
		}
	    }
	}

	time3 = System.currentTimeMillis();

	if (foundOptimisticException) {
	    throw new jvstm.CommitException();
	}

	// delete objects
	if (objsToDelete != null) {
	    for (Object obj : objsToDelete) {
		pb.delete(obj);
	    }
	}

	time4 = System.currentTimeMillis();

	// write m-to-n tuples
	if (mToNTuples != null) {
	    for (RelationTupleInfo info : mToNTuples.values()) {
		updateMtoNRelation(pb, info);
	    }
	}

	time5 = System.currentTimeMillis();

	// write change logs
	Connection conn = pb.serviceConnectionManager().getConnection();
	time6 = System.currentTimeMillis();
	writeAttrChangeLogs(conn, txNumber);
	time7 = System.currentTimeMillis();

	// write ServiceInfo
	ServiceInfo info = ServiceInfo.getCurrentServiceInfo();
	time8 = System.currentTimeMillis();
	if ((info != null) && info.shouldLog()) {
	    PreparedStatement stmt = null;
	    try {
		stmt = conn.prepareStatement("INSERT INTO FF$SERVICE_LOG VALUES (?,?,?,?)");
		time9 = System.currentTimeMillis();
		stmt.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
		stmt.setString(2, info.username);
		stmt.setString(3, info.serviceName);
		Clob clob = new SerialClob(info.getArgumentsAsString().toCharArray());
		stmt.setClob(4, clob);
		time10 = System.currentTimeMillis();
		stmt.executeUpdate();
		time11 = System.currentTimeMillis();
	    } finally {
		stmt.close();
	    }
	}

	if ((time8 - time1) > 500) {
	    System.out.println("makePersistent: ,1: " + (time1 == 0 || time2 == 0 ? "" : (time2 - time1)) + "   ,2: "
		    + (time2 == 0 || time3 == 0 ? "" : (time3 - time2)) + "   ,3: "
		    + (time3 == 0 || time4 == 0 ? "" : (time4 - time3)) + "   ,4: "
		    + (time4 == 0 || time5 == 0 ? "" : (time5 - time4)) + "   ,5: "
		    + (time5 == 0 || time6 == 0 ? "" : (time6 - time5)) + "   ,6: "
		    + (time6 == 0 || time7 == 0 ? "" : (time7 - time6)) + "   ,7: "
		    + (time7 == 0 || time8 == 0 ? "" : (time8 - time7)) + "   ,8: "
		    + (time8 == 0 || time9 == 0 ? "" : (time9 - time8)) + "   ,9: "
		    + (time8 == 0 || time9 == 0 ? "" : (time10 - time9)) + "   ,10: "
		    + (time8 == 0 || time9 == 0 ? "" : (time11 - time10)));
	}
    }

    private void writeAttrChangeLogs(Connection conn, int txNumber) throws SQLException {
        int numRecords = (attrChangeLogs == null) ? 0 : attrChangeLogs.size();

        // allocate a large capacity StringBuilder to avoid reallocation
        int bufferCapacity = Math.min(MIN_BUFFER_CAPACITY + (numRecords * PER_RECORD_LENGTH),
                                      MAX_BUFFER_CAPACITY);
        StringBuilder sqlCmd = new StringBuilder(bufferCapacity);
        sqlCmd.append(SQL_CHANGE_LOGS_CMD_PREFIX);

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            boolean addedRecord = false;

            if (attrChangeLogs == null) {
                // if no AttrChangeLog exists, then it means that we
                // only created objects, without changing any other
                // object

                // Still, we need to notify other servers of the tx
                // number, so create an empty changelog line...
                sqlCmd.append("(0,'',");
                sqlCmd.append(txNumber);
                sqlCmd.append(")");
                addedRecord = true;
            } else {
		for (AttrChangeLog log : attrChangeLogs) {
                    if (isNewObject(log.obj)) {
                        // don't need to warn others of changes to new objects
                        continue;
                    }

		    if (addedRecord) {
			sqlCmd.append(",");
		    }
		    sqlCmd.append("(");
		    sqlCmd.append(log.obj.getOID());
		    sqlCmd.append(",'");
		    sqlCmd.append(log.attr);
		    sqlCmd.append("',");
		    sqlCmd.append(txNumber);
		    sqlCmd.append(")");
		    addedRecord = true;

		    if ((bufferCapacity - sqlCmd.length()) < BUFFER_THRESHOLD) {
			stmt.execute(sqlCmd.toString());
			sqlCmd.setLength(0);
			sqlCmd.append(SQL_CHANGE_LOGS_CMD_PREFIX);
			addedRecord = false;
		    }
		}
            }

            if (addedRecord) {
                try {
                    stmt.execute(sqlCmd.toString());
                } catch (SQLException ex) {
                    System.out.println("SqlException: " + ex.getMessage());
                    System.out.println("Deadlock trying to insert: " + sqlCmd.toString());
                    throw new CommitException();
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private static JdbcType KEY_JDBC_TYPE = JdbcTypesHelper.getJdbcTypeByName("INTEGER");
    private static JdbcType OID_JDBC_TYPE = JdbcTypesHelper.getJdbcTypeByName("BIGINT");

    // copied and adapted from OJB's MtoNBroker
    protected void updateMtoNRelation(PersistenceBroker pb, RelationTupleInfo tupleInfo) {
	DomainObject obj1 = tupleInfo.obj1;
	DomainObject obj2 = tupleInfo.obj2;

	ClassDescriptor cld1 = pb.getDescriptorRepository().getDescriptorFor(obj1.getClass());
	CollectionDescriptor cod = cld1.getCollectionDescriptorByName(tupleInfo.colNameOnObj1);
	if (cod == null) {
	    // try the mapping on the other object
	    cld1 = pb.getDescriptorRepository().getDescriptorFor(obj2.getClass());
	    cod = cld1.getCollectionDescriptorByName(tupleInfo.colNameOnObj2);

	    // switch objects
	    obj1 = tupleInfo.obj2;
	    obj2 = tupleInfo.obj1;
	}

	String[] keyColumns = new String[2];
	keyColumns[0] = cod.getFksToThisClass()[0];
	keyColumns[1] = cod.getFksToItemClass()[0];

	ValueContainer[] keyValues = new ValueContainer[2];
        keyValues[0] = new ValueContainer(obj1.getIdInternal(), KEY_JDBC_TYPE);
        keyValues[1] = new ValueContainer(obj2.getIdInternal(), KEY_JDBC_TYPE);

	String[] oidColumns = new String[2];
	oidColumns[0] = keyColumns[0].replace("KEY_", "OID_");
	oidColumns[1] = keyColumns[1].replace("KEY_", "OID_");

	ValueContainer[] oidValues = new ValueContainer[2];
        oidValues[0] = new ValueContainer(obj1.getOid(), OID_JDBC_TYPE);
        oidValues[1] = new ValueContainer(obj2.getOid(), OID_JDBC_TYPE);

	String table = cod.getIndirectionTable();

	// always remove the tuple
	String sqlStmt = pb.serviceSqlGenerator().getDeleteMNStatement(table, keyColumns, null);
	pb.serviceJdbcAccess().executeUpdateSQL(sqlStmt, cld1, keyValues, null);

	// if it was not to remove but to add, then add it
	// this "delete-first, add-after" serves to ensure that we can add
	// multiple times
	// the same tuple to a relation and still have the Set semantics for the
	// relation.
	if (!tupleInfo.remove) {
	    sqlStmt = pb.serviceSqlGenerator().getInsertMNStatement(table, keyColumns, oidColumns);
	    pb.serviceJdbcAccess().executeUpdateSQL(sqlStmt, cld1, keyValues, oidValues);
	}
    }

    static class RelationTupleInfo {
	final String relation;
	final DomainObject obj1;
	final String colNameOnObj1;
	final DomainObject obj2;
	final String colNameOnObj2;
	final boolean remove;

	RelationTupleInfo(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2, String colNameOnObj2, boolean remove) {
	    this.relation = relation;
	    this.obj1 = obj1;
	    this.colNameOnObj1 = colNameOnObj1;
	    this.obj2 = obj2;
	    this.colNameOnObj2 = colNameOnObj2;
	    this.remove = remove;
	}

	public int hashCode() {
	    return relation.hashCode() + obj1.hashCode() + obj2.hashCode();
	}

	public boolean equals(Object obj) {
	    if ((obj != null) && (obj.getClass() == this.getClass())) {
		RelationTupleInfo other = (RelationTupleInfo) obj;
		return this.relation.equals(other.relation) && this.obj1.equals(other.obj1) && this.obj2.equals(other.obj2);
	    } else {
		return false;
	    }
	}
    }

    static class AttrChangeLog {
	final DomainObject obj;
	final String attr;

	AttrChangeLog(DomainObject obj, String attr) {
	    this.obj = obj;
	    this.attr = attr;
	}

	public int hashCode() {
	    return System.identityHashCode(obj) + attr.hashCode();
	}

	public boolean equals(Object obj) {
	    if ((obj != null) && (obj.getClass() == this.getClass())) {
		AttrChangeLog other = (AttrChangeLog) obj;
		return (this.obj == other.obj) && this.attr.equals(other.attr);
	    } else {
		return false;
	    }
	}
    }
}
