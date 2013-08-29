package pt.ist.fenixframework.backend.infinispan.messaging.drd;

import org.infinispan.distribution.ch.ConsistentHash;
import org.jgroups.Address;
import org.slf4j.Logger;
import pt.ist.fenixframework.backend.infinispan.messaging.LoadBalancePolicy;
import pt.ist.fenixframework.backend.infinispan.messaging.ReceivedBuffer;
import pt.ist.fenixframework.backend.infinispan.messaging.SendBuffer;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Data-chasing Request Dispatching
 *
 * @author Pedro Ruivo
 * @since 2.10
 */
public class DRDLoadBalancePolicy implements LoadBalancePolicy {

    private static final Logger log = getLogger(DRDLoadBalancePolicy.class);
    private final Random random;
    private LoadBalanceTranslation translation;

    public DRDLoadBalancePolicy() {
        random = new Random();
    }

    @Override
    public final void init(LoadBalanceTranslation translation, LoadBalanceChannel ignored, String ignored2) {
        this.translation = translation;
    }

    @Override
    public Iterator<Address> locate(ConsistentHash consistentHash, String hint, boolean primaryBackup, boolean write) {
        Set<org.infinispan.remoting.transport.Address> addressSetOrdered = new LinkedHashSet<org.infinispan.remoting.transport.Address>();
        if (primaryBackup && write) {
            org.infinispan.remoting.transport.Address primary = consistentHash.getMembers().get(0);
            if (log.isTraceEnabled()) {
                log.trace("DRD Load Balance Policy: [Primary Backup + Write]: " + primary);
            }
            return new SingleAddressIterator(translation.translate(primary));
        } else if (primaryBackup) {
            List<org.infinispan.remoting.transport.Address> members = consistentHash.getMembers();
            org.infinispan.remoting.transport.Address primary = members.get(0);
            if (members.size() == 1) {
                if (log.isTraceEnabled()) {
                    log.trace("DRD Load Balance Policy: [Primary Backup + Read]: " + primary);
                }
                return new SingleAddressIterator(translation.translate(primary));
            }

            copyOwnerAndMembers(consistentHash, hint, addressSetOrdered);
            addressSetOrdered.remove(primary);

            if (log.isTraceEnabled()) {
                log.trace("DRD Load Balance Policy: [Primary Backup + Read]: " + addressSetOrdered);
            }

            return new ListDRDIterator(translation, addressSetOrdered).init();
        } else if (isFullReplication(consistentHash)) {
            //full replicated
            copyMembers(consistentHash, addressSetOrdered);
            if (log.isTraceEnabled()) {
                log.trace("DRD Load Balance Policy [Full Replication]: " + addressSetOrdered);
            }
            return new ListDRDIterator(translation, addressSetOrdered).init();
        }

        copyOwnerAndMembers(consistentHash, hint, addressSetOrdered);

        if (log.isTraceEnabled()) {
            log.trace("DRD Load Balance Policy: [Dynamic]: " + addressSetOrdered);
        }
        return new ListDRDIterator(translation, addressSetOrdered).init();
    }

    @Override
    public void getState(SendBuffer buffer, boolean worker, boolean coordinator) {/*no-op*/}

    @Override
    public void setState(ReceivedBuffer buffer) {/*no-op*/}

    @Override
    public Object handle(ReceivedBuffer buffer) {
        return null; //no-op
    }

    private void copyMembers(ConsistentHash consistentHash, Set<org.infinispan.remoting.transport.Address> orderedSet) {
        List<org.infinispan.remoting.transport.Address> copy =
                new ArrayList<org.infinispan.remoting.transport.Address>(consistentHash.getMembers());
        shuffle(copy);
        orderedSet.addAll(copy);
    }

    private void copyOwnerAndMembers(ConsistentHash consistentHash, String key, Set<org.infinispan.remoting.transport.Address> orderedSet) {
        if (key == null || isFullReplication(consistentHash)) {
            copyMembers(consistentHash, orderedSet);
        } else {
            List<org.infinispan.remoting.transport.Address> copy =
                    new ArrayList<org.infinispan.remoting.transport.Address>(consistentHash.locateOwners(key));
            shuffle(copy);
            orderedSet.addAll(copy);
            copyMembers(consistentHash, orderedSet);
        }
    }

    private boolean isFullReplication(ConsistentHash consistentHash) {
        return consistentHash.getNumSegments() == 1;
    }

    private <T> void shuffle(List<T> list) {
        if (list.size() == 2) {
            if (random.nextInt(100) >= 50) {
                list.set(0, list.set(1, list.get(0)));
            }
        } else if (list.size() > 2) {
            Collections.shuffle(list, random);
        }
    }

    private class ListDRDIterator implements Iterator<Address> {

        private final LoadBalanceTranslation translation;
        private final Deque<org.infinispan.remoting.transport.Address> addressList;
        private Address next;

        public ListDRDIterator(LoadBalanceTranslation translation, Set<org.infinispan.remoting.transport.Address> addresses) {
            this.translation = translation;
            addressList = new ArrayDeque<org.infinispan.remoting.transport.Address>(addresses);
        }

        public final ListDRDIterator init() {
            setNext();
            return this;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Address next() {
            if (next == null) {
                throw new NoSuchElementException();
            }
            Address toReturn = next;
            setNext();
            return toReturn;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "ListDRDIterator{" +
                    "addressList=" + addressList +
                    ", next=" + next +
                    '}';
        }

        private void setNext() {
            if (addressList.isEmpty()) {
                next = null;
                return;
            }
            do {
                next = translation.translate(addressList.pollFirst());
            } while (next == null && !addressList.isEmpty());
        }
    }
}
