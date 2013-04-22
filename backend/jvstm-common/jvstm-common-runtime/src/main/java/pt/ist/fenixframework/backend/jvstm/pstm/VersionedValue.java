package pt.ist.fenixframework.backend.jvstm.pstm;

import java.util.Comparator;

public class VersionedValue {

    public static final Comparator<VersionedValue> COMPARE_VERSION_DESCENDING = new Comparator<VersionedValue>() {
        @Override
        public int compare(VersionedValue v1, VersionedValue v2) {
            return v2.version - v1.version;
        }
    };

    private final Object value;
    private final int version;

    public VersionedValue(Object value, int version) {
        this.value = value;
        this.version = version;
    }

    public Object getValue() {
        return this.value;
    }

    public int getVersion() {
        return this.version;
    }
}
