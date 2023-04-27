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

public class NestedTransaction extends ReadWriteTransaction {

    public NestedTransaction(ReadWriteTransaction parent) {
        super(parent);
    }

    protected void tryCommit() {
        ReadWriteTransaction parent = getRWParent();
        //parent.bodiesRead = this.bodiesRead.reverseInto(parent.bodiesRead);
        if (parent.bodiesRead == EMPTY_MAP) {
            parent.bodiesRead = bodiesRead;
        } else {
            parent.bodiesRead.putAll(bodiesRead);
        }
        if (parent.boxesWritten == EMPTY_MAP) {
            parent.boxesWritten = boxesWritten;
        } else {
            parent.boxesWritten.putAll(boxesWritten);
        }
        if (parent.perTxValues == EMPTY_MAP) {
            parent.perTxValues = perTxValues;
        } else {
            parent.perTxValues.putAll(perTxValues);
        }
    }
}
