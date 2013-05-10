package pt.ist.fenixframework.backend.jvstm.pstm;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;

public class DomainClassInfo implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DomainClassInfo.class);
    private volatile static Map<Class<? extends AbstractDomainObject>, DomainClassInfo> classInfoMap;
    private volatile static DomainClassInfo[] classInfoById;
    private volatile static long serverOidBase;
    private static int serverId = -1; // will be provided via DomainClassInfo.initializeClassInfos(...)

    public static int getServerId() {
        return serverId;
    }

    public static void initializeClassInfos(DomainModel domainModel, int serverId) {
        DomainClassInfo.serverId = serverId;
        serverOidBase = (long) serverId << 48;  // the server id provides the 16 most significant bits of the OID

        logger.info("serverId: {}, serverOidBase: {}", serverId, Long.toHexString(serverOidBase));

        try {
            Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map =
                    importClassInfoMap(JVSTMBackEnd.getInstance().getRepository().getDomainClassInfos());
            ArrayList<DomainClassInfo> array = new ArrayList<DomainClassInfo>();

            // read all infos and initialize
            int maxId = 0;

            for (DomainClassInfo classInfo : map.values()) {
                logger.info("Existing domain class '{}' with id '{}'", classInfo.domainClassName,
                        Long.toHexString(classInfo.classId));
                maxId = Math.max(maxId, classInfo.classId);
                addNewInfoToArray(array, classInfo);
            }

            maxId = createAnyMissingRecords(map, array, maxId, domainModel);

            // the commit was ok, so finish the initialization by
            // assigning to the static variables
            classInfoMap = Collections.unmodifiableMap(map);
            classInfoById = new DomainClassInfo[maxId + 1];
            array.toArray(classInfoById);
        } catch (Exception e) {
            // if an exception occurs, throw an error
            throw new Error(e);
        }
    }

    private static int createAnyMissingRecords(Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map,
            ArrayList<DomainClassInfo> array, int maxId, DomainModel domainModel) throws ClassNotFoundException {
        ArrayList<DomainClassInfo> newClasses = new ArrayList<DomainClassInfo>();

        for (DomainClass domClass : domainModel.getDomainClasses()) {
            Class<? extends AbstractDomainObject> javaClass = findClass(domClass.getFullName());
            if (!map.containsKey(javaClass)) {
                // special case: record for DomainRoot must get class id = 0
                int id = javaClass == DomainRoot.class ? 0 : ++maxId;
                DomainClassInfo classInfo = new DomainClassInfo(javaClass, id);
                addNewInfo(map, array, classInfo);
                newClasses.add(classInfo);

                if (logger.isInfoEnabled()) {
                    logger.info("Registering new domain class '{}' with id '{}'", javaClass.getName(),
                            Long.toHexString(classInfo.classId));
                }
            }
        }

        if (!newClasses.isEmpty()) {
            JVSTMBackEnd.getInstance().getRepository()
                    .storeDomainClassInfos(newClasses.toArray(new DomainClassInfo[newClasses.size()]));
        }
        return maxId;
    }

    // Simplifies the serialization of the class info map, by stripping the keys and leaving only the entrySet, which already
    // contains the keys information.  This reduces the space required for serialization, and MOST IMPORTANTLY it avoids
    // serializing instances of the class Class.  Serializing classes that represent domain classes, would required putting a
    // serialVersionUID in each domainclass, lest we have (de-)serialization incompatibilities.
