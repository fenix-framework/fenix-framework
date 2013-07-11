package pt.ist.fenixframework.backend.jvstm.pstm;

import static jvstm.UtilUnsafe.UNSAFE;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jvstm.ActiveTransactionsRecord;
import jvstm.TopLevelTransaction;
import jvstm.Transaction;
import jvstm.TransactionSignaller;
import jvstm.UtilUnsafe;
import jvstm.WriteSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest;
import pt.ist.fenixframework.backend.jvstm.lf.CommitRequest.ValidationStatus;
import pt.ist.fenixframework.backend.jvstm.lf.JvstmLockFreeBackEnd;
import pt.ist.fenixframework.backend.jvstm.lf.SimpleReadSet;

//import jvstm.VBox;

public abstract class CommitOnlyTransaction extends TopLevelTransaction {

    private static final Logger logger = LoggerFactory.getLogger(CommitOnlyTransaction.class);

    private static final long commitTxRecordOffset = UtilUnsafe.objectFieldOffset(TopLevelTransaction.class, "commitTxRecord");

    /**
     * Maps all node local {@link LockFreeTransaction}s using their id as key. Whoever completes the processing is
     * required to remove the entry from this map, lest it grow indefinitely with the number of transactions.
     * 
     */
    public final static ConcurrentHashMap<UUID, LockFreeTransaction> commitsMap =
            new ConcurrentHashMap<UUID, LockFreeTransaction>();

    protected final CommitRequest commitRequest;

//    private final WriteSet writeSet = STUB_WRITE_SET;

    public static final ConcurrentHashMap<Integer, UUID> txVersionToCommitIdMap = new ConcurrentHashMap<Integer, UUID>();

//    private boolean readOnly = false;

//    // for statistics
//    protected int numBoxReads = 0;
//    protected int numBoxWrites = 0;

    public CommitOnlyTransaction(ActiveTransactionsRecord record, CommitRequest commitRequest) {
        super(record);
        this.commitRequest = commitRequest;
    }

    @Override
    public boolean isWriteTransaction() {
        return true;
    }

//    @Override
//    public void setReadOnly() {
//        this.readOnly = true;
//    }
//
//    @Override
//    public boolean txAllowsWrite() {
//        return !this.readOnly;
//    }
//
//    @Override
//    public <T> T getBoxValue(VBox<T> vbox) {
//        return super.getBoxValue(vbox);
//    }
//
//    @Override
//    public boolean isBoxValueLoaded(VBox vbox) {
//        // TODO Auto-generated method stub
//        throw new UnsupportedOperationException("not yet implemented");
//    }
//
//    @Override
//    public int getNumBoxReads() {
//        return numBoxReads;
//    }
//
//    @Override
//    public int getNumBoxWrites() {
//        return numBoxWrites;
//    }

//    @Override
    /**
     * This is the commit algorithm that each CommitOnlyTransaction performs on each node, regardless of whether it is a
     * {@link LocalCommitOnlyTransaction} or a {@link RemoteCommitOnlyTransaction}. Note that {@link LockFreeTransaction}s
     * are decorated by {@link CommitOnlyTransaction}s.
     */
    public void localCommit() {
        // save current
        Transaction savedTx = Transaction.current();
        // set current
        Transaction.current.set(this);
        try {
            // enact the commit
            this.commitTx(false); // smf TODO: double-check whether we want to mess with ActiveTxRecords, thread-locals, etc.  I guess 'false' is the way to go...
        } finally {
            // restore current
            Transaction.current.set(savedTx);
        }
    }

//    @Override
//    public int getNumber() {
//        return this.getUnderlyingTransaction().getNumber();
//    }

    /**
     * Get the concrete transaction that will be committed. For local commits this should be the local tx instance, help by the
     * LocalCommitOnlyTransaction. For remote commits this should be the RemoteCommitOnlyTransaction instance itself.
     */
    public abstract TopLevelTransaction getUnderlyingTransaction();

    @Override
    protected void assignCommitRecord(int txNumber, WriteSet writeSet) {
        // Must set the correct commit number **BEFORE** setting the valid status
        super.assignCommitRecord(txNumber, writeSet);
        this.commitRequest.setValid();
    }

