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

    public final JGroupsAddress get(Address address) {
        JGroupsAddress jGroupsAddress = new JGroupsAddress();
        JGroupsAddress existing = addressCache.putIfAbsent(address, jGroupsAddress);
        return existing == null ? jGroupsAddress : existing;
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

    public void updateLoad(org.jgroups.Address from, Address address, boolean overloaded) {
        if (address != null) {
            JGroupsAddress jGroupsAddress = new JGroupsAddress();
            JGroupsAddress existing = addressCache.putIfAbsent(address, jGroupsAddress);
            if (existing != null) {
                jGroupsAddress = existing;
            } else {
                jGroupsAddress.address = from;
            }
            jGroupsAddress.overloaded = overloaded;
        } else {
            for (JGroupsAddress existing : addressCache.values()) {
                if (existing.address.equals(from)) {
                    existing.overloaded = overloaded;
                    return;
                }
            }
        }

    }

    public static class JGroupsAddress {
        private final ReentrantLock lock;
        private volatile org.jgroups.Address address;
        private volatile boolean overloaded;

        public JGroupsAddress() {
            address = null;
            lock = new ReentrantLock();
        }

        public final org.jgroups.Address address() {
            return address;
        }

        public final void address(org.jgroups.Address address) {
            this.address = address;
        }

        public final boolean overloaded() {
            return overloaded;
        }

        public final void lock() {
            lock.lock();
        }

        public final void unlock() {
            lock.unlock();
        }
    }

}
