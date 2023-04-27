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

import jvstm.util.Cons;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class TopLevelTransaction extends ReadWriteTransaction {
    protected static final ReentrantLock COMMIT_LOCK = new ReentrantLock(true);
    static final CommitException COMMIT_EXCEPTION = new CommitException();

    protected ActiveTransactionsRecord activeTxRecord;

    public TopLevelTransaction(ActiveTransactionsRecord activeRecord) {
        super(activeRecord.transactionNumber);
        this.activeTxRecord = activeRecord;
    }

    @Override
    protected ActiveTransactionsRecord getSameRecordForNewTransaction() {
        this.activeTxRecord.incrementRunning();
        return this.activeTxRecord;
    }

    @Override
    protected void finish() {
        super.finish();
        activeTxRecord.decrementRunning();
    }

    protected boolean isWriteTransaction() {
        return (!boxesWritten.isEmpty()) || (!perTxValues.isEmpty());
    }

    protected void tryCommit() {
        if (isWriteTransaction()) {
            //Thread currentThread = Thread.currentThread();
            //int origPriority = currentThread.getPriority();
            //currentThread.setPriority(Thread.MAX_PRIORITY);
            COMMIT_LOCK.lock();
            try {
                if (validateCommit()) {
                    Cons<VBoxBody> bodiesCommitted = performValidCommit();
                    // the commit is already done, so create a new ActiveTransactionsRecord
                    ActiveTransactionsRecord newRecord = new ActiveTransactionsRecord(getNumber(), bodiesCommitted);
                    setMostRecentActiveRecord(newRecord);

                    // as this transaction changed number, we must
                    // update the activeRecords accordingly

                    // the correct order is to increment first the
                    // new, and only then decrement the old
                    newRecord.incrementRunning();
                    this.activeTxRecord.decrementRunning();
                    this.activeTxRecord = newRecord;
                } else {
                    throw COMMIT_EXCEPTION;
                }
            } finally {
                COMMIT_LOCK.unlock();
                //currentThread.setPriority(origPriority);
            }
        }
    }

    protected Cons<VBoxBody> performValidCommit() {
        int newTxNumber = getMostRecentCommitedNumber() + 1;

        // renumber the TX to the new number
        setNumber(newTxNumber);
        for (Map.Entry<PerTxBox, Object> entry : perTxValues.entrySet()) {
            entry.getKey().commit(entry.getValue());
        }

        return doCommit(newTxNumber);
    }

    protected boolean validateCommit() {
        //for (Pair<VBox,VBoxBody> entry : bodiesRead) {
        //    if (entry.first.body != entry.second) {
        //        return false;
        //    }
        //}
        for (Map.Entry<VBox, VBoxBody> entry : bodiesRead.entrySet()) {
            if (entry.getKey().body != entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    protected Cons<VBoxBody> doCommit(int newTxNumber) {
        Cons<VBoxBody> newBodies = Cons.empty();

        for (Map.Entry<VBox, Object> entry : boxesWritten.entrySet()) {
            VBox vbox = entry.getKey();
            Object newValue = entry.getValue();

            VBoxBody<?> newBody = vbox.commit((newValue == NULL_VALUE) ? null : newValue, newTxNumber);
            newBodies = newBodies.cons(newBody);
        }

        return newBodies;
    }
}
