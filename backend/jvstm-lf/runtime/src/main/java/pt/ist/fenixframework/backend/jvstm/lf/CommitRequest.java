/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.hazelcast.nio.DataSerializable;

public class CommitRequest implements DataSerializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private int serverId;
    private RemoteReadSet readSet;
    private RemoteWriteSet writeSet;
    // The next commit request to process when they are queued.
    private final AtomicReference<CommitRequest> next = new AtomicReference<CommitRequest>();

    public CommitRequest() {
        // required by Hazelcast's DataSerializable
    }

    public CommitRequest(int serverId, RemoteReadSet readSet, RemoteWriteSet writeSet) {
        this.serverId = serverId;
        this.id = UUID.randomUUID();
        this.readSet = readSet;
        this.writeSet = writeSet;
    }

    public UUID getId() {
        return this.id;
    }

    public int getServerId() {
        return this.serverId;
    }

    public CommitRequest getNext() {
        return this.next.get();
    }

    public boolean setNext(CommitRequest next) {
        return this.next.compareAndSet(null, next);
    }

    @Override
    public void writeData(DataOutput out) throws IOException {
        out.writeInt(this.serverId);
        out.writeLong(this.id.getMostSignificantBits());
        out.writeLong(this.id.getLeastSignificantBits());
        this.readSet.writeTo(out);
        this.writeSet.writeTo(out);
    }

    @Override
    public void readData(DataInput in) throws IOException {
        this.serverId = in.readInt();
        this.id = new UUID(in.readLong(), in.readLong());
        this.readSet = RemoteReadSet.readFrom(in);
        this.writeSet = RemoteWriteSet.readFrom(in);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("id=").append(this.getId());
        str.append(", serverId=").append(this.getServerId());
        str.append(", readset={");
        str.append(this.readSet.toString());
        str.append("}, writeset={");
        str.append(this.writeSet.toString());
        str.append("}");
        return str.toString();
    }

}
