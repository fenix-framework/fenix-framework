package pt.ist.fenixframework.backend.jvstmojb.pstm;

import jvstm.VBoxBody;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.metadata.ClassDescriptor;

import pt.ist.fenixframework.DomainObject;

class ReferenceBox<E> extends VBox<E> {

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
    protected void doReload(Object obj, String attr) {
        PersistenceBroker pb = TransactionSupport.getOJBBroker();
        try {
            pb.retrieveReference(obj, attr);
        } catch (PersistenceBrokerException pbe) {
            // the attr may be missing from the OJB mapping...
            ClassDescriptor cld = pb.getClassDescriptor(obj.getClass());
            if ((cld.getObjectReferenceDescriptorByName(attr) == null) && (cld.getCollectionDescriptorByName(attr) == null)) {
                // assume a null value...
                setFromOJB(obj, attr, null);
            } else {
                throw pbe;
            }
        }
    }
}
