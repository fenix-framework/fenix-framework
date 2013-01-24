package pt.ist.fenixframework.adt.linkedlist;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import pt.ist.fenixframework.adt.linkedlist.LinkedList_Base;
import pt.ist.fenixframework.dml.runtime.DomainBasedSet;

public class LinkedList<T extends Serializable> extends LinkedList_Base implements DomainBasedSet<T> {

    public  LinkedList() {
	super();
	setHead(new ListNode<T>(null, null, null));
    }

    public boolean insert(Comparable toInsert, T value) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable oid = null;
	while (next != null && (oid = next.getKeyValue().key).compareTo(toInsert) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	if (next == null || toInsert.compareTo(oid) != 0) {
	    previous.setNext(new ListNode(toInsert, value, next));
	    return true;
	}
	return false;
    }

    public boolean removeKey(Comparable toRemove) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable oid = null;
	while (next != null && (oid = next.getKeyValue().key).compareTo(toRemove) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	if (oid != null && toRemove.compareTo(oid) == 0) {
	    if (next != null) { 
		previous.setNext(next.getNext());
	    } else {
		previous.setNext(null);
	    }
	    return true;
	}
	return false;
    }

    public boolean containsKey(Comparable key) {
	ListNode<T> previous = getHead();
	ListNode<T> next = previous.getNext();
	Comparable oid = false;
	while (next != null && (oid = next.getKeyValue().key).compareTo(key) < 0) {
	    previous = next;
	    next = previous.getNext();
	}
	return next != null && key.compareTo(oid) == 0;
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
		Object value = iter.getKeyValue().value;
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
    public boolean add(Comparable key, T e) {
        return insert(key, e);
    }

    @Override
    public boolean remove(Comparable key) {
        return removeKey(key);
    }
    
    @Override
    public boolean contains(Comparable key) {
        return containsKey(key);
    }

}
