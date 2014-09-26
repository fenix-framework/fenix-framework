package pt.ist.fenixframework.core;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainObject;

/**
 * This is the top-level class for every DomainObject. Each backend should provide a subclass of
 * this class, with a backend-specific implementation of both {@link DomainObject#getExternalId()} and {@link #getOid()}. In this
 * class, they simply throw an {@link UnsupportedOperationException}. The method {@link DomainObject#getExternalId()} should be
 * used
 * by the user of the framework, whereas the method {@link #getOid()} should be used by code within
 * the framework. This allows for a more efficient implementation of the object's internal
 * identifier, other than the String type imposed on the external identifier.
 * 
 * Additionally, the subclass must also implement {@link #ensureOid()}, {@link #makeSerializedForm()}, and
 * {@link SerializedForm#fromExternalId(String)}. See their
 * documentation for further explanation.
 */
public abstract class AbstractDomainObject implements DomainObject {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDomainObject.class);

    @Override
    public String getExternalId() {
        throw new UnsupportedOperationException("getExternalId not implemented at this level");
    }

    /**
     * Returns an internal global representation of this object. Although, it can be implemented
     * simply as a call to {@link #getExternalId()}, this method enables code **within** the
     * framework to leverage on the knowledge of the concrete identifier, thus being more efficient.
     * <strong>This method should only be used by code internal to the framework</strong>.
     */
    public Comparable getOid() {
        throw new UnsupportedOperationException("getOid not implemented at this level");
    }

    /**
     * Default, no-arg constructor. Calls {@link #ensureOid()} to set the object's identifier.
     * Every {@link DomainObject} constructor (except for the special allocate-instance constructor)
     * should ensure that {@link #ensureOid()} is called once during object creation.
     * 
     * @see #ensureOid
     */
    protected AbstractDomainObject() {
        super();
        init$Instance(false);
        ensureOid();
    }

    /**
     * This constructor exists only as part of the allocate-instance protocol and should never be
     * explicitly invoked by the programmer. Each backend must implement this constructor, and
     * decide how the OID gets restored to the object. Note that the classes modelled in DML are
     * automatically injected with this constructor, which simply delegates the operation to their
     * superclass's constructor.
     * 
     * In this class, this constructor is empty. It is here just as a placeholder for this
     * documentation.
     */
    protected AbstractDomainObject(DomainObjectAllocator.OID oid) {
        init$Instance(true);
    }

    /**
     * Set the identifier (<code>oid</code>) of the object that is being created. This method is
     * invoked by the no-arg constructor. It is only intented to be invoked from within a
     * constructor (with the exception of {@link #AbstractDomainObject(DomainObjectAllocator.OID)},
     * which has its own way of restoring the object's id.
     */
    protected void ensureOid() {
        throw new UnsupportedOperationException("ensureOid not implemented at this level");
    }

    /**
     * Initialize this instance as needed.
     * 
     * @param allocateOnly <code>false</code> if this is a new instance. <code>true</code> otherwise (in which case the instance
     *            should only be allocated, not fully initialized).
     */
    protected void init$Instance(boolean allocateOnly) {
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    /**
     * Determines whether this object can safely be deleted, from a business-logic point of view.
     * Subclasses may override and use this method to properly determine the object's status.
     * 
     * @return
     *         Whether this object can safely be deleted.
     */
    protected List<String> getDeletionBlockers() {
        return Collections.emptyList();
    }

    protected void deleteDomainObject() {
        throw new UnsupportedOperationException("deleteDomainObject not implemented at this level");
    }

    // Serialization Code

    /* This method must be in the format:
     *
     * protected Object writeReplace() throws ObjectStreamException;
     *
     * to allow subclasses to have this writeReplace action when serialization occurs.
     */
    protected Object writeReplace() throws ObjectStreamException {
        if (logger.isTraceEnabled()) {
            logger.trace("Serializing " + this.getClass().getName() + ":" + this.getExternalId());
        }
        return makeSerializedForm();
    }

    /**
     * Creates the concrete instance of SerializedForm for this DomainObject. This method is
     * invoked when serialization of the DomainObject occurs. Final users of this framework should
     * never invoke this method explicitly. Backend developers should provide an implementation for
     * this method in their subclasses of AbstractDomainObject.
     * 
     * @return The corresponding SerializedForm
     */
    protected SerializedForm makeSerializedForm() {
        throw new UnsupportedOperationException("makeSerializedForm not implemented at this level");
    }

    protected static abstract class SerializedForm implements Serializable {
        private static final long serialVersionUID = 1L;

        // the external serialized form of any domain object only needs to keep its external ID
        private final String externalId;

        protected SerializedForm(AbstractDomainObject obj) {
            externalId = obj.getExternalId();
        }

        /* This method must be in the format:
         *
         * protected Object readResolve() throws ObjectStreamException;
         *
         * to allow subclasses to have this readResolve action when de-serialization occurs.
         */
        protected Object readResolve() throws ObjectStreamException {
            if (logger.isTraceEnabled()) {
                logger.trace("Deserializing " + this.externalId);
            }
            return fromExternalId(externalId);
        }

        /**
         * Returns a DomainObject given its externalId. This method is invoked when
         * de-serialization of a previously serialized DomainObject occurs.
         * 
         * Final users of this framework should not invoke this method explicitly. Backend
         * developers should provide an implementation for this method in their subclasses of
         * AbstractDomainObject.SerializedForm.
         * 
         * @param externalId The object's external identifier
         * @return The corresponding DomainObject
         */
        protected DomainObject fromExternalId(String externalId) {
            throw new UnsupportedOperationException("fromExternalID not implemented at this level");
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + ":" + getExternalId();
    }
}
