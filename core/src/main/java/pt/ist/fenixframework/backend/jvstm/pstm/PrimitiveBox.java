package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;

class PrimitiveBox<E> extends OwnedVBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(PrimitiveBox.class);

    PrimitiveBox(JVSTMDomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    PrimitiveBox(JVSTMDomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(ownerObj, slotName, body);
    }

    // when a box needs reloading it's because the required value was NOT_LOADED_VALUE and thus the responsibility of this
    // method is to ensure that the box gets properly loaded
    @Override
    protected void doReload(/*Object obj, String attr*/) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reload PrimitiveVBox: slot {} for id {}", this.slotName, this.ownerObj.getExternalId());
        }

        JVSTMBackEnd.getInstance().getRepository().reloadPrimitiveAttribute(this);
    }

}
