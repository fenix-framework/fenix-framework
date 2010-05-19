package dml.runtime;

import java.util.Iterator;
import java.util.NoSuchElementException;

import jvstm.util.Cons;

public class FunctionalSet<E> {
    public static final FunctionalSet EMPTY = new FunctionalSet(0, Cons.empty());

    protected final int size;
    protected final Cons<E> elems;
    
    protected FunctionalSet(int size, Cons<E> elems) {
        this.size = size;
        this.elems = elems;
    }

    public int size() {
        return size;
    }

    public E get(int index) {
        if (index > (size - 1)) {
            throw new NoSuchElementException();
        } else {
            Cons<E> iter = elems;
            while (index-- > 0) {
                iter = iter.rest();
            }
            return iter.first();
        }
    }

    public FunctionalSet<E> addUnique(E obj) {
        return makeFunctionalSet(size + 1, elems.cons(obj));
    }

    public FunctionalSet<E> add(E obj) {
        if (elems.contains(obj)) {
            return this;
        } else {
            return addUnique(obj);
        }
    }

    public FunctionalSet<E> remove(Object obj) {
        if (! elems.contains(obj)) {
            return this;
        } else {
            return removeExisting(obj);
        }
    }
    
    private FunctionalSet<E> removeExisting(Object obj) {
        return makeFunctionalSet(size - 1, elems.removeFirst(obj));
    }

    public boolean contains(Object obj) {
        return elems.contains(obj);
    }

    public Iterator<E> iterator() {
        return elems.iterator();
    }

    protected FunctionalSet<E> makeFunctionalSet(int size, Cons<E> elems) {
	return new FunctionalSet<E>(size, elems);
    }
}
