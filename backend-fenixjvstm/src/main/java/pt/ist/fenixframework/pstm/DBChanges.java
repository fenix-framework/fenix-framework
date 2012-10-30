package pt.ist.fenixframework.pstm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jvstm.CommitException;

import org.apache.ojb.broker.OptimisticLockException;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.core.ValueContainer;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.JdbcType;
import org.apache.ojb.broker.util.JdbcTypesHelper;
import org.apache.ojb.broker.util.ObjectModificationDefaultImpl;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

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
		if (!isNewObject(log.obj)) {
		    modified.add(log.obj);
		}
	    }
	}

	return modified;
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

    public void removeRelationTuple(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2,
	    String colNameOnObj2) {
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

    private void setRelationTuple(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2,
	    String colNameOnObj2, boolean remove) {
	if (mToNTuples == null) {
	    mToNTuples = new HashMap<RelationTupleInfo, RelationTupleInfo>();
	}

	RelationTupleInfo info = new RelationTupleInfo(relation, obj1, colNameOnObj1, obj2, colNameOnObj2, remove);
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
	// store new objects
	if (newObjs != null) {
	    for (Object obj : newObjs) {
		pb.store(obj, ObjectModificationDefaultImpl.INSERT);
	    }
	}

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

	if (foundOptimisticException) {
	    throw new jvstm.CommitException();
	}

	// delete objects
	if (objsToDelete != null) {
	    for (Object obj : objsToDelete) {
		pb.delete(obj);
	    }
	}

	// write m-to-n tuples
	if (mToNTuples != null) {
	    for (RelationTupleInfo info : mToNTuples.values()) {
		updateMtoNRelation(pb, info);
	    }
	}

	// write change logs
	Connection conn = pb.serviceConnectionManager().getConnection();
	writeAttrChangeLogs(conn, txNumber);
    }

    private void writeAttrChangeLogs(Connection conn, int txNumber) throws SQLException {
	int numRecords = (attrChangeLogs == null) ? 0 : attrChangeLogs.size();

	// allocate a large capacity StringBuilder to avoid reallocation
	int bufferCapacity = Math.min(MIN_BUFFER_CAPACITY + (numRecords * PER_RECORD_LENGTH), MAX_BUFFER_CAPACITY);
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

    private static JdbcType OID_JDBC_TYPE = JdbcTypesHelper.getJdbcTypeByName("BIGINT");
    private final static String[] EMPTY_ARRAY = new String[0];

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

	String[] oidColumns = new String[2];
	oidColumns[0] = cod.getFksToThisClass()[0];
	oidColumns[1] = cod.getFksToItemClass()[0];

	ValueContainer[] oidValues = new ValueContainer[2];
	oidValues[0] = new ValueContainer(obj1.getOid(), OID_JDBC_TYPE);
	oidValues[1] = new ValueContainer(obj2.getOid(), OID_JDBC_TYPE);

	String table = cod.getIndirectionTable();

	// always remove the tuple
	String sqlStmt = pb.serviceSqlGenerator().getDeleteMNStatement(table, oidColumns, null);
	pb.serviceJdbcAccess().executeUpdateSQL(sqlStmt, cld1, oidValues, null);

	// if it was not to remove but to add, then add it
	// this "delete-first, add-after" serves to ensure that we can add
	// multiple times
	// the same tuple to a relation and still have the Set semantics for the
	// relation.
	if (!tupleInfo.remove) {
	    sqlStmt = pb.serviceSqlGenerator().getInsertMNStatement(table, oidColumns, EMPTY_ARRAY);
	    pb.serviceJdbcAccess().executeUpdateSQL(sqlStmt, cld1, oidValues, null);
	}
    }

    static class RelationTupleInfo {
	final String relation;
	final DomainObject obj1;
	final String colNameOnObj1;
	final DomainObject obj2;
	final String colNameOnObj2;
	final boolean remove;

	RelationTupleInfo(String relation, DomainObject obj1, String colNameOnObj1, DomainObject obj2, String colNameOnObj2,
		boolean remove) {
	    this.relation = relation;
	    this.obj1 = obj1;
	    this.colNameOnObj1 = colNameOnObj1;
	    this.obj2 = obj2;
	    this.colNameOnObj2 = colNameOnObj2;
	    this.remove = remove;
	}

	@Override
	public int hashCode() {
	    return relation.hashCode() + obj1.hashCode() + obj2.hashCode();
	}

	@Override
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

	@Override
	public int hashCode() {
	    return System.identityHashCode(obj) + attr.hashCode();
	}

	@Override
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
