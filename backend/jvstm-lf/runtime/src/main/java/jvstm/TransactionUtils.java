package jvstm;

import org.hibernate.annotations.common.AssertionFailure;

public class TransactionUtils {

    /* This operation needs to be performed atomically.  Currently, it is invoked
    during the bootstrap of Fenix Framework, so there is no other transaction
    running that might be changing the ActiveTransactionsRecord. */
    public static void initializeTxNumber(int maxTx) {
        ActiveTransactionsRecord initialRecord = new ActiveTransactionsRecord(maxTx, WriteSet.empty());
        boolean success = Transaction.mostRecentCommittedRecord.trySetNext(initialRecord);
        if (!success) {
            throw new AssertionError("Impossible condition: Failed to initializeTxNumber.");
        }
        Transaction.setMostRecentCommittedRecord(initialRecord);
    }

    /* smf: clearly need to revise this.  hook up with the code that will be
    able to discover the write set of any transaction (up to the oldest in the
    cluster) */
    public static ActiveTransactionsRecord getRecordForRemoteTransaction(int startVersion) {
        return new RemoteActiveTransactionsRecord(startVersion);
    }

    /**
     * An instance of this class is used whenever we need to create a RemoteLockFreeTransaction. It provides a way to pass the
     * transaction number up to the jvstm.Transaction constructor.
     */

    /* Care must be taken to ensure that no methods assume this to be a 'valid'
    ActiveTxRecord and use it in error. For this reason, all method that are not
    relevant are overriden here throwing an exception.  This allows detection
    in case they are used by mistake. */

    /* smf: Brainstorming: The active Tx records now may be more than
    the normal list: it's a set of lists that eventually join to the 'original'
    list.  This may happen when a remote transaction appears and there is no
    'local' ActiveTransactionsRecord for it.  Either because (1) it was already
    GC'ed, or (2) the local system is not that far ahead! (2) only happens after
    the node starts up: the system cannot be "not that far ahead" in other cases,
    because its applying the remote commits.  This is poorly explained, I know...*/

    /* smf: Provavelmente preciso de informação da persistencia se um record
    puder ser GCed e mais tarde aparecer um pedido de commit remoto. na pratica
    tenho de saber qual a versao mais antiga que o cluster pode precisar e manter
    a lista de vboxIds escritos em cada tx desde essa ate ha mais recente */
    private static class RemoteActiveTransactionsRecord extends ActiveTransactionsRecord {

        public RemoteActiveTransactionsRecord(int txNumber) {
            super(txNumber, WriteSet.empty());
        }

        @Override
        public ActiveTransactionsRecord getNext() {
            throw new UnsupportedOperationException("not yet implemented");
            /* Maybe take care to upgrade the transaction to a 'local'
            ActiveTransactionsRecord before calling this method */
        }

        @Override
        public boolean trySetNext(ActiveTransactionsRecord next) {
            throw new UnsupportedOperationException("not yet implemented");
            /* might be used to set a pointer to a valid 'local' record? or maybe
            even to another RemoteActiveTransactionsRecord before eventually
            merging into a local? */
        }

        @Override
        protected void setCommitted() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("not yet implemented.");
            /* probably not used, because this method is only invoked after
            committing the commitTxRecord, which should be an instance of a the
            super class */
        }

        @Override
        public boolean isCommitted() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("not yet implemented");
            /* probably always true, because this record is supposed to represent
            a past committed version.  However, I think it should not be used
            at all.  Again, because this method is used by the committing algorithm
            for a queued record waiting for the write-back. */
        }

        @Override
        public WriteSet getWriteSet() {
            throw new AssertionFailure("Should be used on a RemoteActiveTransactionsRecord instance.");
        }

        @Override
        public void clean() {
            throw new AssertionFailure("Should be used on a RemoteActiveTransactionsRecord instance.");
        }

    }

}
