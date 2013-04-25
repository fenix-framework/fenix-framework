package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;

// This class is currently only used for VBoxes that hold collections
// represented using DomainBasedMaps (versioned collections). Maybe CollectionBox
// would be a more appropriate name.
class ReferenceBox<E> extends VBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(PrimitiveBox.class);

    ReferenceBox(DomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    ReferenceBox(DomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(ownerObj, slotName, body);
    }

    public ReferenceBox(DomainObject ownerObj, String slotName, E value) {
        super(ownerObj, slotName, value);
    }

    @Override
    protected void doReload(/*Object obj, String attr*/) {
        if (logger.isDebugEnabled()) {
            logger.debug("Reload ReferenceVBox: {} for {}", this.slotName, this.ownerObj.getExternalId());
        }

        JVSTMBackEnd.getInstance().getRepository().reloadReferenceAttribute(this);
    }
}
