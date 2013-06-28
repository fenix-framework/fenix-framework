package pt.ist.fenixframework.backend.jvstm.pstm;

import static jvstm.UtilUnsafe.UNSAFE;
import jvstm.ActiveTransactionsRecord;
import jvstm.TopLevelTransaction;
import jvstm.TransactionSignaller;
import jvstm.UtilUnsafe;
import jvstm.VBox;
import jvstm.WriteSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest.ValidationStatus;
import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeBackEnd;
import pt.ist.fenixframework.backend.jvstm.lf.RemoteReadSet;

public class LocalCommitOnlyTransaction extends CommitOnlyTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LocalCommitOnlyTransaction.class);

    private static final long commitTxRecordOffset = UtilUnsafe.objectFieldOffset(TopLevelTransaction.class, "commitTxRecord");

    private final CommitRequest commitRequest;

    private final LockFreeTransaction decoratedTransaction;

    private final WriteSet writeSet;

    public LocalCommitOnlyTransaction(CommitRequest commitRequest, LockFreeTransaction tx) {
        super(tx.getActiveTxRecord());
        this.decoratedTransaction = tx;
        this.commitRequest = commitRequest;
        this.writeSet = this.decoratedTransaction.makeWriteSet();

    }

    @Override
    public boolean isWriteTransaction() {
        return true;
    }

    @Override
    public int getNumber() {
        // smf: TODO check this
        /* using this.decoratedTransaction.getNumber(); is safer, but
        only works for LocalCommitOnlyTransaction.  On the other hand. can getNumber()
        here always run the code below safely? Yes, if we don't upgrade the
        transaction's version. */
        return this.commitRequest.getTxVersion();
    }

    /*
     The validation for local transactions follows the usual protocol: (1)
    helpCommitAll, (2) snapshotValidation, and (3) validateAndEnqueue.  The
    difference is that now others may also be helping with the validation. So:

    - initially, check if validation already completed.  This is also useful to
    avoid starting the validation when there is only one (already validated)
    entry in the commit requests queue, and we're just waiting for more requests
    to arrive.

    - snapshot validation is a helped phase (split into buckets).  if it fails
    the request is marked as failed.  however, care must be taken to check that
    if a read vbox already contains a more recent entry, such entry may belong
    to this transaction already! (someone else helped quite a lot!).  In that
    case, the whole commit for this transaction is done! :-)  As usual, a more
    recent write by another to a VBox that was read, will cause validation to
    fail.
    
    - validateCommitAndEnqueue only needs to be attempted once.  Given that
    snapshotValidation passed, then if enqueue fails it's because someone else
    did it already.  Moving along...
     */

    @Override
    protected void validate() {
        logger.debug("Validating commit request: {}", this.commitRequest.getId());

        // some other helper may have already done the work for us
        ValidationStatus validationStatus = this.commitRequest.getValidationStatus();
        boolean alreadyValidated = validationStatus != ValidationStatus.UNSET;

        if (alreadyValidated) {
            logger.debug("Commit request {} is already {}", this.commitRequest.getId(),
                    (validationStatus == validationStatus.VALID ? "VALID" : "INVALID"));
            return;
        }

        super.validate();
    }

    @Override
    protected void snapshotValidation(int lastSeenCommittedTxNumber) {
        /* Notice that someone may have already enqueued this tx, in which case
        I may happen to have seen it already committed! But in that case the
        validation status must have been set already! So, the following test is
        a double take: it is necessary for correctness (to avoid enqueueing the
        same record twice), but it may also bring in the lucky benefit of the
        validation status for this tx already being set (either valid or invalid).
        */
        ValidationStatus currentStatus = this.commitRequest.getValidationStatus();
        if (currentStatus != ValidationStatus.UNSET) {
            if (currentStatus == ValidationStatus.VALID) {
                logger.debug("Commit request {} was found already VALID", this.commitRequest.getId());
                return;
            } else {
                logger.debug("Commit request {} was found already INVALID", this.commitRequest.getId());
                /* Still, we throw exception to ensure that our own flow does not proceed to enqueueing */
                TransactionSignaller.SIGNALLER.signalCommitFail();
                throw new AssertionError("Impossible condition - Commit fail signalled!");
            }
        }

        int myReadVersion = getNumber();

        if (lastSeenCommittedTxNumber == myReadVersion) {
            logger.debug("Commit request {} is immediately VALID", this.commitRequest.getId());
            // Must set the correct commit number **BEFORE** setting the valid status
            assignCommitRecord(lastSeenCommittedTxNumber + 1, this.writeSet);
            this.commitRequest.setValid();
            return;
        }

        RemoteReadSet readSet = this.commitRequest.getReadSet();

        // smf: TODO implement the helping mechanism here. For now, just iterate all.

        JvstmLockFreeBackEnd backend = JvstmLockFreeBackEnd.getInstance();

        for (String vboxId : readSet.getVBoxIds()) {
            VBox vbox = backend.lookupCachedVBox(vboxId);
            if (vbox == null) {
                // smf: TODO this vbox is not cached locally. deal with this later
                logger.error("not implemented yet. must deal with uncached vboxes in this node. cannot continue to commit deterministically. exiting");
                System.exit(-1);
            } else {
                // check whether the read was valid
                if (vbox.body.version > myReadVersion) {
                    /* caution: it could be our own commit that we're seeing!
                    But, in that case, validation must have finished! */

                    boolean validationFinished = this.commitRequest.getValidationStatus() != ValidationStatus.UNSET;

                    if (!validationFinished) {
                        /* this validation did not finish yet (thus neither did
                        the write back) *AND* there is already a newer version
                        written to the vbox.  Thus, this transaction is invalid
                        to commit. */
                        logger.debug("Commit request {} is INVALID", this.commitRequest.getId());
                        this.commitRequest.setInvalid();
                        TransactionSignaller.SIGNALLER.signalCommitFail();
                        throw new AssertionError("Impossible condition - Commit fail signalled!");
                    } else {
                        // whatever the result, validation has finished already
                        if (this.commitRequest.getValidationStatus() == ValidationStatus.VALID) {
                            logger.debug("Some helper already found commit request {} to be VALID", this.commitRequest.getId());
                            return;
                        } else {
                            logger.debug("Some helper already found commit request {} to be INVALID", this.commitRequest.getId());
                            TransactionSignaller.SIGNALLER.signalCommitFail();
                            throw new AssertionError("Impossible condition - Commit fail signalled!");
                        }
                    }
                }
            }
        }

        logger.debug("Commit request {} is VALID", this.commitRequest.getId());
        // Must set the correct commit number **BEFORE** setting the valid status
        assignCommitRecord(lastSeenCommittedTxNumber + 1, this.writeSet);
        this.commitRequest.setValid();
    }

    @Override
    protected void validateCommitAndEnqueue(ActiveTransactionsRecord lastCheck) {
        enqueueValidCommit(lastCheck, this.decoratedTransaction.getCommitTxRecord().getWriteSet());

        updateOrecVersion();
    }

    @Override
    public void updateOrecVersion() {
        this.decoratedTransaction.updateOrecVersion();
    }

    @Override
    protected void enqueueValidCommit(ActiveTransactionsRecord lastCheck, WriteSet writeSet) {
        ActiveTransactionsRecord commitRecord = this.decoratedTransaction.getCommitTxRecord();

        /* Here we know that our commit is valid.  However, we may have concluded
        such result via some helper AND even have seen already our record enqueued
        and commit. So we need to check for that to skip enqueuing. */
        if (lastCheck.transactionNumber >= commitRecord.transactionNumber) {
            logger.debug("Transaction {} of commit request {} was already enqueued AND even committed by another helper.",
                    commitRecord.transactionNumber, this.commitRequest.getId());
            return;
        }

        if (lastCheck.trySetNext(commitRecord)) {
            logger.debug("Enqueued record for valid transaction {} of commit request {}", commitRecord.transactionNumber,
                    this.commitRequest.getId());
        } else {
            logger.debug("Transaction {} of commit request {} was already enqueued by another helper.",
                    commitRecord.transactionNumber, this.commitRequest.getId());
        }
    }

    @Override
    public WriteSet makeWriteSet() {
        String msg = "Making a writeset is not a thread-safe operation. It was already done safely when creating this instance.";
        logger.error(msg);
        throw new UnsupportedOperationException(msg);
    }

    /* The commitTxRecord can only be set once */
    @Override
    public void setCommitTxRecord(ActiveTransactionsRecord record) {
        if (UNSAFE.compareAndSwapObject(this.decoratedTransaction, this.commitTxRecordOffset, null, record)) {
            logger.debug("set commitTxRecord with version {}", record.transactionNumber);
        } else {
            logger.debug("commitTxRecord was already set with version {}",
                    this.decoratedTransaction.getCommitTxRecord().transactionNumber);
        }

    }

    @Override
    public ActiveTransactionsRecord getCommitTxRecord() {
        return this.decoratedTransaction.getCommitTxRecord();
    }

    @Override
    protected void upgradeTx(ActiveTransactionsRecord newRecord) {
        // no op.  
        /* This is not a required step in this type of transaction.  The
        corresponding LockFreeTransaction will do this on its own
        node, after all the helping is done. */
    }
}
