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

import java.util.concurrent.Callable;

public abstract class Transaction {

    // static part starts here

    /*
     * The mostRecentRecord static field is volatile to ensure correct
     * synchronization among different threads:
     *
     * - A newly created transaction reads the value of this field at
     *   the very beginning of its existence, before trying to
     *   access any box.
     *
     * - A write transaction writes to this field at the very end,
     *   after commiting all the boxes to their new values.
     *
     * This way, because of the new semantics of the Java Memory
     * Model, as specified by JSR133 (which is incorporated in the
     * newest Java Language Specification), we know that all the
     * values written previously in the commit of write transaction
     * will be visible to any other transaction that is created with
     * the new value of the committed field.
     *
     * This change is sufficient to ensure the correct synchronization
     * guarantees, even if we remove all the remaining volatile
     * declarations from the VBox and VBoxBody classes.
     */
    protected static volatile ActiveTransactionsRecord mostRecentRecord = new ActiveTransactionsRecord(0, null);

    protected static final ThreadLocal<Transaction> current = new ThreadLocal<Transaction>();

    private static TransactionFactory TRANSACTION_FACTORY = new DefaultTransactionFactory();

    public static void setTransactionFactory(TransactionFactory factory) {
        TRANSACTION_FACTORY = factory;
    }

    public static Transaction current() {
        return current.get();
    }

    public static int getMostRecentCommitedNumber() {
        return mostRecentRecord.transactionNumber;
    }

    // this method is called during the commit of a write transaction
    // the commits are already synchronized, so this method doesn't need to be
    public static void setMostRecentActiveRecord(ActiveTransactionsRecord record) {
        mostRecentRecord.setNext(record);
        mostRecentRecord = record;
    }

    public static void addTxQueueListener(TxQueueListener listener) {
        ActiveTransactionsRecord.addListener(listener);
    }

    public static boolean isInTransaction() {
        return current.get() != null;
    }

    /**
     * Warning: this method has limited usability. See the UnsafeSingleThreaded class for
     * details
     */
    public static Transaction beginUnsafeSingleThreaded() {
        Transaction parent = current.get();
        if (parent != null) {
            throw new Error("Unsafe single-threaded transactions cannot be nested");
        }

        ActiveTransactionsRecord activeRecord = mostRecentRecord.getRecordForNewTransaction();
        Transaction tx = new UnsafeSingleThreadedTransaction(activeRecord);
        tx.start();
        return tx;
    }

    public static Transaction beginInevitable() {
        Transaction parent = current.get();
        if (parent != null) {
            throw new Error("Inevitable transactions cannot be nested");
        }

        ActiveTransactionsRecord activeRecord = mostRecentRecord.getRecordForNewTransaction();
        Transaction tx = new InevitableTransaction(activeRecord);
        tx.start();
        return tx;
    }

    public static Transaction begin() {
        return begin(false);
    }

    public static Transaction begin(boolean readOnly) {
        ActiveTransactionsRecord activeRecord = null;
        Transaction parent = current.get();

        if (parent == null) {
            activeRecord = mostRecentRecord.getRecordForNewTransaction();
        }

        return beginWithActiveRecord(readOnly, activeRecord);
    }

    private static Transaction beginWithActiveRecord(boolean readOnly, ActiveTransactionsRecord activeRecord) {
        Transaction parent = current.get();
        Transaction tx = null;

        if (parent == null) {
            if (readOnly) {
                tx = TRANSACTION_FACTORY.makeReadOnlyTopLevelTransaction(activeRecord);
            } else {
                tx = TRANSACTION_FACTORY.makeTopLevelTransaction(activeRecord);
            }
        } else {
            // passing the readOnly parameter to makeNestedTransaction is a temporary solution to
            // support the correct semantics in the composition of @Atomic annotations.  Ideally, we
            // should adjust the code generation of @Atomic to let WriteOnReadExceptions pass to the
            // parent
            tx = parent.makeNestedTransaction(readOnly);
        }
        tx.start();

        return tx;
    }

