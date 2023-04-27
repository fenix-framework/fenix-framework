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

import jvstm.util.VLinkedSet;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

public class DefaultDependenceRecord implements DependenceRecord {
    protected final Object dependent;
    protected final Method predicate;
    protected final VLinkedSet<Depended> depended;

    public DefaultDependenceRecord(Object dependent, Method predicate, Set<? extends Depended> depended) {
        this.dependent = dependent;
        this.predicate = predicate;
        this.depended = new VLinkedSet<Depended>(depended);
    }

    public Object getDependent() {
        return dependent;
    }

    public Method getPredicate() {
        return predicate;
    }

    public Iterator<Depended> getDepended() {
        return depended.iterator();
    }

    public void addDepended(Depended dep) {
        depended.add(dep);
    }
}
