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
package jvstm.atomic;

import jvstm.Atomic;
import pt.ist.esw.advice.Advice;
import pt.ist.esw.advice.AdviceFactory;

public final class AtomicAdviceFactory extends AdviceFactory<Atomic> {

    private static AdviceFactory<Atomic> instance = new AtomicAdviceFactory();

    public static AdviceFactory<Atomic> getInstance() {
        return instance;
    }

    public Advice newAdvice(Atomic atomic) {
        if (atomic.readOnly())
            return DefaultAtomicContext.FLATTEN_READONLY;
        if (!atomic.canFail())
            return DefaultAtomicContext.FLATTEN_READWRITE;
        if (atomic.speculativeReadOnly())
            return DefaultAtomicContext.READ_ONLY;
        return DefaultAtomicContext.READ_WRITE;
    }

}
