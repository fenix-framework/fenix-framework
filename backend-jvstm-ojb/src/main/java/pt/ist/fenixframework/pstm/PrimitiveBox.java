package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;

import pt.ist.fenixframework.DomainObject;

class PrimitiveBox<E> extends VBox<E> {

    PrimitiveBox(DomainObject ownerObj, String slotName) {
        super(ownerObj, slotName);
    }

    PrimitiveBox(DomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(ownerObj, slotName, body);
    }

    @Override
    protected void doReload(Object obj, String attr) {
        PersistenceBroker pb = TransactionSupport.getOJBBroker();
        Identity oid = new Identity(obj, pb);
        ClassDescriptor cld = pb.getClassDescriptor(obj.getClass());
        pb.serviceJdbcAccess().materializeObject(cld, oid);
    }
}
