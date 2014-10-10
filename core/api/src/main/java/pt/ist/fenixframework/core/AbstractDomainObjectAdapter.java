package pt.ist.fenixframework.core;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;
import pt.ist.fenixframework.dml.DomainModel;

/**
 * This class contains useful code, required by concrete {@link DomainObject}s. Backend
 * implementations may benefit from the code in this class when providing their own implementations
 * of DomainObject.
 */
public class AbstractDomainObjectAdapter extends AbstractDomainObject {

    protected AbstractDomainObjectAdapter() {
    }

    protected AbstractDomainObjectAdapter(DomainObjectAllocator.OID oid) {
        super(oid);
    }

    /**
     * Invokes all the registered {@link DeletionListener}s for this type.
     * 
     * <p>
     * This method (or an equivalent one) <strong>MUST</strong> be invoked by backends that support object deletion.
     * </p>
     */
    protected final void invokeDeletionListeners() {
        for (DeletionListener<DomainObject> listener : getDomainModel().getDeletionListenersForType(getClass())) {
            listener.deleting(this);
        }
    }

    // serialization code

    @Override
    protected SerializedForm makeSerializedForm() {
        return new SerializedForm(this);
    }

    protected static class SerializedForm extends AbstractDomainObject.SerializedForm {
        private static final long serialVersionUID = 1L;

        private SerializedForm(AbstractDomainObject obj) {
            super(obj);
        }

        @Override
        protected DomainObject fromExternalId(String externalId) {
            return FenixFramework.getDomainObject(externalId);
        }
    }

    @Override
    protected DomainModel getDomainModel() {
        return FenixFramework.getDomainModel();
    }

}
