package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.TransactionSignaller;
import jvstm.WriteSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest.ValidationStatus;
import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeBackEnd;
import pt.ist.fenixframework.backend.jvstm.lf.RemoteReadSet;

public class LocalLockFreeTransaction extends AbstractLockFreeTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LocalLockFreeTransaction.class);

    private final CommitRequest commitRequest;

    public LocalLockFreeTransaction(CommitRequest commitRequest, DistributedLockFreeTransaction tx) {
        super(tx.getActiveTxRecord());
        this.commitRequest = commitRequest;

    }

    @Override
    public int getNumber() {
        // smf: TODO check this
        /* using this.commitRequest.getTransaction().getNumber(); is safer, but
        only works for LocalLockFreeTransaction.  On the other hand. can getNumber()
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
            logger.debug("This commit request is already {}", (validationStatus == validationStatus.VALID ? "VALID" : "INVALID"));
            return;
        }
        super.validate();
    }

    @Override
    protected void snapshotValidation(int lastSeenCommittedTxNumber) {
        int myNumber = getNumber();

        if (lastSeenCommittedTxNumber == myNumber) {
            logger.debug("Commit request {} is VALID", this.commitRequest.getId());
            this.commitRequest.setValid();
            return;
        }

        RemoteReadSet readSet = this.commitRequest.getReadSet();

        // smf: TODO implement the helping mechanism here. For now, just iterate all.

        JvstmLockFreeBackEnd backend = JvstmLockFreeBackEnd.getInstance();

        for (String vboxId : readSet.getVBoxIds()) {
            VBox vbox = backend.lookupCachedVBox(vboxId);
            if (vbox == null) {
                // smf: TODO this vbox is not cached locally. deal with this latter
                logger.error("not implemented yet. must deal with uncached vboxes in this node. cannot continue to commit deterministically. exiting");
                System.exit(-1);
            } else {
                // check whether the read was valid
                if (vbox.body.version > myNumber) {
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
                    }
                }
            }
        }

        logger.debug("Commit request {} is VALID", this.commitRequest.getId());
        this.commitRequest.setValid();
    }

    @Override
    public WriteSet makeWriteSet() {
        return this.commitRequest.getTransaction().makeWriteSet();
    }

    @Override
    protected void enqueueValidCommit(ActiveTransactionsRecord lastCheck, WriteSet writeSet) {
        if (lastCheck.trySetNext(this.commitTxRecord)) {
            logger.debug("Enqueued record for valid transaction {} of commit request {}", this.commitTxRecord.transactionNumber,
                    this.commitRequest.getId());
        } else {
            logger.debug("Transaction {} of commit request {} was already enqueued by another helper.",
                    this.commitTxRecord.transactionNumber, this.commitRequest.getId());
        }

    }
}
