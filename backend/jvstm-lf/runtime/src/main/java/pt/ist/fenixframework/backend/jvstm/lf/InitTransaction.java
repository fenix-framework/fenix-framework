/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import java.util.UUID;

import jvstm.ActiveTransactionsRecord;
import jvstm.CommitException;
import jvstm.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.LockFreeTransaction;

public class InitTransaction extends LockFreeTransaction {

    private static final Logger logger = LoggerFactory.getLogger(LockFreeTransaction.class);

    /* used to store the most recent persisted version before the commit request
    is sent.  This way we later know that it's enough to lookup commit higher
    than this one to find out our own commit version.*/
    private int existingVersion;

    public InitTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    protected void upgradeWithPendingCommits(ActiveTransactionsRecord record) {
        // no-op
        /* we cannot help while initializing... :-) */
    }

    @Override
    protected void helpedTryCommit(CommitRequest myRequest) throws CommitException {
        this.existingVersion = JvstmLockFreeBackEnd.getInstance().getRepository().getMaxCommittedTxNumber();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.helpedTryCommit(myRequest);
    }

    @Override
    protected CommitRequest tryCommit(CommitRequest requestToProcess, UUID myRequestId) throws CommitException {
        // discard all commit request up to mine
        while (!requestToProcess.getId().equals(myRequestId)) {
            logger.debug("Ignoring commit request: {}", requestToProcess.getId());

            requestToProcess = LockFreeClusterUtils.tryToRemoveCommitRequest(requestToProcess);
        }

        /* wait until I see my request committed. It will necessarily have a
        number higher than the one committed before I sent my request, so no
        need to look to numbers lower than that */

        int versionToLookup = this.existingVersion + 1;
        String myRequestIdString = myRequestId.toString();
        String commitId;
        do {
            commitId = JvstmLockFreeBackEnd.getInstance().getRepository().getCommitIdFromVersion(versionToLookup);
            if (commitId != null) {
                // next try, if needed, will be for a higher version
                versionToLookup++;
            }
        } while (!myRequestIdString.equals(commitId));

        // perform the commit steps for this request so that others will see them

        // 1. validation and enqueue
        assignCommitRecord(versionToLookup - 1, makeWriteSet());
        requestToProcess.setValid();
        Transaction.mostRecentCommittedRecord.trySetNext(getCommitTxRecord());

        // 2. write back
        ensureCommitStatus();

        // 3. we don't need it in the queue anymore
        LockFreeClusterUtils.tryToRemoveCommitRequest(requestToProcess);

        return requestToProcess;

    }
}
