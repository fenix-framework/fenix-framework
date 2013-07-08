package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.TopLevelTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;

public class LocalCommitOnlyTransaction extends CommitOnlyTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LocalCommitOnlyTransaction.class);

//    private static final long commitTxRecordOffset = UtilUnsafe.objectFieldOffset(TopLevelTransaction.class, "commitTxRecord");

    private final LockFreeTransaction decoratedTransaction;

//    private final WriteSet writeSet;

//    private final ConcurrentHashMap<Integer, UUID> txVersionToCommitIdMap = new ConcurrentHashMap<Integer, UUID>();

    public LocalCommitOnlyTransaction(CommitRequest commitRequest, LockFreeTransaction tx) {
        super(tx.getActiveTxRecord(), commitRequest, tx.makeWriteSet());
        this.decoratedTransaction = tx;
//        this.commitRequest = commitRequest;
//        this.writeSet = this.decoratedTransaction.makeWriteSet();

    }

//    @Override
//    public int getNumber() {
//        // smf: TODO check this
//        /* using this.decoratedTransaction.getNumber(); is safer, but
//        only works for LocalCommitOnlyTransaction.  On the other hand. can getNumber()
//        here always run the code below safely? Yes, if we don't upgrade the
//        transaction's version. */
//        return this.commitRequest.getTxVersion();
//    }

    @Override
    public TopLevelTransaction getUnderlyingTransaction() {
        return this.decoratedTransaction;
    }

//    @Override
//    protected void snapshotValidation(int lastSeenCommittedTxNumber) {
//        /* Notice that someone may have already enqueued this tx, in which case
//        I may happen to have seen it already committed! But in that case the
//        validation status must have been set already! So, the following test is
//        a double take: it is necessary for correctness (to avoid enqueueing the
//        same record twice and to throw an exception if already invalid), but it
//        may also bring in the lucky benefit of the validation status for this
//        tx already being set (either valid or invalid).  */
//        ValidationStatus currentStatus = this.commitRequest.getValidationStatus();
//        if (currentStatus != ValidationStatus.UNSET) {
//            if (currentStatus == ValidationStatus.VALID) {
//                logger.debug("Commit request {} was found already VALID", this.commitRequest.getId());
//                return;
//            } else {
//                logger.debug("Commit request {} was found already INVALID", this.commitRequest.getId());
//                /* Still, we throw exception to ensure that our own flow does not proceed to enqueueing */
//                TransactionSignaller.SIGNALLER.signalCommitFail();
//                throw new AssertionError("Impossible condition - Commit fail signalled!");
//            }
//        }
//
//        int myReadVersion = getNumber();
//
//        if (lastSeenCommittedTxNumber == myReadVersion) {
//            logger.debug("Commit request {} is immediately VALID", this.commitRequest.getId());
//            assignCommitRecord(lastSeenCommittedTxNumber + 1, this.writeSet);
//            return;
//        }
//
//        RemoteReadSet readSet = this.commitRequest.getReadSet();
//
//        // smf: TODO implement the helping mechanism here. For now, just iterate all.
//
//        JvstmLockFreeBackEnd backend = JvstmLockFreeBackEnd.getInstance();
//
//        for (String vboxId : readSet.getVBoxIds()) {
//            VBox vbox = backend.lookupCachedVBox(vboxId);
//            if (vbox == null) {
//                // smf: TODO this vbox is not cached locally. deal with this later
//                logger.error("not implemented yet. must deal with uncached vboxes in this node. cannot continue to commit deterministically. exiting");
//                System.exit(-1);
//            } else {
//                // check whether the read was valid
//                if (vbox.body.version > myReadVersion) {
//                    /* caution: it could be our own commit that we're seeing!
//                    But, in that case, validation must have finished! */
//
//                    boolean validationFinished = this.commitRequest.getValidationStatus() != ValidationStatus.UNSET;
//
//                    if (!validationFinished) {
//                        /* this validation did not finish yet (thus neither did
//                        the write back) *AND* there is already a newer version
//                        written to the vbox.  Thus, this transaction is invalid
//                        to commit. */
//                        logger.debug("Commit request {} is INVALID", this.commitRequest.getId());
//                        this.commitRequest.setInvalid();
//                        TransactionSignaller.SIGNALLER.signalCommitFail();
//                        throw new AssertionError("Impossible condition - Commit fail signalled!");
//                    } else {
//                        // whatever the result, validation has finished already
//                        if (this.commitRequest.getValidationStatus() == ValidationStatus.VALID) {
//                            logger.debug("Some helper already found commit request {} to be VALID", this.commitRequest.getId());
//                            return;
//                        } else {
//                            logger.debug("Some helper already found commit request {} to be INVALID", this.commitRequest.getId());
//                            TransactionSignaller.SIGNALLER.signalCommitFail();
//                            throw new AssertionError("Impossible condition - Commit fail signalled!");
//                        }
//                    }
//                }
//            }
//        }
//
//        logger.debug("Commit request {} is VALID", this.commitRequest.getId());
//        assignCommitRecord(lastSeenCommittedTxNumber + 1, this.writeSet);
//    }

//    @Override
//    public void updateOrecVersion() {
//        this.decoratedTransaction.updateOrecVersion();
//    }

//    /* The commitTxRecord can only be set once */
//    @Override
//    public void setCommitTxRecord(ActiveTransactionsRecord record) {
//        if (UNSAFE.compareAndSwapObject(this.decoratedTransaction, this.commitTxRecordOffset, null, record)) {
//            logger.debug("set commitTxRecord with version {}", record.transactionNumber);
//        } else {
//            logger.debug("commitTxRecord was already set with version {}", this.getCommitTxRecord().transactionNumber);
//        }
//
//    }

//    @Override
//    public ActiveTransactionsRecord getCommitTxRecord() {
//        return this.decoratedTransaction.getCommitTxRecord();
//    }

//    @Override
//    protected void helpCommit(ActiveTransactionsRecord recordToCommit) {
//        if (!recordToCommit.isCommitted()) {
//            int txVersion = this.getCommitTxRecord().transactionNumber;
//            UUID commitId = this.txVersionToCommitIdMap.get(txVersion);
//
//            if (commitId != null) { // may be null if it was already persisted 
//                JvstmLockFreeBackEnd.getInstance().getRepository().mapTxVersionToCommitId(txVersion, commitId);
//                this.txVersionToCommitIdMap.remove(txVersion);
//            }
//        }
//
//        super.helpCommit(recordToCommit);
//    }

//    @Override
//    protected void upgradeTx(ActiveTransactionsRecord newRecord) {
//        // no op.  
//        /* This is not a required step in this type of transaction.  The
//        corresponding LockFreeTransaction will do this on its own
//        node, after all the helping is done. */
//    }

}
