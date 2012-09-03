package pt.ist.fenixframework.core;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObject;

/**
 * This class contains useful code, required by concrete {@link DomainObject}s.  Backend
 * implementations may benefit from the code in this class when proving their own implementations of
 * DomainObject.
 */
public class AbstractDomainObjectAdapter extends AbstractDomainObject {
    private static final Logger logger = Logger.getLogger(AbstractDomainObjectAdapter.class);

    // must be here because of the required constructor of the allocate instance protocol
    protected AbstractDomainObjectAdapter() { }

    // must be here to enable climbing throught the hierarchy of constructors using the special
    // allocate instance constructor only
    protected AbstractDomainObjectAdapter(DomainObjectAllocator.OID oid) {
        super(oid);
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

