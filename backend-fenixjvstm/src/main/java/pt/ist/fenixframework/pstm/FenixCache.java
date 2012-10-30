package pt.ist.fenixframework.pstm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.ist.fenixframework.DomainObject;


public class FenixCache {

    private static final ReferenceQueue<DomainObject> refQueue = new ReferenceQueue<DomainObject>();

    private ConcurrentHashMap<Long,CacheEntry> cache;

    public FenixCache() {
	this.cache = new ConcurrentHashMap<Long,CacheEntry>();
    }

    public DomainObject cache(DomainObject obj) {
	processQueue();
	Long key = obj.getOid();
	CacheEntry newEntry = new CacheEntry(obj, key, this.refQueue);

	return cacheNewEntry(newEntry, obj);
    }

    private DomainObject cacheNewEntry(CacheEntry newEntry, DomainObject obj) {
	CacheEntry entryInCache = putIfAbsent(this.cache, newEntry.key, newEntry);

	if (entryInCache == newEntry) {
	    return obj;
	} else {
	    DomainObject objInCache = entryInCache.get();
	    if (objInCache != null) {
		return objInCache;
	    } else {
		// the entry in cache was GCed already, so remove it and retry
		removeEntry(entryInCache);
		return cacheNewEntry(newEntry, obj);
	    }
	}
    }

    public DomainObject lookup(Long key) {
	processQueue();
	CacheEntry entry = this.cache.get(key);
	if (entry != null) {
	    DomainObject result = entry.get();
	    if (result != null) {
		return result;
	    } else {
		removeEntry(entry);
		return null;
	    }
	} else {
	    return null;
	}
    }


    private void removeEntry(CacheEntry entry) {
	this.cache.remove(entry.key, entry);
    }

    /* This method stores the new value if an older one didn't exist already.  In either case it returns the value that was left
     * in the cache.  This implementation works as intended because we assume that we never store a null value in the
     * cache. Otherwise, map.putIfAbsent would not put a new value over a null entry.
     *
     * This method's behaviour is very important to ensure that we do not inadvertently permit more than one reference to the same
     * domain object to wander around in the system.
     */
    private static <K,V> V putIfAbsent(ConcurrentHashMap<K,V> map, K key, V value) {
	V oldValue = map.putIfAbsent(key, value);
	return ((oldValue == null) ? value : oldValue);
    }

    private void processQueue() {
        CacheEntry gcedEntry = (CacheEntry)refQueue.poll();
        while (gcedEntry != null) {
	    removeEntry(gcedEntry);
	    gcedEntry = (CacheEntry)refQueue.poll();
        }
    }


    private static class CacheEntry extends SoftReference<DomainObject> {
	private final Long key;

        CacheEntry(DomainObject object, Long key, ReferenceQueue q) {
            super(object, q);
	    this.key = key;
        }
    }
}
