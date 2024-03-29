/*
 * JVSTM: a Java library for Software Transactional Memory
 * Copyright (C) 2005 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package jvstm.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Cons<E> implements Iterable<E> {
    protected static final Cons EMPTY = new Cons(null, null);

    public static <T> Cons<T> empty() {
        return (Cons<T>) EMPTY;
    }

    protected final E first;
    protected final Cons<E> rest;

    private Cons(E first, Cons<E> rest) {
        this.first = first;
        this.rest = rest;
    }

    public Cons<E> cons(E elem) {
        return new Cons<E>(elem, this);
    }

    public E first() {
        if (isEmpty()) {
            throw new EmptyListException();
        } else {
            return first;
        }
    }

    public Cons<E> rest() {
        if (isEmpty()) {
            throw new EmptyListException();
        } else {
            return rest;
        }
    }

    public Cons<E> removeFirst(Object elem) {
        Cons<E> found = member(elem);
        if (found == null) {
            return this;
        } else {
            return removeExistingCons(found);
        }
    }

    public Cons<E> removeAll(Object elem) {
        Cons<E> lastFound = lastMember(elem);
        if (lastFound == null) {
            return this;
        } else if (lastFound == this) {
            return rest;
        } else {
            // skip over conses containing the elem
            Cons<E> next = this;
            if (elem == null) {
                while ((next != lastFound) && (next.first == null)) {
                    next = next.rest;
                }
            } else {
                while ((next != lastFound) && (elem.equals(next.first))) {
                    next = next.rest;
                }
            }

            if (next == lastFound) {
                return next.rest;
            }

            // We have to allocate new Cons cells until we reach the lastFound cons
            Cons<E> newCons = ((Cons<E>) EMPTY).cons(next.first);
            next = next.rest;

            if (elem == null) {
                while (next != lastFound) {
                    if (next.first != null) {
                        newCons = newCons.cons(next.first);
                    }
                    next = next.rest;
                }
            } else {
                while (next != lastFound) {
                    if (!elem.equals(next.first)) {
                        newCons = newCons.cons(next.first);
                    }
                    next = next.rest;
                }
            }

            // share the rest
            newCons = newCons.reverseInto(next.rest);
            return newCons;
        }
    }

    public Cons<E> removeCons(Cons<?> cons) {
        Cons<?> iter = this;
        while ((iter != cons) && (iter != EMPTY)) {
            iter = iter.rest;
        }

        if (iter == EMPTY) {
            return this;
        } else {
            return removeExistingCons(cons);
        }
    }

    private Cons<E> removeExistingCons(Cons<?> cons) {
        if (cons == this) {
            return rest;
        } else {
            // We have to allocate new Cons cells until we reach the cons to remove
            Cons<E> newCons = ((Cons<E>) EMPTY).cons(first);
            Cons<E> next = rest;
            while (next != cons) {
                newCons = newCons.cons(next.first);
                next = next.rest;
            }

            // share the rest
            newCons = newCons.reverseInto(next.rest);
            return newCons;
        }
    }

    public int size() {
        int size = 0;
        Cons<?> iter = this;
        while (iter != EMPTY) {
            size++;
            iter = iter.rest;
        }
        return size;
    }

    public boolean isEmpty() {
        return (this == EMPTY);
    }

    public boolean contains(Object elem) {
        return member(elem) != null;
    }

    public Cons<E> member(Object elem) {
        Cons<E> iter = this;
        if (elem == null) {
            while (iter != EMPTY) {
                if (iter.first == null) {
                    return iter;
                }
                iter = iter.rest;
            }
        } else {
            while (iter != EMPTY) {
                if (elem.equals(iter.first)) {
                    return iter;
                }
                iter = iter.rest;
            }
        }
        return null;
    }

    public Cons<E> lastMember(Object elem) {
        Cons<E> found = null;
        Cons<E> iter = this;
        if (elem == null) {
            while (iter != EMPTY) {
                if (iter.first == null) {
                    found = iter;
                }
                iter = iter.rest;
            }
        } else {
            while (iter != EMPTY) {
                if (elem.equals(iter.first)) {
                    found = iter;
                }
                iter = iter.rest;
            }
        }

        return found;
    }

    public Cons<E> reverse() {
        return reverseInto((Cons<E>) EMPTY);
    }

    public Cons<E> reverseInto(Cons<E> tail) {
        Cons<E> result = tail;
        Cons<E> iter = this;
        while (iter != EMPTY) {
            result = result.cons(iter.first);
            iter = iter.rest;
        }
        return result;
    }

    public Iterator<E> iterator() {
        return new ConsIterator<E>(this);
    }

    static class ConsIterator<T> implements Iterator<T> {
        private Cons<T> current;

        ConsIterator(Cons<T> start) {
            this.current = start;
        }

        public boolean hasNext() {
            return (current != EMPTY);
        }

        public T next() {
            if (current == EMPTY) {
                throw new NoSuchElementException();
            } else {
                T result = current.first;
                current = current.rest;
                return result;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
