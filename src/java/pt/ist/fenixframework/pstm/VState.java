package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

class VState extends PrimitiveBox<OneBoxDomainObject.DO_State> {

    VState() {
        super();
    }

    VState(VBoxBody<OneBoxDomainObject.DO_State> body) {
	super(body);
    }

    public static VState makeNew(boolean allocateOnly) {
        if (allocateOnly) {
            // when a box is allocated, it is safe 
            // to say that the version number is 0
            return new VState(makeNewBody(VBox.<OneBoxDomainObject.DO_State>notLoadedValue(), 0, null));
        } else {
            return new VState();
        }
    }

    @Override
    public VBoxBody commit(OneBoxDomainObject.DO_State newValue, int txNumber) {
        newValue.markCommitted();
        return super.commit(newValue, txNumber);
    }
}
