package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.jgroups.Address;

import java.io.IOException;
import java.util.*;

/**
 * @author Pedro Ruivo
 * @since 2.10
 */
public interface LoadBalancePolicy {

    public static final Iterator<Address> EMPTY_ITERATOR = new Iterator<Address>() {
        @Override
        public final boolean hasNext() {
            return false;
        }

        @Override
        public final Address next() {
            throw new NoSuchElementException();
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final String toString() {
            return "EMPTY_ITERATOR";
        }
    };

    void init(LoadBalanceTranslation translation, LoadBalanceChannel channel, String appName);

    Iterator<Address> locate(ConsistentHash consistentHash, String hint, boolean primaryBackup, boolean write);

    void getState(SendBuffer buffer, boolean worker, boolean coordinator) throws IOException;

    void setState(ReceivedBuffer buffer) throws IOException;

    Object handle(ReceivedBuffer buffer) throws IOException;

    public static interface LoadBalanceTranslation {

        Address translate(org.infinispan.remoting.transport.Address address);

    }

    public static interface LoadBalanceChannel {

        Object broadcast(SendBuffer buffer, boolean sync) throws Exception;

    }

    public static class SingleAddressIterator implements Iterator<Address> {
        private final Address address;
        private boolean hasNext;

        public SingleAddressIterator(Address address) {
            this.address = address;
            this.hasNext = address != null;
        }

        @Override
        public final boolean hasNext() {
            return hasNext;
        }

        @Override
        public final Address next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return address;
        }

        @Override
        public final void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "SingleAddressIterator{" +
                    "address=" + address +
                    '}';
        }
    }

    public static class AddressListIterator implements Iterator<Address> {
        private final LoadBalanceTranslation translation;
        private final Deque<org.infinispan.remoting.transport.Address> addressList;
        private Address next;

        public AddressListIterator(LoadBalanceTranslation translation,
                                   Set<org.infinispan.remoting.transport.Address> addresses) {
            this.translation = translation;
            addressList = new ArrayDeque<org.infinispan.remoting.transport.Address>(addresses);
        }

        public final AddressListIterator init() {
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
            return "ListLCDRIterator{" +
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
