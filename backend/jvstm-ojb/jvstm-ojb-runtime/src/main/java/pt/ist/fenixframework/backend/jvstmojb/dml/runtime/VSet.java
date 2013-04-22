package pt.ist.fenixframework.backend.jvstmojb.dml.runtime;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import jvstm.VBox;

public class VSet<E> extends AbstractSet<E> implements Set<E> {

    private VBox<FunctionalSet<E>> elements = new VBox<FunctionalSet<E>>(FunctionalSet.EMPTY);

    public VSet() {
    }

    public VSet(Collection<? extends E> coll) {
        this();
        addAll(coll);
    }

    @Override
    public int size() {
        return elements.get().size();
    }

    @Override
    public boolean contains(Object o) {
        return elements.get().contains(o);
    }

    @Override
    public void clear() {
        elements.put(FunctionalSet.EMPTY);
    }

    @Override
    public Iterator<E> iterator() {
        return new VSetIterator<E>(elements.get());
    }

    @Override
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

    @Override
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

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            E result = iterator.next();
            canRemove = true;
            current = result;
            return current;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            } else {
                canRemove = false;
                VSet.this.remove(current);
            }
        }
    }
}
