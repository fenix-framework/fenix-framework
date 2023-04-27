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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ReadWriteTransaction extends Transaction {
    protected static final Object NULL_VALUE = new Object();

    protected static final Map EMPTY_MAP = Collections.emptyMap();

    //protected Cons<Pair<VBox,VBoxBody>> bodiesRead = Cons.empty();
    protected Map<VBox, VBoxBody> bodiesRead = EMPTY_MAP;
    protected Map<VBox, Object> boxesWritten = EMPTY_MAP;
    protected Map<PerTxBox, Object> perTxValues = EMPTY_MAP;

    public ReadWriteTransaction(int number) {
        super(number);
    }

    public ReadWriteTransaction(ReadWriteTransaction parent) {
        super(parent);
    }

    public Transaction makeNestedTransaction(boolean readOnly) {
        // always create a RW nested transaction, because we need its read-set
        return new NestedTransaction(this);
    }

    ReadWriteTransaction getRWParent() {
        return (ReadWriteTransaction) getParent();
    }

    @Override
    protected void finish() {
        super.finish();
        // to allow garbage collecting the collections
        bodiesRead = null;
        boxesWritten = null;
        perTxValues = null;
    }

    protected void doCommit() {
        tryCommit();
        // if commit is successful, then reset transaction to a clean state
        //bodiesRead = Cons.empty();
        bodiesRead = EMPTY_MAP;
        boxesWritten = EMPTY_MAP;
        perTxValues = EMPTY_MAP;
    }

    protected abstract void tryCommit();

    protected <T> T getLocalValue(VBox<T> vbox) {
        T value = null;
        if (boxesWritten != EMPTY_MAP) {
            value = (T) boxesWritten.get(vbox);
        }
        if ((value == null) && (parent != null)) {
            value = getRWParent().getLocalValue(vbox);
        }

        return value;
    }

    public <T> T getBoxValue(VBox<T> vbox) {
        T value = getLocalValue(vbox);
        if (value == null) {
            VBoxBody<T> body = vbox.body.getBody(number);
            //bodiesRead = bodiesRead.cons(new Pair<VBox,VBoxBody>(vbox, body));
            if (bodiesRead == EMPTY_MAP) {
                bodiesRead = new HashMap<VBox, VBoxBody>();
            }
            bodiesRead.put(vbox, body);
            value = body.value;
        }
        return (value == NULL_VALUE) ? null : value;
    }

    public <T> void setBoxValue(VBox<T> vbox, T value) {
        if (boxesWritten == EMPTY_MAP) {
            boxesWritten = new HashMap<VBox, Object>();
        }
        boxesWritten.put(vbox, value == null ? NULL_VALUE : value);
    }

    protected <T> T getPerTxValue(PerTxBox<T> box) {
        T value = null;
        if (perTxValues != EMPTY_MAP) {
            value = (T) perTxValues.get(box);
        }
        if ((value == null) && (parent != null)) {
            value = getRWParent().getPerTxValue(box);
        }
        return value;
    }

    public <T> T getPerTxValue(PerTxBox<T> box, T initial) {
        T value = getPerTxValue(box);
        if (value == null) {
            value = initial;
        }

        return value;
    }

    public <T> void setPerTxValue(PerTxBox<T> box, T value) {
        if (perTxValues == EMPTY_MAP) {
            perTxValues = new HashMap<PerTxBox, Object>();
        }
        perTxValues.put(box, value);
    }
}
