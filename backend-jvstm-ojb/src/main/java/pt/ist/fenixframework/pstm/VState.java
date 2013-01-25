package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;
import pt.ist.fenixframework.DomainObject;

class VState extends PrimitiveBox {

    VState(DomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    VState(DomainObject ownerObj, String slotName, VBoxBody body) {
	super(ownerObj, slotName, body);
    }

    public static VState makeNew(DomainObject ownerObj, String slotName, boolean allocateOnly) {
        if (allocateOnly) {
            // when a box is allocated, it is safe 
            // to say that the version number is 0
            return new VState(ownerObj, slotName, makeNewBody(VBox.notLoadedValue(), 0, null));
        } else {
            return new VState(ownerObj, slotName);
        }
    }

    @Override
    public VBoxBody commit(Object newValue, int txNumber) {
        if (newValue != NOT_LOADED_VALUE) {
            ((OneBoxDomainObject.DO_State)newValue).markCommitted();
        }
        return super.commit(newValue, txNumber);
    }
}
