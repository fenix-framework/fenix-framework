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

public class InevitableTransaction extends Transaction {

    private ActiveTransactionsRecord activeTxRecord;
    private Cons<VBoxBody> bodiesCommitted = Cons.empty();

    public InevitableTransaction(ActiveTransactionsRecord activeRecord) {
        super(activeRecord.transactionNumber);
        this.activeTxRecord = activeRecord;
    }

    @Override
    public void start() {
        // acquire the lock before starting the transaction, and
        // release it on transaction finish
        TopLevelTransaction.COMMIT_LOCK.lock();

        // we may need to upgrade to a different transaction number if
        // other transactions have committed since this transaction
        // was created, to ensure that it is the latest transaction
        // running
        ActiveTransactionsRecord newestRecord = mostRecentRecord;
        if (newestRecord != this.activeTxRecord) {
            // the correct order is to increment first the
            // new, and only then decrement the old
            newestRecord.incrementRunning();
            this.activeTxRecord.decrementRunning();
            this.activeTxRecord = newestRecord;
        }

        // once we get here, we may already increment the transaction
        // number
        int newTxNumber = this.activeTxRecord.transactionNumber + 1;

        // renumber the TX to the new number
        setNumber(newTxNumber);

        super.start();
    }

    @Override
    protected void abortTx() {
        commitTx(true);
        //throw new Error("An Inevitable transaction cannot abort.  I've committed it instead.");
    }

    @Override
    protected void finish() {
        super.finish();
        activeTxRecord.decrementRunning();
        TopLevelTransaction.COMMIT_LOCK.unlock();
    }

    public Transaction makeNestedTransaction(boolean readOnly) {
        throw new Error(getClass().getSimpleName() + " doesn't support nesting yet");
    }

    public <T> T getBoxValue(VBox<T> vbox) {
        return vbox.body.value;
    }

    public <T> void setBoxValue(VBox<T> vbox, T value) {
        if ((vbox.body != null) && (vbox.body.version == number)) {
            vbox.body.value = value;
        } else {
            VBoxBody<T> newBody = VBox.makeNewBody(value, number, vbox.body);
            if (vbox.body != null)
                bodiesCommitted = bodiesCommitted.cons(newBody);
            vbox.body = newBody;
        }
    }

    public <T> T getPerTxValue(PerTxBox<T> box, T initial) {
        throw new Error(getClass().getSimpleName() + " doesn't support PerTxBoxes yet");
    }

    public <T> void setPerTxValue(PerTxBox<T> box, T value) {
        throw new Error(getClass().getSimpleName() + " doesn't support PerTxBoxes yet");
    }

    protected void doCommit() {
        // the commit is already done, so create a new ActiveTransactionsRecord
        ActiveTransactionsRecord newRecord = new ActiveTransactionsRecord(getNumber(), bodiesCommitted);
        setMostRecentActiveRecord(newRecord);

        // we must update the activeRecords accordingly

        // the correct order is to increment first the
        // new, and only then decrement the old
        newRecord.incrementRunning();
        this.activeTxRecord.decrementRunning();
        this.activeTxRecord = newRecord;
    }
}
