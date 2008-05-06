package pt.ist.fenixframework.pstm;

import jvstm.VBoxBody;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.fieldaccess.PersistentField;

class PrimitiveBox<E> extends VBox<E> {

    PrimitiveBox() {
        super();
    }

    PrimitiveBox(VBoxBody<E> body) {
	super(body);
    }

    protected void doReload(Object obj, String attr) {
	PersistenceBroker pb = Transaction.getOJBBroker();
	Identity oid = new Identity(obj, pb);
	ClassDescriptor cld = pb.getClassDescriptor(obj.getClass());
        pb.serviceJdbcAccess().materializeObject(cld, oid);
    }
}
