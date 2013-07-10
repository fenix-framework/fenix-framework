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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleWriteSet {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWriteSet.class);

    private static final String INVALID_WRITE_SET = "when provided, values must be the same length as vboxIds";

    private final String[] vboxIds;
    private final Object[] values;

    public SimpleWriteSet(String[] vboxIds) {
        this(vboxIds, null);
    }

    public SimpleWriteSet(String[] vboxIds, Object[] values) {
        if (values != null && (vboxIds.length != values.length)) {
            logger.error(INVALID_WRITE_SET);
            throw new IllegalArgumentException(INVALID_WRITE_SET);
        }
        this.vboxIds = vboxIds;
        this.values = values;
    }

    public String[] getVboxIds() {
        return this.vboxIds;
    }

    public Object[] getValues() {
        return this.values;
    }

    public int getNumElements() {
        return this.vboxIds.length;
    }

    public void writeTo(DataOutput out) throws IOException {
        out.writeInt(this.vboxIds.length);
        for (String vboxId : this.vboxIds) {
            out.writeUTF(vboxId);

            // The values are written to the repository before broadcasting the remote commit
//            byte[] externalValue = Externalization.externalizeObject(this.values[i]);
//            out.writeInt(externalValue.length);
//            out.write(externalValue);
        }
    }

    public static SimpleWriteSet readFrom(DataInput in) throws IOException {
        int size = in.readInt();
        String ids[] = new String[size];
//        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            ids[i] = in.readUTF();

//            int valueSize = in.readInt();
//            byte[] externalValue = new byte[valueSize];
//            in.readFully(externalValue);
//            values[i] = Externalization.internalizeObject(externalValue);
        }
//        return new SimpleWriteSet(ids,/* null,*/size);
        return new SimpleWriteSet(ids);
    }

    @Override
    public String toString() {
        int size = this.vboxIds.length;
        StringBuilder str = new StringBuilder();

        str.append("length=").append(size);
        str.append(", vboxIds={");
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                str.append(", ");
            }
            str.append(this.vboxIds[i]);
        }
        str.append("}");

        if (this.values != null) {
            str.append(", values={");
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    str.append(", ");
                }
                str.append(this.values[i]);
            }
            str.append("}");
        }

        return str.toString();
    }
}
