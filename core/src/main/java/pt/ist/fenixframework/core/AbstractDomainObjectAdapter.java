package pt.ist.fenixframework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import eu.cloudtm.LocalityHints;

/**
 * This class contains useful code, required by concrete {@link DomainObject}s. Backend
 * implementations may benefit from the code in this class when providing their own implementations
 * of DomainObject.
 */
public class AbstractDomainObjectAdapter extends AbstractDomainObject {

    protected AbstractDomainObjectAdapter() {
        this((LocalityHints) null);
    }

    protected AbstractDomainObjectAdapter(DomainObjectAllocator.OID oid) {
        super(oid);
    }

    // serialization code

    public AbstractDomainObjectAdapter(LocalityHints hints) {
        super(hints);
    }

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
