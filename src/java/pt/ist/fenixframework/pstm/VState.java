package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

class VState extends PrimitiveBox {

    VState() {
        super();
    }

    VState(VBoxBody body) {
	super(body);
    }

    public static VState makeNew(boolean allocateOnly) {
        if (allocateOnly) {
            // when a box is allocated, it is safe 
            // to say that the version number is 0
            return new VState(makeNewBody(VBox.notLoadedValue(), 0, null));
        } else {
            return new VState();
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
