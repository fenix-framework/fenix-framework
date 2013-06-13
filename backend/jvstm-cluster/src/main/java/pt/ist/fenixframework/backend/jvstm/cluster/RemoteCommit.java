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
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hazelcast.nio.DataSerializable;

public class RemoteCommit implements DataSerializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(RemoteCommit.class);

    protected int serverId;
    protected int txNumber;

    // these two arrays have a matching oid:slotName pair in each position 
    protected long[] oids;
    protected String[] slotNames;

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

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("serverId=").append(getServerId());
        str.append(", txNumber=").append(getTxNumber());
        str.append(", changes={");
        int size = getOids().length;
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                str.append(", ");
            }
            long oid = getOids()[i];
            String slotName = getSlotNames()[i];
            str.append('(').append(Long.toHexString(oid)).append(':').append(slotName).append(')');
        }
        str.append("}");
        return str.toString();
    }

    public static class SpeculativeRemoteCommit extends RemoteCommit {
        private static final long serialVersionUID = 1L;

        /**
         * The {@link JsonParser} to be used. Because its instances are
         * stateless we can reuse the parser.
         */
        private static final JsonParser parser = new JsonParser();

        // prepared data to send so that during commit lock, things can go faster
        protected String commitData;

        public SpeculativeRemoteCommit() {
            // required by Hazelcast's DataSerializable
        }

        // makes a speculative RemoteCommit (doesn't yet know the transaction number). Advantage: can be performed outside the locked code :-)
        public SpeculativeRemoteCommit(int serverId, Map<jvstm.VBox, Object> boxesWritten) {
            super();

            // get everything ready to 'just' send
            this.serverId = serverId;

            JsonArray array = new JsonArray();
            int size = 0;
            for (Map.Entry<jvstm.VBox, Object> entry : boxesWritten.entrySet()) {
                VBox<?> vbox = (VBox<?>) entry.getKey();
                array.add(new JsonPrimitive(vbox.getSlotName()));
                array.add(new JsonPrimitive(((JVSTMDomainObject) vbox.getOwnerObject()).getOid()));
                size++;
            }

            JsonObject topLevel = new JsonObject();
            topLevel.add("size", new JsonPrimitive(size));
            topLevel.add("data", array);

            this.commitData = topLevel.toString();
        }

        public void setTxNumber(int txNumber) {
            this.txNumber = txNumber;
        }

        @Override
        public void writeData(DataOutput out) throws IOException {
            out.writeInt(this.serverId);
            out.writeInt(this.txNumber);
            out.writeUTF(this.commitData);
        }

        @Override
        public void readData(DataInput in) throws IOException {
            this.serverId = in.readInt();
            this.txNumber = in.readInt();
            String commitData = in.readUTF();

            JsonObject topLevel = parser.parse(commitData).getAsJsonObject();

            int size = topLevel.get("size").getAsInt();
            this.slotNames = new String[size];
            this.oids = new long[size];

            Iterator<JsonElement> it = topLevel.get("data").getAsJsonArray().iterator();
            for (int i = 0; i < size; i++) {

                this.slotNames[i] = it.next().getAsString();
                this.oids[i] = it.next().getAsLong();
            }
        }

        @Override
        public String toString() {
            // if this is remote commit was received then the oids array is set.  Otherwise, we'll print the JSON array
            if (getOids() != null) {
                return super.toString();
            } else {
                StringBuilder str = new StringBuilder();
                str.append("serverId=").append(getServerId());
                str.append(", txNumber=").append(getTxNumber());
                str.append(", changes=");
                str.append(commitData);
                return str.toString();
            }
        }
    }

}