    /* The validation for commit-only transactions follows the usual protocol:
    (1) helpCommitAll, (2) snapshotValidation, and (3) validateAndEnqueue.  The
    difference is that now others may also be helping with the validation. So:

    - initially, check if validation already completed.  This is also useful to
    avoid starting the validation when there is only one (already validated)
    entry in the commit requests queue, and we're just waiting until more requests
    arrive.  Actually, I think that doing this would skip enqueuing if needed,
    so we should be doing the normal validation and just skipping when we really
    see them done.  In short, if already valid just move along to enqueueing.

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

//        // some other helper may have already done the work for us
//        ValidationStatus validationStatus = this.commitRequest.getValidationStatus();
//        boolean alreadyValidated = validationStatus != ValidationStatus.UNSET;
//
//        if (alreadyValidated) {
//            logger.debug("Commit request {} is already {}", this.commitRequest.getId(),
//                    (validationStatus == ValidationStatus.VALID ? "VALID" : "INVALID"));
//            return;
//        }

        super.validate();
    }

    @Override
    protected void snapshotValidation(int lastSeenCommittedTxNumber) {
        /* Notice that someone may have already enqueued this tx, in which case
        I may happen to have seen it already committed! But in that case the
        validation status must have been set already! So, the following test is
        a double take: it is necessary for correctness (to avoid enqueueing the
        same record twice and to throw an exception if already invalid), but it
        may also bring in the lucky benefit of the validation status for this
        tx already being set (either valid or invalid).  */
        ValidationStatus validationStatus = this.commitRequest.getValidationStatus();
        boolean alreadyValidated = validationStatus != ValidationStatus.UNSET;

