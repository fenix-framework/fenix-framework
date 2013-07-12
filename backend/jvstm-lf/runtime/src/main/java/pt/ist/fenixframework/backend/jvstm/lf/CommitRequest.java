/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import jvstm.CommitException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.CommitOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.LocalCommitOnlyTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.LockFreeTransaction;
import pt.ist.fenixframework.backend.jvstm.pstm.RemoteCommitOnlyTransaction;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class CommitRequest implements DataSerializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CommitRequest.class);

    public enum ValidationStatus {
        UNSET, VALID, INVALID;
    }

    /* for any transaction instance this will always change deterministically
    from UNSET to either VALID or INVALID, i.e. if concurrent helpers try to
    decide, they will conclude the same and the value will never revert back to
    UNSET. */
//    private volatile ValidationStatus validationStatus = ValidationStatus.UNSET; // AtomicReference?
    private final AtomicReference<ValidationStatus> validationStatus = new AtomicReference<ValidationStatus>(
            ValidationStatus.UNSET); // AtomicReference?

//    /* The sentinel has a null transaction attribute. It is only used to ensure
//    that there is a beginning to the commit requests queue */
    private static boolean sentinelRequestCreated = false;

    public static synchronized CommitRequest makeSentinelRequest() {
        if (sentinelRequestCreated) {
            throw new Error("CommitRequest::makeSentinelRequest() invoked more than once!");
        }
        sentinelRequestCreated = true;
        return new CommitRequest() {
            private static final long serialVersionUID = 2L;

            {
                this.id = new UUID(0, 0);
            }

            @Override
            public ValidationStatus getValidationStatus() {
                return ValidationStatus.VALID;
            };

            @Override
            public void internalHandle() {
                // no-op
            };

            @Override
            public String toString() {
                return "SENTINEL";
            };
        };
    }

    /**
     * A unique request ID.
     */
    protected UUID id;

    /**
     * The serverId from where the request originates.
     */
    private int serverId;

    /**
     * The current version of the transaction that creates this commit request.
     */
    private int txVersion;

    private SimpleReadSet readSet;
    private SimpleWriteSet writeSet;

    /* The following fields are set only by the receiver of the commit request. */

    // The next commit request to process in the queue.
    private final AtomicReference<CommitRequest> next = new AtomicReference<CommitRequest>();
    // The corresponding CommitOnlyTransaction
    private CommitOnlyTransaction transaction;

    public CommitRequest() {
        // required by Hazelcast's DataSerializable
    }

    public CommitRequest(int serverId, int txVersion, SimpleReadSet readSet, SimpleWriteSet writeSet) {
        this.id = UUID.randomUUID();
        this.serverId = serverId;
        this.txVersion = txVersion;
        this.readSet = readSet;
        this.writeSet = writeSet;
    }

    public UUID getId() {
        return this.id;
    }

    public int getServerId() {
        return this.serverId;
    }

    public int getTxVersion() {
        return this.txVersion;
    }

    public SimpleReadSet getReadSet() {
        return this.readSet;
    }

    public SimpleWriteSet getWriteSet() {
        return this.writeSet;
    }

    public CommitRequest getNext() {
        return this.next.get();
    }

    public boolean setNext(CommitRequest next) {
        return this.next.compareAndSet(null, next);
    }

    public CommitOnlyTransaction getTransaction() {
        return this.transaction;
    }

    /**
     * Set this request's {@link CommitOnlyTransaction}. It does so based on whether its a local or a remote commit. This
     * method must be called before making the CommitRequest visible to others: This way there is no race in the assignment of
     * this request's transaction.
     */
    public void assignTransaction() {
        LockFreeTransaction tx = CommitOnlyTransaction.commitsMap.remove(this.id);
        if (tx != null) {
            logger.debug("Assigning LocalCommitOnlyTransaction to CommitRequest: {}", this.id);
            this.transaction = new LocalCommitOnlyTransaction(this, tx);
        } else {
            logger.debug("Assigning new RemoteCommitOnlyTransaction to CommitRequest: {}", this.id);
            this.transaction = new RemoteCommitOnlyTransaction(this);
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(this.serverId);
        out.writeInt(this.txVersion);
        out.writeLong(this.id.getMostSignificantBits());
        out.writeLong(this.id.getLeastSignificantBits());
        this.readSet.writeTo(out);
        this.writeSet.writeTo(out);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        this.serverId = in.readInt();
        this.txVersion = in.readInt();
        this.id = new UUID(in.readLong(), in.readLong());
        this.readSet = SimpleReadSet.readFrom(in);
        this.writeSet = SimpleWriteSet.readFrom(in);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id=").append(this.getId());
        str.append(", txVersion=").append(this.getTxVersion());
        str.append(", serverId=").append(this.getServerId());
        str.append(", readset={");
        str.append(this.readSet.toString());
        str.append("}, writeset={");
        str.append(this.writeSet.toString());
        str.append("}");
        return str.toString();
    }

    /**
     * Handles a commit request. This method is responsible for all the operations required to commit this request. In the end,
     * the request will be marked either as committed or invalid. It may then be removed from the queue of commit requests. It
     * will not be removed from the queue if there is no 'next' request. This is to ensure the invariant: There is always at least
     * one request in the queue. This method never throws an exception. It catches all and always returns the next request to
     * process.
     * 
     * @return The next request to process, or <code>null</code> if there are no more records in line.
     */
    @SuppressWarnings("finally")
    public CommitRequest handle() {
        CommitRequest next = null;
        try {
            internalHandle();
        } catch (CommitException e) {
            logger.debug("Commit Request {} throw CommitException. Exception will be discarded.", this.getId());
        } catch (Throwable e) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Handling localCommit for request {} threw {}.  It will be obfuscated by the return of this method.",
                        this.getId(), e);
                e.printStackTrace();
            }
        } finally {
            next = advanceToNext();
            return next;
        }
    }

    protected void internalHandle() {
        this.getTransaction().localCommit();
    }

    public ValidationStatus getValidationStatus() {
//        return this.validationStatus;
        return this.validationStatus.get();
    }

    /**
     * Mark this commit request as invalid. More than one thread may attempt to set this status. Nevertheless, all threads that
     * attempt it will have the same opinion.
     */
    public void setInvalid() {
//        this.validationStatus = ValidationStatus.INVALID;
        ValidationStatus previous = this.validationStatus.getAndSet(ValidationStatus.INVALID);
        if (previous == ValidationStatus.VALID) {
            String msg = "This is a bug! Validation status must be deterministic!";
            logger.error("msg");
            System.exit(-1);
        }
    }

    /**
     * Mark this commit request as valid. More than one thread may attempt to set this status. Nevertheless, all threads that
     * attempt it will have the same opinion.
     */
    public void setValid() {
//        this.validationStatus = ValidationStatus.VALID;
        ValidationStatus previous = this.validationStatus.getAndSet(ValidationStatus.VALID);
        if (previous == ValidationStatus.INVALID) {
            String msg = "This is a bug! Validation status must be deterministic!";
            logger.error("msg");
            System.exit(-1);
        }
    }

    /**
     * Attempts to remove the current commit request from the head of the queue. It always returns the commit request following
     * this one, which may be <code>null</code> if there is no next request.
     * 
     * @return The commit request following this one (may be <code>null</code> if there is no next request).
     */
    private CommitRequest advanceToNext() {
        /* If we were to return the result of the following method we could skip
        over some commits, in particular, the one we're interested in committing.
        */
        LockFreeClusterUtils.tryToRemoveCommitRequest(this);

        // Always return the commit that really follows
        return this.getNext();
    }

}
