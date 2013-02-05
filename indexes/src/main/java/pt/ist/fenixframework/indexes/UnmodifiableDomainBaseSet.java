package pt.ist.fenixframework.indexes;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.runtime.DomainBasedMap;

public class UnmodifiableDomainBaseSet<T extends AbstractDomainObject> implements Set<T> {

    private final DomainBasedMap<T> delegate;
    
    public UnmodifiableDomainBaseSet(DomainBasedMap<T> delegate) {
	this.delegate = delegate;
    }
    
    @Override
    public boolean add(T e) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
	if (o instanceof AbstractDomainObject) {
	    return delegate.contains(((AbstractDomainObject) o).getOid());
	} else {
	    return false;
	}
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	for (Object obj : c) {
	    if (!contains(obj)) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean isEmpty() {
	return !delegate.iterator().hasNext();
    }

    @Override
    public Iterator<T> iterator() {
	return delegate.iterator();
    }

    @Override
    public boolean remove(Object o) {
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
    public int size() {
	return delegate.size();
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
