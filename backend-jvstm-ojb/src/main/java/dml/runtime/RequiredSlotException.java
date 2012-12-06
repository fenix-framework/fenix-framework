package dml.runtime;

import jvstm.cps.ConsistencyException;

public class RequiredSlotException extends ConsistencyException {
    private Class objClass;
    private String slotname;

    public RequiredSlotException(Class objClass, String slotname) {
        this.objClass = objClass;
        this.slotname = slotname;
    }

    public Class getObjectClass() {
        return objClass;
    }

    public String getSlotname() {
        return slotname;
    }
}
