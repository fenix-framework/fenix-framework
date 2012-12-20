package pt.ist.fenixframework.core.adt.bplustree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class LinkedList<T extends AbstractDomainObject> extends LinkedList_Base implements Iterable<T> {

    public  LinkedList() {
	super();
    }

    public void insert(T value) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable toInsert = value.getOid();
	Comparable oid;
	while ((oid = next.getValue().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	if (toInsert.compareTo(oid) != 0) {
	    previous.setNext(new ListNode(value, next));
	}
    }

    public void remove(T value) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable toInsert = value.getOid();
	Comparable oid;
	while ((oid = next.getValue().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	if (toInsert.compareTo(oid) == 0) {
	    previous.setNext(next.getNext());
	}
    }

    public boolean contains(T value) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable toInsert = value.getOid();
	Comparable oid;
	while ((oid = next.getValue().getOid()).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	return toInsert.compareTo(oid) == 0;
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

	    private ListNode<T> iter = getHead();
	    
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

}
