package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.jgroups.Address;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

    void init(LoadBalanceTranslation translation);

    Iterator<Address> locate(ConsistentHash consistentHash, String hint, boolean primaryBackup, boolean write);

    public static interface LoadBalanceTranslation {

        Address translate(org.infinispan.remoting.transport.Address address);

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


}
