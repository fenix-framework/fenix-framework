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

import jvstm.CommitException;
import jvstm.Transaction;
import jvstm.WriteOnReadException;
import pt.ist.esw.advice.Advice;

import java.util.concurrent.Callable;

public enum DefaultAtomicContext implements Advice {

    FLATTEN_READONLY(true, true), FLATTEN_READWRITE(true, false), READ_ONLY(false, true), READ_WRITE(false, false);

    private final boolean flattenTx;
    private final boolean tryReadOnly;

    private DefaultAtomicContext(boolean flatten, boolean speculativeReadOnly) {
        flattenTx = flatten;
        tryReadOnly = speculativeReadOnly;
    }

    @Override
    public final <V> V perform(Callable<V> method) throws Exception {
        boolean inTransaction = Transaction.isInTransaction();
        if (flattenTx && inTransaction) {
            return method.call();
        }

        boolean readOnly = tryReadOnly;
        while (true) {
            Transaction.begin(readOnly);
            boolean txFinished = false;
            try {
                V result = method.call();
                Transaction.commit();
                txFinished = true;
                return result;
            } catch (CommitException ce) {
                Transaction.abort();
                txFinished = true;
            } catch (WriteOnReadException wore) {
                Transaction.abort();
                txFinished = true;
                readOnly = false;
            } finally {
                if (!txFinished) {
                    Transaction.abort();
                }
            }
        }
    }

}