//    private static DomainClassInfo[] exportClassInfoMap(Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map) {
//        return map.values().toArray(new DomainClassInfo[map.size()]);
//    }

    private static Map<Class<? extends AbstractDomainObject>, DomainClassInfo> importClassInfoMap(
            DomainClassInfo[] domainClassInfos) {
        Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map =
                new IdentityHashMap<Class<? extends AbstractDomainObject>, DomainClassInfo>();

        for (DomainClassInfo info : domainClassInfos) {
            map.put(info.domainClass, info);  // smf: could this use addNewInfoToMap()?
        }
        return map;
    }

    // This class is responsible for defining the behavior of mapping a domain class
    // when the domain class is no longer available.
    public static Class<? extends AbstractDomainObject> findClass(String classname) {
        try {
            return Class.forName(classname).asSubclass(AbstractDomainObject.class);
        } catch (ClassNotFoundException cnfe) {
            // domain classes may disappear, but their id should not be reused
            // so, if the corresponding Java class does not exist, return null
            // E.g. this exception can occur when a class is removed from the
            // DomainModel, but its record still exists in storage 
            logger.info("Domain class '{}' was not found.", classname);
            return null;
        }
    }

    private static void addNewInfo(Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map,
            ArrayList<DomainClassInfo> array, DomainClassInfo info) {
        addNewInfoToMap(map, info);
        addNewInfoToArray(array, info);
    }

    private static void addNewInfoToMap(Map<Class<? extends AbstractDomainObject>, DomainClassInfo> map, DomainClassInfo info) {
        if (info.domainClass != null) {
            map.put(info.domainClass, info);
        }
    }

    private static void addNewInfoToArray(ArrayList<DomainClassInfo> array, DomainClassInfo info) {
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

    public static int mapClassToId(Class<? extends AbstractDomainObject> objClass) {
        DomainClassInfo domainClassInfo = classInfoMap.get(objClass);
        if (domainClassInfo == null) {
            throw new RuntimeException("Domain class not registered: " + objClass.getCanonicalName());
        }
        return domainClassInfo.classId;
    }

    private static Class<? extends AbstractDomainObject> mapIdToClass(int cid) {
        if ((cid < 0) || (cid >= classInfoById.length)) {
            return null;
        } else {
            return classInfoById[cid].domainClass;
        }
    }

    private static int mapOidToClassId(long oid) {
        // shift class id to rightmost position and clear server id bits
        return ((int) (oid >> 32)) & 0x0000FFFF;
    }

    public static Class<? extends AbstractDomainObject> mapOidToClass(long oid) {
        return mapIdToClass(mapOidToClassId(oid));
    }

    public static long getNextOidFor(Class<? extends AbstractDomainObject> objClass) throws Exception {
        int nextKey;
        DomainClassInfo info = classInfoMap.get(objClass);
        long oid;

        synchronized (info) {
            int lastKey = info.getLastKey();
            if (lastKey == UNKNOWN_KEY) {  // not yet initialized from the persistent storage
                lastKey = getLastKeyFor(info);
                logger.debug("Initialize last used counter for class {}: {}", info.domainClassName, lastKey);
            }

            nextKey = lastKey + 1;
            info.setLastKey(nextKey);
        }

        // build the OID
        if ((DomainRoot.class == objClass) && (nextKey == 1)) {
            // this first DomainRoot instance is special and always takes a known value, regardless of the serverOidBase
            oid = 1L;
        } else {
            oid = serverOidBase + ((long) info.classId << 32) + nextKey;
        }

        if (logger.isDebugEnabled()) {
            String serverIdHex = Long.toHexString(serverOidBase);
            String classIdHex = Long.toHexString(info.classId);
            String objectIdHex = Long.toHexString(nextKey);

            logger.debug("New OID: {},{},{} -> {} ({})", serverIdHex, classIdHex, objectIdHex, Long.toHexString(oid),
                    objClass.getCanonicalName());
        }

        // inform the Repository of the new OID; it **may** require such knowledge
        JVSTMBackEnd.getInstance().getRepository().updateMaxCounterForClass(info, nextKey);

        return oid;
    }

    /* Invocations to this method should be synchronized in the <code>info</code> argument */
    private static int getLastKeyFor(DomainClassInfo info) throws Exception {
        int maxCounter = JVSTMBackEnd.getInstance().getRepository().getMaxCounterForClass(info);

        return maxCounter < 0 ? UNKNOWN_KEY : maxCounter;
    }

    private static final int UNKNOWN_KEY = 0;

    // the non-static part starts here

    public final String domainClassName;
    public final transient Class<? extends AbstractDomainObject> domainClass;
    public final int classId;
    /** The maximum object key used for objects of this class in this server */
    private transient int lastKey = UNKNOWN_KEY;

    public DomainClassInfo(Class<? extends AbstractDomainObject> domainClass, int classId) {
        this(domainClass.getName(), domainClass, classId);
    }

    public DomainClassInfo(String domainClassName, int classId) {
        this(domainClassName, findClass(domainClassName), classId);
    }

    public DomainClassInfo(String domainClassName, Class<? extends AbstractDomainObject> domainClass, int classId) {
        this.domainClassName = domainClassName;
        this.domainClass = domainClass;
        this.classId = classId;
    }

    public int getLastKey() {
        return this.lastKey;
    }

    private void setLastKey(int lastKey) {
        this.lastKey = lastKey;
    }

    // serialization code
    protected Object writeReplace() throws ObjectStreamException {
        return new SerializedForm(this);
    }

    private static class SerializedForm implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String className;
        private final int classId;

        SerializedForm(DomainClassInfo obj) {
            this.className = obj.domainClassName;
            this.classId = obj.classId;
        }

        Object readResolve() throws ObjectStreamException, ClassNotFoundException {
            return new DomainClassInfo(this.className, this.classId);
        }
    }
}
