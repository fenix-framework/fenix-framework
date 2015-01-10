package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.ojb.OJBMetadataGenerator;
import pt.ist.fenixframework.backend.jvstmojb.repository.ServerId;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;

public class DomainClassInfo {

    private static final Logger logger = LoggerFactory.getLogger(DomainClassInfo.class);
    private volatile static Map<Class, DomainClassInfo> classInfoMap;
    private volatile static DomainClassInfo[] classInfoById;

    public static void initializeClassInfos() {

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

                rs =
                        stmt.executeQuery("SELECT DOMAIN_CLASS_NAME,DOMAIN_CLASS_ID FROM FF$DOMAIN_CLASS_INFO ORDER BY DOMAIN_CLASS_ID");

                Map<Class, DomainClassInfo> map = new HashMap<Class, DomainClassInfo>();
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
                    DomainModel model = FenixFramework.getDomainModel();

                    for (DomainClass domClass : model.getDomainClasses()) {
                        boolean isDomainRoot = domClass.getFullName().equals(DomainRoot.class.getName());

                        Class javaClass = Class.forName(domClass.getFullName());
                        if (!map.containsKey(javaClass)) {
                            DomainClassInfo classInfo = new DomainClassInfo(javaClass, isDomainRoot ? 0 : ++maxId);
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
                    logger.error("The registration of new DomainClassInfos failed.  Retrying...");
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
            Class<?> type = Class.forName(classname);
            if (!OneBoxDomainObject.class.isAssignableFrom(type)) {
                logger.error("Type '{}' is no longer a domain object!", classname);
                return null;
            }
            return type;
        } catch (ClassNotFoundException cnfe) {
            // domain classes may disappear, but their id should not be reused
            // so, if the corresponding Java class does not exist, return null
            return null;
        }
    }

    private static void addNewInfo(Map<Class, DomainClassInfo> map, ArrayList<DomainClassInfo> array, DomainClassInfo info) {
        if (info.domainClass != null && !map.containsKey(info.domainClass)) {
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

    private static Class mapIdToClass(int cid) {
        if (cid < 0 || cid >= classInfoById.length) {
            return null;
        } else {
            return classInfoById[cid].domainClass;
        }
    }

    private static int mapOidToClassId(long oid) {
        if (oid == 1) {
            return 0;
        } else {
            return (int) (oid >> 32) & 0x0000FFFF; // shift class id to
            // rightmost position and
            // clear server id bits
        }
    }

    public static Class mapOidToClass(long oid) {
        return mapIdToClass(mapOidToClassId(oid));
    }

    public static long getNextOidFor(Class<?> objClass) throws SQLException {
        int nextKey;
        DomainClassInfo info = classInfoMap.get(objClass);

        synchronized (info) {
            int lastKey = info.getLastKey();
            if (lastKey == UNKNOWN_KEY) { // not yet initialized from the DB
                lastKey = initLastKeyFor(info);
            }

            nextKey = lastKey + 1;
            info.setLastKey(nextKey);
        }

        return ServerId.getServerOidBase() + ((long) info.classId << 32) + nextKey;
    }

    /* Invocations to this method should be synchronized in the <code>info</code> argument */
    private static int initLastKeyFor(DomainClassInfo info) throws SQLException {
        long baseRange = ServerId.getServerOidBase() + ((long) info.classId << 32);
        long maxId = getMaxIdForClass(info.domainClass.getName(), baseRange, baseRange + 0xFFFFFFFFL);
        return (int) maxId; // the lower 32 bit are the object's relative id.
    }

    private static long getMaxIdForClass(String className, long lowestId, long highestId) throws SQLException {
        Connection conn = TransactionSupport.getCurrentSQLConnection();
        try (Statement stmt = conn.createStatement()) {

            StringBuilder sqlStmtText = new StringBuilder();
            sqlStmtText.append("SELECT MAX(OID) FROM `");
            DomainClass domainClass = FenixFramework.getDomainModel().findClass(className);
            sqlStmtText.append(OJBMetadataGenerator.getExpectedTableName(domainClass));
            sqlStmtText.append("` WHERE OID > ");
            sqlStmtText.append(lowestId);
            sqlStmtText.append(" AND OID <= ");
            sqlStmtText.append(highestId);
            sqlStmtText.append(";");

            ResultSet rs = stmt.executeQuery(sqlStmtText.toString());

            rs.first();
            return rs.getLong(1); // getLong() will return 0 in case there is no line matching the query
        }
    }

    private static final int UNKNOWN_KEY = -1;

    // the non-static part starts here

    private final Class<?> domainClass;
    private final int classId;
    /** The maximum object key used for objects of this class in this server */
    private int lastKey = UNKNOWN_KEY;

    private DomainClassInfo(Class<?> domainClass, int classId) {
        this.domainClass = domainClass;
        this.classId = classId;
    }

    private DomainClassInfo(String domainClassName, int classId) {
        this(findClass(domainClassName), classId);
    }

    private int getLastKey() {
        return this.lastKey;
    }

    private void setLastKey(int lastKey) {
        this.lastKey = lastKey;
    }

    public static void ensureDomainRoot() {
        PersistenceBroker broker = PersistenceBrokerFactory.defaultPersistenceBroker();
        try {
            broker.beginTransaction();

            Connection conn = broker.serviceConnectionManager().getConnection();
            conn.setAutoCommit(false);

            Statement stm = conn.createStatement();

            ResultSet rs = stm.executeQuery("SELECT * FROM DOMAIN_ROOT WHERE OID = 1");

            boolean hasRoot = rs.next();
            rs.close();

            if (!hasRoot) {
                logger.info("DomainRoot not found. Initializing...");
                int value = stm.executeUpdate("INSERT INTO DOMAIN_ROOT (OID) VALUES (1)");

                if (value == 0) {
                    logger.error("Could not initialize DomainRoot!");
                } else {
                    logger.info("DomainRoot initialized successfully!");
                }
            }
            broker.commitTransaction();
            stm.close();
            conn.close();
        } catch (Throwable e) {
            throw new Error(e);
        } finally {
            broker.close();
        }
    }
}
