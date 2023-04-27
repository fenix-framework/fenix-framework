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

import jvstm.ReadTransaction;
import jvstm.Transaction;
import jvstm.VBox;

import java.util.HashSet;
import java.util.Set;

public class DefaultConsistencyCheckTransaction extends ReadTransaction implements ConsistencyCheckTransaction {

    protected HashSet<VBox> boxesRead = new HashSet<VBox>();
    protected int numStarts = 0;

    public DefaultConsistencyCheckTransaction(Transaction parent) {
        super(parent);
    }

    public Transaction makeNestedTransaction() {
        return this;
    }

    public void start() {
        if (numStarts == 0) {
            super.start();
        }
        numStarts++;
    }

    protected void finish() {
        numStarts--;
        if (numStarts == 0) {
            super.finish();
        }
    }

    public <T> T getBoxValue(VBox<T> vbox) {
        boxesRead.add(vbox);
        // ask the parent transaction (a RW tx) for the value of the box
        return parent.getBoxValue(vbox);
    }

    public Set<Depended> getDepended() {
        Set<Depended> depended = new HashSet<Depended>(boxesRead.size());

        for (VBox box : boxesRead) {
            depended.add(getDependedForBox(box));
        }

        return depended;
    }

    protected Depended getDependedForBox(VBox box) {
        return DependedVBoxes.getDependedForBox(box);
    }
}
