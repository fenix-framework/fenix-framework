package pt.ist.fenixframework.core.dml.runtime;

public class ConsistencyChecks {
    public static void checkRequired(Object obj, String slotname, Object slotvalue) {
        if (slotvalue == null) {
            throw new RequiredSlotException(obj.getClass(), slotname);
        }
    }
}
