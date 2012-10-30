package pt.ist.fenixframework.backend.mem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;

public class DomainClassInfo {

    private static final Logger logger = LoggerFactory.getLogger(DomainClassInfo.class);
    private volatile static Map<Class, DomainClassInfo> classInfoMap;
    private volatile static DomainClassInfo[] classInfoById;
    private volatile static long serverOidBase;

    static void initializeClassInfos(DomainModel model, int serverId) {
        serverOidBase = (long)serverId << 48;  // the server id provides de 16 most significant bits of the OID

        int maxId = 0;
        Map<Class, DomainClassInfo> map = new IdentityHashMap<Class, DomainClassInfo>();
        ArrayList<DomainClassInfo> array = new ArrayList<DomainClassInfo>();

        // special case: create record for DomainRoot (must get class id = 0)
        addNewInfo(map, array, new DomainClassInfo(DomainRoot.class, 0));

        // create all other records, skipping DomainRoot of course
        for (DomainClass domClass : model.getDomainClasses()) {
            Class javaClass;
            try {
                javaClass = Class.forName(domClass.getFullName());
            } catch (ClassNotFoundException e) {
                throw new ExceptionInInitializerError(e);
            }

            if (javaClass != DomainRoot.class && !map.containsKey(javaClass)) {
                DomainClassInfo classInfo = new DomainClassInfo(javaClass, ++maxId);
                addNewInfo(map, array, classInfo);

            }
        }

        // finish the initialization by assigning to the static variables
        classInfoMap = Collections.unmodifiableMap(map);
        classInfoById = new DomainClassInfo[maxId + 1];
        array.toArray(classInfoById);
    }

    // private static Class findClass(String classname) {
    //     try {
    //         return Class.forName(classname);
    //     } catch (ClassNotFoundException cnfe) {
    //         // domain classes may disappear, but their id should not be reused
    //         // so, if the corresponding Java class does not exist, return null
    //         return null;
    //     }
    // }

    private static void addNewInfo(Map<Class, DomainClassInfo> map, ArrayList<DomainClassInfo> array, DomainClassInfo info) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering new domain class '" + info.domainClass.getName() + "' with id " + info.classId);
        }

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
        DomainClassInfo domainClassInfo = classInfoMap.get(objClass);
        if (domainClassInfo == null) {
            throw new RuntimeException("Domain class not registered: " + objClass.getCanonicalName());
        }
        return domainClassInfo.classId;
    }

    private static Class mapIdToClass(int cid) {
        if ((cid < 0) || (cid >= classInfoById.length)) {
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

    public static long getNextOidFor(Class objClass) {
        int nextKey;
        DomainClassInfo info = classInfoMap.get(objClass);

        synchronized(info) {
            int lastKey = info.getLastKey();
            nextKey = lastKey + 1;
            info.setLastKey(nextKey);
        }
        
        if (logger.isDebugEnabled()) {
            StringBuilder message = new StringBuilder();
            message.append("New OID: Server(");
            message.append((int)(serverOidBase >> 48));
            message.append("), Class(");
            message.append(info.classId);
            message.append("), Object(");
            message.append(nextKey);
            message.append("): ");
            message.append(serverOidBase);
            message.append(" + ");
            message.append(((long)info.classId << 32));
            message.append(" + ");
            message.append(nextKey);
            message.append(" = ");
            message.append((serverOidBase + ((long)info.classId << 32) + nextKey));
            logger.debug(message.toString());
        }
        // build and return OID
	if ((DomainRoot.class == objClass) && (nextKey == 1)) {
	    // this first DomainRoot instance is special and always takes a known value, regardless of the serverOidBase
	    return 1L;
	} else {
	    return serverOidBase + ((long)info.classId << 32) + nextKey;
	}
    }


    // the non-static part starts here

    public final transient Class domainClass;
    public final int classId;
    /** The maximum object key used for objects of this class in this server */
    private transient int lastKey = 0;

    public DomainClassInfo(Class domainClass, int classId) {
	this.domainClass = domainClass;
	this.classId = classId;
    }

    protected int getLastKey() {
        return this.lastKey;
    }

    protected void setLastKey(int lastKey) {
        this.lastKey = lastKey;
    }

}
