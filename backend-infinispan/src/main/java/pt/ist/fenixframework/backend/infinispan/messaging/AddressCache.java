package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.remoting.transport.Address;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class AddressCache {

    private final ConcurrentMap<Address, JGroupsAddress> addressCache;

    public AddressCache() {
        addressCache = new ConcurrentHashMap<Address, JGroupsAddress>();
    }

    public final org.jgroups.Address get(Address address) {
        JGroupsAddress jGroupsAddress = addressCache.get(address);
        return jGroupsAddress == null ? null : jGroupsAddress.address;
    }

    public final void add(Address ispnAddress, org.jgroups.Address jgrpAddress) {
        JGroupsAddress existing = addressCache.putIfAbsent(ispnAddress, new JGroupsAddress());
        if (existing == null) {
            existing = addressCache.get(ispnAddress);
        }
        existing.address = jgrpAddress;
    }

    public final void lock(Address address) {
        JGroupsAddress existing = addressCache.putIfAbsent(address, new JGroupsAddress());
        if (existing == null) {
            existing = addressCache.get(address);
        }
        existing.lock.lock();
    }

    public final void unlock(Address address) throws InterruptedException {
        JGroupsAddress existing = addressCache.putIfAbsent(address, new JGroupsAddress());
        if (existing == null) {
            existing = addressCache.get(address);
        }
        existing.lock.unlock();
    }

    public final void clean(List<org.jgroups.Address> members) {
        ArrayList<Address> toRemove = new ArrayList<Address>(members.size());
        for (Map.Entry<Address, JGroupsAddress> entry : addressCache.entrySet()) {
            if (!members.contains(entry.getValue().address)) {
                toRemove.add(entry.getKey());
            }
        }
        for (Address address : toRemove) {
            addressCache.remove(address);
        }
    }

    public final Map<String, String> cacheAsString() {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<Address, JGroupsAddress> entry : addressCache.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().address.toString());
        }
        return result;
    }

    private class JGroupsAddress {
        private org.jgroups.Address address;
        private final ReentrantLock lock;

        public JGroupsAddress() {
            address = null;
            lock = new ReentrantLock();
        }
    }

}
