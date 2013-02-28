package pt.ist.fenixframework.backend.jvstmojb.pstm;

import java.util.HashMap;
import java.util.Map;

import jvstm.VBox;
import jvstm.VBoxBody;

public class ReadSet {
    private HashMap<VBox, VBoxBody> bodiesRead = new HashMap<VBox, VBoxBody>();

    ReadSet(Map<VBox, VBoxBody> bodiesRead) {
        this.bodiesRead.putAll(bodiesRead);
    }

    public void merge(ReadSet otherReadSet) {
        this.bodiesRead.putAll(otherReadSet.bodiesRead);
    }

    public boolean isStillCurrent() {
        for (Map.Entry<VBox, VBoxBody> entry : bodiesRead.entrySet()) {
            if (entry.getKey().body != entry.getValue()) {
                return false;
            }
        }
        return true;
    }
}
