package pt.ist.fenixframework.pstm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;

import dml.DomainClass;
import dml.DomainModel;

import pt.ist.fenixframework.pstm.repository.DbUtil;

public class DomainClassInfo implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DomainClassInfo.class);
    private volatile static Map<Class, DomainClassInfo> classInfoMap;
    private volatile static DomainClassInfo[] classInfoById;
    private volatile static long serverOidBase;

    static void initializeClassInfos(int serverId) {
        serverOidBase = (long)serverId << 48;  // the server id provides de 16 most significant bits of the OID

	PersistenceBroker broker = null;
	ResultSet rs = null;
	Statement stmt = null;

	try {
	    broker = PersistenceBrokerFactory.defaultPersistenceBroker();

	    // repeat until success
	    while (true) {
		broker.beginTransaction();

		Connection conn = broker.serviceConnectionManager().getConnection();
		stmt = conn.createStatement();

		rs = stmt.executeQuery("SELECT DOMAIN_CLASS_NAME,DOMAIN_CLASS_ID FROM FF$DOMAIN_CLASS_INFO");

		Map<Class, DomainClassInfo> map = new IdentityHashMap<Class, DomainClassInfo>();
		ArrayList<DomainClassInfo> array = new ArrayList<DomainClassInfo>();

		int maxId = 0;

		// read all infos
		while (rs.next()) {
		    String classname = rs.getString(1);
		    int cid = rs.getInt(2);

		    DomainClassInfo classInfo = new DomainClassInfo(classname, cid);

		    maxId = Math.max(maxId, cid);
		    addNewInfo(map, array, classInfo);
		}

		// create any missing records
		try {
		    DomainModel model = MetadataManager.getDomainModel();

		    for (DomainClass domClass : model.getDomainClasses()) {
			Class javaClass = Class.forName(domClass.getFullName());
			if (javaClass != PersistentRoot.class && !map.containsKey(javaClass)) {
			    DomainClassInfo classInfo = new DomainClassInfo(javaClass, ++maxId);
			    addNewInfo(map, array, classInfo);

			    if (logger.isInfoEnabled()) {
				logger.info("Registering new domain class '" + javaClass.getName() + "' with id "
					+ classInfo.classId);
			    }
			    stmt.executeUpdate("INSERT INTO FF$DOMAIN_CLASS_INFO VALUES ('" + javaClass.getName() + "', "
				    + classInfo.classId + ")");
			}
		    }

		    // try to commit
		    broker.commitTransaction();

		    // the commit was ok, so finish the initialization by
		    // assigning to the static variables
		    classInfoMap = Collections.unmodifiableMap(map);
		    classInfoById = new DomainClassInfo[maxId + 1];
		    array.toArray(classInfoById);
		    return;
		} catch (SQLException e) {
		    logger.info("The registration of new DomainClassInfos failed.  Retrying...");
		    // the inserts into the database or the commit may fail if a
		    // concurrent execution tries to create new records also
		    // if that happens, abort the current transaction and retry
		    // with a new one
		    broker.abortTransaction();
		}
	    }
	} catch (Exception e) {
	    // if an exception occurs, throw an error
	    throw new Error(e);
	} finally {
	    if (broker != null) {
		if (broker.isInTransaction()) {
		    broker.abortTransaction();
		}
		broker.close();
	    }
	    if (rs != null) {
		try {
		    rs.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	    if (stmt != null) {
		try {
		    stmt.close();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    private static Class findClass(String classname) {
	try {
	    return Class.forName(classname);
	} catch (ClassNotFoundException cnfe) {
	    // domain classes may disappear, but their id should not be reused
	    // so, if the corresponding Java class does not exist, return null
	    return null;
	}
    }

    private static void addNewInfo(Map<Class, DomainClassInfo> map, ArrayList<DomainClassInfo> array, DomainClassInfo info) {
	if (info.domainClass != null) {
	    map.put(info.domainClass, info);
	}

	int index = info.classId;
	int size = array.size();
	if (size <= index) {
	    array.ensureCapacity(index + 1);
	    while (size < index) {
		array.add(null);
		size++;
	    }
	    array.add(info);
	} else {
	    array.set(info.classId, info);
	}
    }

    public static int mapClassToId(Class objClass) {
	if (objClass == PersistentRoot.class) {
	    return 0;
	} else {
	    DomainClassInfo domainClassInfo = classInfoMap.get(objClass);
	    if (domainClassInfo == null) {
		throw new RuntimeException("Domain class not registered: " + objClass.getCanonicalName());
	    }
	    return domainClassInfo.classId;
	}
    }

    private static Class mapIdToClass(int cid) {
	if (cid == 0) {
	    return PersistentRoot.class;
	} else if ((cid < 1) || (cid >= classInfoById.length)) {
	    return null;
	} else {
	    return classInfoById[cid].domainClass;
	}
    }

    private static int mapOidToClassId(long oid) {
	if (oid == 1) {
	    return 0;
	} else {
	    return (((int) (oid >> 32)) & 0x0000FFFF); // shift class id to
						       // rightmost position and
						       // clear server id bits
	}
    }

    public static Class mapOidToClass(long oid) {
	return mapIdToClass(mapOidToClassId(oid));
    }

    public static long getNextOidFor(Class objClass) throws Exception {
        int nextKey;
        DomainClassInfo info = classInfoMap.get(objClass);

        synchronized(info) {
            int lastKey = info.getLastKey();
            if (lastKey == UNKNOWN_KEY) {  // not yet initialized from the DB
                lastKey = initLastKeyFor(info);
            }

            nextKey = lastKey + 1;
            info.setLastKey(nextKey);
        }
        
//         System.out.print("Server(" + (int)(serverOidBase >> 48) + ")");
//         System.out.print(", Class(" + info.classId + ")");
//         System.out.print(", Object(" + nextKey + ")");
//         System.out.println(": " + serverOidBase + " + " + ((long)info.classId << 32) + " + "
//                            + nextKey + " = " + (serverOidBase + ((long)info.classId << 32) + nextKey));

        // build and return OID
	if ((PersistentRoot.class == objClass) && (nextKey == 1)) {
	    // this first PersistentRoot instance is special and always takes a known value
	    return 1L;
	} else {
	    return serverOidBase + ((long)info.classId << 32) + nextKey;
	}
    }

    /* Invocations to this method should be synchronized in the <code>info</code> argument */
    private static int initLastKeyFor(DomainClassInfo info) throws Exception {
	long baseRange = serverOidBase + ((long)info.classId << 32);
  	long maxId = getMaxIdForClass(info.domainClass.getName(), baseRange, baseRange + 0xFFFFFFFFL);
	return (int)maxId; // the lower 32 bit are the object's relative id.
    }

    public static long getMaxIdForClass(String className, long lowestId, long highestId) throws Exception {
        PersistenceBroker broker = null;
        Statement stmt = null;
        
        try {
            broker = PersistenceBrokerFactory.defaultPersistenceBroker();
            broker.beginTransaction();
            
            Connection conn = broker.serviceConnectionManager().getConnection();
            stmt = conn.createStatement();

            StringBuilder sqlStmtText = new StringBuilder();
            sqlStmtText.append("SELECT MAX(OID) FROM ");
	    DomainClass domainClass = MetadataManager.getDomainModel().findClass(className);
	    sqlStmtText.append(OJBMetadataGenerator.getExpectedTableName(domainClass));
	    sqlStmtText.append(" WHERE OID > ");
	    sqlStmtText.append(lowestId);
            sqlStmtText.append(" AND OID <= ");
	    sqlStmtText.append(highestId);
            sqlStmtText.append(";");

            ResultSet rs = stmt.executeQuery(sqlStmtText.toString());
            broker.commitTransaction();

	    rs.first();
	    return rs.getLong(1); // getLong() will return 0 in case there is no line matching the query
        } finally {
            if (broker != null) {
                if (broker.isInTransaction()) {
                    broker.abortTransaction();
                }
                broker.close();
            }
            if (stmt != null) {
        	try {
		    stmt.close();
		} catch (SQLException e) {
		    // nothing can be done now.
		}
            }
        }
    }

    private static final int UNKNOWN_KEY = 0;

    // the non-static part starts here

    public final String domainClassName;
    public final transient Class domainClass;
    public final int classId;
    /** The maximum object key used for objects of this class in this server */
    private transient int lastKey = UNKNOWN_KEY;

    public DomainClassInfo(Class domainClass, int classId) {
	this(domainClass.getName(), domainClass, classId);
    }

    public DomainClassInfo(String domainClassName, int classId) {
	this(domainClassName, findClass(domainClassName), classId);
    }

    public DomainClassInfo(String domainClassName, Class domainClass, int classId) {
	this.domainClassName = domainClassName;
	this.domainClass = domainClass;
	this.classId = classId;
    }

    protected int getLastKey() {
        return this.lastKey;
    }

    protected void setLastKey(int lastKey) {
        this.lastKey = lastKey;
    }

    // serialization code
    protected Object writeReplace() throws ObjectStreamException {
	return new SerializedForm(this);
    }

    private static class SerializedForm implements Serializable {
  	private static final long serialVersionUID = 1L;
	
	private String className;
	private int classId;

	SerializedForm(DomainClassInfo obj) {
	    this.className = obj.domainClassName;
	    this.classId = obj.classId;
	}

	Object readResolve() throws ObjectStreamException, ClassNotFoundException {
	    return new DomainClassInfo(this.className, this.classId);
	}
    }
}
