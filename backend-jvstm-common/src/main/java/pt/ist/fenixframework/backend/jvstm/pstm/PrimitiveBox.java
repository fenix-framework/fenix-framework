package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;

class PrimitiveBox<E> extends VBox<E> {

    PrimitiveBox(DomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    PrimitiveBox(DomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(ownerObj, slotName, body);
    }

    // when a box needs reloading it's because the required value was NOT_LOADED_VALUE and thus the responsibility of this
    // method is to ensure that the box gets properly loaded
    @Override
    protected void doReload(/*Object obj, String attr*/) {
        JVSTMBackEnd.getInstance().getRepository().reloadPrimitiveAttribute(this);
    }

}
