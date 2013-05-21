/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

import com.hazelcast.nio.DataSerializable;

public class RemoteCommit implements DataSerializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(RemoteCommit.class);

    private int serverId;
    private int txNumber;

    // these two arrays have a matching oid:slotName pair in each position 
    private long[] oids;
    private String[] slotNames;

    public RemoteCommit() {
        // required by Hazelcast's DataSerializable
    }

    public RemoteCommit(int serverId, int txNumber, Map<jvstm.VBox, Object> boxesWritten) {
        this.serverId = serverId;
        this.txNumber = txNumber;

        int writeSetSize = boxesWritten.size();
        this.oids = new long[writeSetSize];
        this.slotNames = new String[writeSetSize];

        // construir isto fora do lock e tb o byte array?
        int pos = 0;
        for (Map.Entry<jvstm.VBox, Object> entry : boxesWritten.entrySet()) {
            VBox<?> vbox = (VBox<?>) entry.getKey();
            slotNames[pos] = vbox.getSlotName();
            oids[pos++] = ((JVSTMDomainObject) vbox.getOwnerObject()).getOid();
        }
    }

    public int getServerId() {
        return this.serverId;
    }

    public int getTxNumber() {
        return this.txNumber;
    }

    public long[] getOids() {
        return this.oids;
    }

    public String[] getSlotNames() {
        return this.slotNames;
    }

    @Override
    public void writeData(DataOutput out) throws IOException {
        out.writeInt(this.serverId);
        out.writeInt(this.txNumber);

        int size = this.oids.length;
        out.writeInt(size);

        int commitSize = 4 * 3; // debug

        for (int i = 0; i < size; i++) {
            out.writeLong(oids[i]);
            out.writeUTF(slotNames[i]);

            commitSize += 8 + slotNames[i].length(); // debug: UTF-8 simplification but good enough to get a debug figure
        }

        logger.debug("RemoteCommit approximate size: {} bytes", commitSize);
    }

    @Override
    public void readData(DataInput in) throws IOException {
        this.serverId = in.readInt();
        this.txNumber = in.readInt();

        int size = in.readInt();
        this.oids = new long[size];
        this.slotNames = new String[size];
        for (int i = 0; i < size; i++) {
            this.oids[i] = in.readLong();
            this.slotNames[i] = in.readUTF();
        }
    }

}
