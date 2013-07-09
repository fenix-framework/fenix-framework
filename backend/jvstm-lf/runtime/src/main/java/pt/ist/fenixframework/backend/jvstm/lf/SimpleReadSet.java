package pt.ist.fenixframework.backend.jvstm.lf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SimpleReadSet {

    private final String[] vboxIds;

    public SimpleReadSet(String[] vboxIds) {
        this.vboxIds = vboxIds;
    }

    public String[] getVBoxIds() {
        return this.vboxIds;
    }

    public void writeTo(DataOutput out) throws IOException {
        // write number of ids
        out.writeInt(this.vboxIds.length);
        // write each id
        for (String s : this.vboxIds) {
            out.writeUTF(s);
        }
    }

    public static SimpleReadSet readFrom(DataInput in) throws IOException {
        int size = in.readInt();
        String ids[] = new String[size];
        for (int i = 0; i < size; i++) {
            ids[i] = in.readUTF();
        }
        return new SimpleReadSet(ids);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        int size = this.vboxIds.length;

        str.append("size=").append(size);
        str.append(", vboxIds={");

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                str.append(", ");
            }
            str.append(this.vboxIds[i]);
        }
        str.append("}");
        return str.toString();
    }

}