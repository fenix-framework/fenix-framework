package pt.ist.fenixframework.backend.jvstm.lf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class RemoteWriteSet {

    /* WARNING: Because of the algorithm used to create these arrays, the may
    be created larger than necessary. As such, their length is meaningless, and
    we should use the value of writeSetLength instead */
    private final String[] vboxIds;
//    private final Object[] values;
    private final int writeSetLength;

//    public RemoteWriteSet(String[] vboxIds, Object[] values, int writeSetLength) {
    public RemoteWriteSet(String[] vboxIds,/* Object[] values, */int writeSetLength) {
        this.vboxIds = vboxIds;
//        this.values = values;
        this.writeSetLength = writeSetLength;
    }

    public void writeTo(DataOutput out) throws IOException {
        out.writeInt(writeSetLength);
        for (int i = 0; i < writeSetLength; i++) {
            out.writeUTF(this.vboxIds[i]);

            // The values are written to the repository before broadcasting the remote commit
//            byte[] externalValue = Externalization.externalizeObject(this.values[i]);
//            out.writeInt(externalValue.length);
//            out.write(externalValue);
        }
    }

    public static RemoteWriteSet readFrom(DataInput in) throws IOException {
        int size = in.readInt();
        String ids[] = new String[size];
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            ids[i] = in.readUTF();

//            int valueSize = in.readInt();
//            byte[] externalValue = new byte[valueSize];
//            in.readFully(externalValue);
//            values[i] = Externalization.internalizeObject(externalValue);
        }
        return new RemoteWriteSet(ids,/* null,*/size);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("writeSetLength=").append(writeSetLength);
        str.append(", vboxIds={");
        for (int i = 0; i < writeSetLength; i++) {
            if (i != 0) {
                str.append(", ");
            }
            str.append(this.vboxIds[i]);
        }
//        str.append(", values={");
//        for (int i = 0; i < writeSetLength; i++) {
//            if (i != 0) {
//                str.append(", ");
//            }
//            str.append("(").append(this.values[i]).append(")");
//        }
        str.append("}");
        return str.toString();
    }
}
