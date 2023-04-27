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
package jvstm.cps;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ChainedIterator<E> implements Iterator<E> {

    private Iterator<? extends Iterator<? extends E>> iterators;
    private Iterator<? extends E> current;

    public ChainedIterator(Iterator<? extends Iterator<? extends E>> iterators) {
        this.iterators = iterators;

        if (iterators.hasNext()) {
            this.current = iterators.next();
            updateCurrent();
        } else {
            this.current = null;
        }
    }

    private void updateCurrent() {
        while ((current != null) && (!current.hasNext())) {
            current = (iterators.hasNext()) ? iterators.next() : null;
        }
    }

    public boolean hasNext() {
        return (current != null);
    }

    public E next() {
        if (current != null) {
            E result = current.next();
            updateCurrent();
            return result;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