        if (alreadyValidated) {
            if (validationStatus == ValidationStatus.VALID) {
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
            assignCommitRecord(lastSeenCommittedTxNumber + 1, getWriteSet());
            return;
        }

        SimpleReadSet readSet = this.commitRequest.getReadSet();

        // smf: TODO implement the helping mechanism here. For now, just iterate all.

        JvstmLockFreeBackEnd backend = JvstmLockFreeBackEnd.getInstance();

        for (String vboxId : readSet.getVBoxIds()) {
            VBox vbox = backend.vboxFromId(vboxId);
//            if (vbox == null) {
//                // smf: TODO this vbox is not cached locally. deal with this later
//                logger.error("not implemented yet. must deal with uncached vboxes in this node. cannot continue to commit deterministically. exiting");
//                System.exit(-1);
//                /* We know that if this tx is valid then it will commit with
//                commitNumber=lastSeenCommittedTxNumber+1. So we need to check
//                if there is some version committed greater than this.getNumber()
//                and less than commitNumber.  If it exists, this transaction is
//                invalid. If it doesn't exists, we may continue validation.
//                However, we can speed up the conclusion bu also checking whether
//                there is a commit for commitNumber.  If so, then this transaction
//                is immediately valid or invalid depending on whether such
//                commitNumber's commitId is ours.  Question: do we need to reload
//                everything from the most recent down to the lastSeenCommittedTxNumber?
//                Probably yes to ensure the invariant of the reload. Then we only
//                need to check the most recent loaded version :-).
//
//                Another note: a simple reload here is not enough, because reloads
//                only load up to the mostRecentCommit seen by this node.  We need
//                to know whether THIS request may have been already committed by
//                another node!
//
//                NOTE: We never enter this branch of the if in a
//                LocalCommitOnlyTransaction, but it doesn't hurt to have this
//                code in the common CommitOnlyTransaction.
//
//                In summary: If I'm not mistaken, after asking for a reload of
//                version mostRecentCommitted (whatever that may be), I'll be able
//                to run (exactly?) the same code as the one that runs when the
//                box is loaded...  */
//                // it is enough to reload the most recent version in order to decide about validation
//                vbox.reload(lastSeenCommittedTxNumber);
//            } /*else {*/

            if (vbox.body.version == 0) {
                vbox.reload(lastSeenCommittedTxNumber);
            }

            // check whether the read was valid
            if (vbox.body.version > myReadVersion) {
                /* caution: it could be our own commit that we're seeing!
                But, in that case, validation must have finished! If validation
                hasn't finished, this implies that no more recent version
                could have been loaded and the version we're seeing is >
                myReadVersion but lower than my prospective commit version,
                which means I'm invalid.

                (true for local tx only?!)

                Can validation NOT have finished in this node, and what we
                see written is a box that another node already wrote.  No,
                because our reload only loads versions that may be necessary
                in this node.  By 'be necessary' I mean reload loads at
                highest, from the mostRecentlyCommitted version.  So, for a
                box version to be higher than my read version, either a local
                commit or a remote commit higher than my read version (but
                lower than mostRecentCommitted) must have already been
                processed in this node, which ultimately implies that THIS
                commit request needs to have its validation status already
                set (after all, it was ahead in the queue!)

                So, if this commit request's validation state is unset after
                we already have seen a body with a version > myReadVersion,
                this commit request must be invalid.
                */

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
//            }
        }

        logger.debug("Commit request {} is VALID", this.commitRequest.getId());
        assignCommitRecord(lastSeenCommittedTxNumber + 1, getWriteSet());
    }

    /**
     * Get the {@link WriteSet} for this transaction.
     * 
     */
    protected abstract WriteSet getWriteSet();

    @Override
    public abstract WriteSet makeWriteSet();

    @Override
    protected void validateCommitAndEnqueue(ActiveTransactionsRecord lastCheck) {
        enqueueValidCommit(lastCheck, this.getCommitTxRecord().getWriteSet());

        updateOrecVersion();
    }

//    @Override
//    public void updateOrecVersion() {
//        this.getUnderlyingTransaction().updateOrecVersion();
//    }

    @Override
    protected void enqueueValidCommit(ActiveTransactionsRecord lastCheck, WriteSet writeSet) {
        ActiveTransactionsRecord commitRecord = this.getCommitTxRecord();

        /* Here we know that our commit is valid.  However, we may have concluded
        such result via some helper AND even have seen already our record enqueued
        and committed. So we need to check for that to skip enqueuing. */
        if (lastCheck.transactionNumber >= commitRecord.transactionNumber) {
            logger.debug("Transaction {} for commit request {} was already enqueued AND even committed by another helper.",
                    commitRecord.transactionNumber, this.commitRequest.getId());
        } else {
            if (lastCheck.trySetNext(commitRecord)) {
                logger.debug("Enqueued record for valid transaction {} of commit request {}", commitRecord.transactionNumber,
                        this.commitRequest.getId());
            } else {
                logger.debug("Transaction {} of commit request {} was already enqueued by another helper.",
                        commitRecord.transactionNumber, this.commitRequest.getId());
            }
        }

        // EVERYONE MUST TRY THIS, to ensure visibility when looking it up ahead.
        txVersionToCommitIdMap.putIfAbsent(commitRecord.transactionNumber, this.commitRequest.getId());
    }

    /* The commitTxRecord can only be set once */
    @Override
    public void setCommitTxRecord(ActiveTransactionsRecord record) {
        if (UNSAFE.compareAndSwapObject(this.getUnderlyingTransaction(), this.commitTxRecordOffset, null, record)) {
            logger.debug("set commitTxRecord with version {}", record.transactionNumber);
        } else {
            logger.debug("commitTxRecord was already set with version {}", this.getCommitTxRecord().transactionNumber);
        }
    }

//    @Override
//    public ActiveTransactionsRecord getCommitTxRecord() {
//        return this.getUnderlyingTransaction().getCommitTxRecord();
//    }

    @Override
    protected void helpCommit(ActiveTransactionsRecord recordToCommit) {
        if (!recordToCommit.isCommitted()) {
            logger.debug("Helping to commit version {}", recordToCommit.transactionNumber);

            int txVersion = this.getCommitTxRecord().transactionNumber;
            UUID commitId = CommitOnlyTransaction.txVersionToCommitIdMap.get(txVersion);

            if (commitId != null) { // may be null if it was already persisted 
                JvstmLockFreeBackEnd.getInstance().getRepository().mapTxVersionToCommitId(txVersion, commitId);
                CommitOnlyTransaction.txVersionToCommitIdMap.remove(txVersion);
            }
            super.helpCommit(recordToCommit);
        } else {
            logger.debug("Version {} was already fully committed", recordToCommit.transactionNumber);
        }
    }

    @Override
    protected void upgradeTx(ActiveTransactionsRecord newRecord) {
        // no op.  
        /* This is not a required step in this type of transaction.  The
        corresponding LockFreeTransaction will do this on its own
        node, after all the helping is done. */
    }

}
