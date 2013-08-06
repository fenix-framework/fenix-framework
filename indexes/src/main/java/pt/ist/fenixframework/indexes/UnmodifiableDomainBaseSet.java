package pt.ist.fenixframework.indexes;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.dml.runtime.DomainBasedMap;
import eu.cloudtm.LocalityHints;

public class UnmodifiableDomainBaseSet<T extends AbstractDomainObject> implements Set<T> {

    public final DomainBasedMap<T> delegate;

    public UnmodifiableDomainBaseSet(DomainBasedMap<T> delegate) {
        if (delegate == null) {
            this.delegate = new EmptyDomainBasedMap();
        } else {
            this.delegate = delegate;
        }
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

    private final class EmptyDomainBasedMap implements DomainBasedMap<T> {

        @Override
        public String getExternalId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LocalityHints getLocalityHints() {
            throw new UnsupportedOperationException();
        }

        @Override
        public T get(Comparable key) {
            return null;
        }
        
        @Override
        public T getCached(boolean forceMiss, Comparable key) {
            return null;
        }

        @Override
        public boolean putIfMissing(Comparable key, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void put(Comparable key, T value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Comparable key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Comparable key) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        @Override
        public Iterator<T> iteratorCached(boolean forceMiss) {
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new NoSuchElementException();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

    }

}