    public static void commitAndBegin(boolean readOnly) {
        Transaction tx = current.get();
        tx.commitTx(false);

        // prevent
        ActiveTransactionsRecord activeRecord = tx.getSameRecordForNewTransaction();

        // now it is safe to finish
        tx.finishTx();

        beginWithActiveRecord(readOnly, activeRecord);
    }

    // This method must be overridden in sub-classes that have an ActiveTransactionsRecord
    protected ActiveTransactionsRecord getSameRecordForNewTransaction() {
        return null;
    }

    public static void abort() {
        Transaction tx = current.get();
        tx.abortTx();
    }

    public static void commit() {
        Transaction tx = current.get();
        tx.commitTx(true);
    }

    public static void checkpoint() {
        Transaction tx = current.get();
        tx.commitTx(false);
    }

    public static Transaction suspend() {
        Transaction tx = current.get();
        tx.suspendTx();
        return tx;
    }

    public static void resume(Transaction tx) {
        if (current.get() != null) {
            throw new ResumeException("Can't resume a transaction into a thread with an active transaction already");
        }

        // In the previous lines I'm checking that the current thread
        // has no transaction, because, otherwise, we would lost the
        // current transaction.
        //
        // Likewise, I should not allow that the same transaction is
        // associated with more than one thread.  For that, however, I
        // would have to keep track of which thread owns each
        // transaction and change that atomically.  I recall having
        // the thread in each transaction but I removed sometime ago.
        // So, until I investigate this further, whoever is using this
        // resume stuff must be carefull, because the system will not
        // detect that the same transaction is being used in two
        // different threads.

        tx.resumeTx();
    }

    protected int number;
    protected final Transaction parent;

    public Transaction(int number) {
        this.number = number;
        this.parent = null;
    }

    public Transaction(Transaction parent) {
        this.number = parent.getNumber();
        this.parent = parent;
    }

    public void start() {
        current.set(this);
    }

    protected Transaction getParent() {
        return parent;
    }

    public int getNumber() {
        return number;
    }

    protected void setNumber(int number) {
        this.number = number;
    }

    protected void abortTx() {
        finishTx();
    }

    protected void commitTx(boolean finishAlso) {
        doCommit();

        if (finishAlso) {
            finishTx();
        }
    }

    private void finishTx() {
        finish();

        current.set(this.getParent());
    }

    protected void finish() {
        // intentionally empty
    }

    protected void suspendTx() {
        current.set(null);
    }

    protected void resumeTx() {
        current.set(this);
    }

    public abstract Transaction makeNestedTransaction(boolean readOnly);

    public abstract <T> T getBoxValue(VBox<T> vbox);

    public abstract <T> void setBoxValue(VBox<T> vbox, T value);

    public abstract <T> T getPerTxValue(PerTxBox<T> box, T initial);

    public abstract <T> void setPerTxValue(PerTxBox<T> box, T value);

    protected abstract void doCommit();

    public static void transactionallyDo(TransactionalCommand command) {
        while (true) {
            Transaction tx = Transaction.begin();
            try {
                command.doIt();
                tx.commit();
                tx = null;
                return;
            } catch (CommitException ce) {
                tx.abort();
                tx = null;
            } finally {
                if (tx != null) {
                    tx.abort();
                }
            }
        }
    }

    public static <T> T doIt(Callable<T> xaction) throws Exception {
        return doIt(xaction, false);
    }

    public static <T> T doIt(Callable<T> xaction, boolean tryReadOnly) throws Exception {
        T result = null;
        while (true) {
            Transaction.begin(tryReadOnly);
            boolean finished = false;
            try {
                result = xaction.call();
                Transaction.commit();
                finished = true;
                return result;
            } catch (CommitException ce) {
                Transaction.abort();
                finished = true;
            } catch (WriteOnReadException wore) {
                Transaction.abort();
                finished = true;
                tryReadOnly = false;
            } finally {
                if (!finished) {
                    Transaction.abort();
                }
            }
        }
    }
}
