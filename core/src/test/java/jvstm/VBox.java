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
package jvstm;

public class VBox<E> {
    public VBoxBody<E> body;

    public VBox() {
        this((E) null);
    }

    public VBox(E initial) {
        this.body = new VBoxBody<E>(initial, 0, null);
    }

    // used for persistence support
    protected VBox(VBoxBody<E> body) {
        this.body = body;
    }

    public E get() {
        Transaction tx = Transaction.current();
        if (tx == null) {
            // Access the box body without creating a full transaction, while
            // still preserving ordering guarantees by 'piggybacking' on the
            // version from the latest commited transaction.
            // If the box body is GC'd before we can reach it, the process
            // re-starts with a newer transaction.
            while (true) {
                int transactionNumber = Transaction.mostRecentRecord.transactionNumber;
                VBoxBody<E> boxBody = this.body;
                do {
                    if (boxBody.version <= transactionNumber) {
                        return boxBody.value;
                    }
                    boxBody = boxBody.next;
                } while (boxBody != null);
            }
        } else {
            return tx.getBoxValue(this);
        }
    }

    public void put(E newE) {
        Transaction tx = Transaction.current();
        if (tx == null) {
            tx = Transaction.beginInevitable();
            tx.setBoxValue(this, newE);
            tx.commit();
        } else {
            tx.setBoxValue(this, newE);
        }
    }

    public VBoxBody<?> commit(E newValue, int txNumber) {
        VBoxBody<E> newBody = makeNewBody(newValue, txNumber, this.body);
        this.body = newBody;
        return newBody;
    }

    // in the future, if more than one subclass of body exists, we may
    // need a factory here but, for now, it's simpler to have it like
    // this
    public static <T> VBoxBody<T> makeNewBody(T value, int version, VBoxBody<T> next) {
        return new VBoxBody<T>(value, version, next);
    }
}
