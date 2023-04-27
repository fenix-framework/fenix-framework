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

import jvstm.VBox;
import jvstm.util.VLinkedSet;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class DependedVBoxes {

    // class used to store the depended-aspect of a VBox, so that we
    // don't need to change the VBox class
    // this class is simply a VLinkedSet (hence, a versioned set) that
    // answers to the Depended interface
    static class DependedState extends VLinkedSet<DependenceRecord> implements Depended {
        public void addDependence(DependenceRecord record) {
            add(record);
        }

        public void removeDependence(DependenceRecord record) {
            remove(record);
        }

        public Set<DependenceRecord> getDependenceRecords() {
            return this;
        }
    }

    // this implementation is just for a proof-of-concept

    // the use of global-lock to access the map is not, most probably,
    // the best choice for this, but it will do for now

    // the use of a ConcurrentHashMap would be preferable, but I would need a
    // ConcurrentWeakHashMap that does not exist yet...

    private static final Map<VBox, Depended> DEPENDED = new WeakHashMap<VBox, Depended>();

    public static Depended getDependedForBox(VBox box) {
        return getDependedForBox(box, true);
    }

    public static Depended getDependedForBoxIfExists(VBox box) {
        return getDependedForBox(box, false);
    }

    public synchronized static Depended getDependedForBox(VBox box, boolean create) {
        Depended dep = DEPENDED.get(box);

        if ((dep == null) && create) {
            // no depended exists yet, so create one and store it
            dep = new DependedState();
            DEPENDED.put(box, dep);
        }

        return dep;
    }
}
