package dml.runtime;

import jvstm.VBox;

import java.util.Collection;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Set;

public class VSet<E> extends AbstractSet<E> implements Set<E> {

    private VBox<FunctionalSet<E>> elements = new VBox<FunctionalSet<E>>(FunctionalSet.EMPTY);

    public VSet() {
    }

    public VSet(Collection<? extends E> coll) {
        this();
        addAll(coll);
    }
    
    public int size() {
        return elements.get().size();
    }

    public boolean contains(Object o) {
        return elements.get().contains(o);
    }

    public void clear() {
        elements.put(FunctionalSet.EMPTY);
    }

    public Iterator<E> iterator() {
        return new VSetIterator<E>(elements.get());
    }

    public boolean add(E o) {
        FunctionalSet<E> oldSet = elements.get();
        FunctionalSet<E> newSet = oldSet.add(o);
        if (newSet == oldSet) {
            return false;
        } else {
            elements.put(newSet);
            return true;
        }
    }

    public boolean remove(Object o) {
        FunctionalSet<E> oldSet = elements.get();
        FunctionalSet<E> newSet = oldSet.remove(o);
        if (newSet == oldSet) {
            return false;
        } else {
            elements.put(newSet);
            return true;
        }
    }


    private class VSetIterator<E> implements Iterator<E> {
        private Iterator<E> iterator;
        private E current = null;
        private boolean canRemove = false;

        VSetIterator(FunctionalSet<E> funcSet) {
            this.iterator = funcSet.iterator();
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public E next() {
            E result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }
            
        public void remove() {
            if (! canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                VSet.this.remove(current);
            }
        }
    }
}
