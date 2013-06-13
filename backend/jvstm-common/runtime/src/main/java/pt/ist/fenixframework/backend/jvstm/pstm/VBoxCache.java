package pt.ist.fenixframework.backend.jvstm.pstm;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

public class VBoxCache {
    private final static VBoxCache instance = new VBoxCache();

    private static final ReferenceQueue<VBox> refQueue = new ReferenceQueue<VBox>();

    private final ConcurrentHashMap<String, CacheEntry> cache;

    public VBoxCache() {
        this.cache = new ConcurrentHashMap<String, CacheEntry>();
    }

    public static VBoxCache getCache() {
        return instance;
    }

    public int size() {
        return this.cache.size();
    }

    public VBox cache(VBox vbox) {
        processQueue();
        String key = vbox.getId();
        CacheEntry newEntry = new CacheEntry(vbox, key, this.refQueue);

        return cacheNewEntry(newEntry, vbox);
    }

    private VBox cacheNewEntry(CacheEntry newEntry, VBox vbox) {
        CacheEntry entryInCache = putIfAbsent(this.cache, newEntry.key, newEntry);

        if (entryInCache == newEntry) {
            return vbox;
        } else {
            VBox vboxInCache = entryInCache.get();
            if (vboxInCache != null) {
                return vboxInCache;
            } else {
                // the entry in cache was GCed already, so remove it and retry
                removeEntry(entryInCache);
                return cacheNewEntry(newEntry, vbox);
            }
        }
    }

    public VBox lookup(String key) {
        processQueue();
        CacheEntry entry = this.cache.get(key);
        if (entry != null) {
            VBox result = entry.get();
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
    private static <K, V> V putIfAbsent(ConcurrentHashMap<K, V> map, K key, V value) {
        V oldValue = map.putIfAbsent(key, value);
        return ((oldValue == null) ? value : oldValue);
    }

    private void processQueue() {
        CacheEntry gcedEntry = (CacheEntry) refQueue.poll();
        while (gcedEntry != null) {
            removeEntry(gcedEntry);
            gcedEntry = (CacheEntry) refQueue.poll();
        }
    }

    private static class CacheEntry extends SoftReference<VBox> {
        private final String key;

        CacheEntry(VBox vbox, String key, ReferenceQueue q) {
            super(vbox, q);
            this.key = key;
        }
    }
}
