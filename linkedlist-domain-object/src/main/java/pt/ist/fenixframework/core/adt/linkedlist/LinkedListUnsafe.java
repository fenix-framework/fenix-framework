package pt.ist.fenixframework.core.adt.linkedlist;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class LinkedListUnsafe<T extends AbstractDomainObject> extends LinkedListUnsafe_Base implements Set<T> {

    public  LinkedListUnsafe() {
	super();
	setHead(new ListNode<T>(null, null));
    }

    public boolean insert(T value) {
	ListNode<T> previous = getHeadUnsafe();
	ListNode<T> next = previous.getNextUnsafe();
	Comparable toInsert = value.getOid();
	Comparable oid = null;
	while (next != null && (oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNextUnsafe();
	}
	previous.registerGetNext();
	if (next == null || toInsert.compareTo(oid) != 0) {
	    previous.setNext(new ListNode(value, next));
	    return true;
	}
	return false;
    }

    public boolean remove(T value) {
	ListNode<T> previous = getHeadUnsafe();
	ListNode<T> next = previous.getNextUnsafe();
	Comparable toInsert = value.getOid();
	Comparable oid = null;
	while (next != null && (oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNextUnsafe();
	}
	previous.registerGetNext();
	if (oid != null && toInsert.compareTo(oid) == 0) {
	    if (next != null) { 
		previous.setNext(next.getNext());
	    } else {
		previous.setNext(null);
	    }
	    return true;
	}
	return false;
    }

    public boolean contains(T value) {
	ListNode<T> previous = getHeadUnsafe();
	ListNode<T> next = previous.getNextUnsafe();
	Comparable toInsert = value.getOid();
	Comparable oid = false;
	while (next != null && (oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNextUnsafe();
	}
	previous.registerGetNext();
	return next != null && toInsert.compareTo(oid) == 0;
    }
    
    public int size() {
	ListNode<T> iter = getHead();
	int size = 0;
	while (iter != null) {
	    size++;
	    iter = iter.getNext();
	}
	return size;
    }
    
    public Iterator<T> iterator() {
	return new Iterator<T>() {

	    private ListNode<T> iter = getHead().getNext(); // skip head tomb
	    
	    @Override
	    public boolean hasNext() {
		return iter != null;
	    }

	    @Override
	    public T next() {
		if (iter == null) {
		    throw new NoSuchElementException();
		}
		Object value = iter.getValue();
		iter = iter.getNext();
		return (T)value;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	    }

	};
    }

    @Override
    public boolean add(T e) {
        return insert(e);
    }

    @Override
    public boolean remove(Object o) {
        if (! (o instanceof AbstractDomainObject)) {
            return false;
        }
        return remove((T)o);
    }
    
    @Override
    public boolean contains(Object o) {
        if (! (o instanceof AbstractDomainObject)) {
            return false;
        }
        return contains((T)o);
    }

    /* The following methods are not needed at the moment but we need to implement Set */
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	throw new UnsupportedOperationException();
    }
}
