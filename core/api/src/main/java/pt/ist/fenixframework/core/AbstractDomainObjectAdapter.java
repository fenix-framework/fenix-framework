package pt.ist.fenixframework.core;

import static pt.ist.fenixframework.FenixFramework.getDomainModel;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.dml.DeletionListener;
import pt.ist.fenixframework.dml.DeletionListener.DeletionAdapter;

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
     * {@inheritDoc}
     * 
     * <p>
     * By default, checks with registered {@link DeletionAdapter}s if it can be safely deleted.
     * </p>
     */
    @Override
    protected boolean canBeDeleted() {
        for (DeletionListener<DomainObject> listener : getDomainModel().getDeletionListenersForType(getClass())) {
            if (listener instanceof DeletionAdapter && !((DeletionAdapter<DomainObject>) listener).canBeDeleted(this)) {
                return false;
            }
        }
        return true;
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

}
