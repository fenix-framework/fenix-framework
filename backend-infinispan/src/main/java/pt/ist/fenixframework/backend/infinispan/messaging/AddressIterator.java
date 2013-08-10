package pt.ist.fenixframework.backend.infinispan.messaging;

import org.jgroups.Address;

import java.util.*;

/**
 * @author Pedro Ruivo
 * @since 2.10
 */
public class AddressIterator implements Iterator<Address> {
    private final LoadBalancePolicy.LoadBalanceTranslation translation;
    private final Deque<org.infinispan.remoting.transport.Address> addressList;
    private Address next;

    public AddressIterator(LoadBalancePolicy.LoadBalanceTranslation translation, Set<org.infinispan.remoting.transport.Address> addresses) {
        this.translation = translation;
        addressList = new ArrayDeque<org.infinispan.remoting.transport.Address>(addresses);
    }

    public final AddressIterator init() {
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
