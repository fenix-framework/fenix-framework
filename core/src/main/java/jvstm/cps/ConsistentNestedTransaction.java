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

import jvstm.NestedTransaction;
import jvstm.ReadWriteTransaction;
import jvstm.util.Cons;

public class ConsistentNestedTransaction extends NestedTransaction implements ConsistentTransaction {

    private Cons newObjects = Cons.empty();

    public ConsistentNestedTransaction(ReadWriteTransaction parent) {
        super(parent);
    }

    public void registerNewObject(Object obj) {
        newObjects = newObjects.cons(obj);
    }

    public void registerNewObjects(Cons objs) {
        newObjects = objs.reverseInto(newObjects);
    }

    public Cons getNewObjectsRegister() {
        return newObjects;
    }

    @Override
    protected void tryCommit() {
        ((ConsistentTransaction) getParent()).registerNewObjects(newObjects);
        super.tryCommit();
    }
}
