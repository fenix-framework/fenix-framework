package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.core.SharedIdentityMap;

/**
 * A VBox that is onwed by some DomainObject. It extends VBox with an owner and a slotName. The box's id is composed by both
 * these attributes.
 */
public abstract class OwnedVBox<E> extends VBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(OwnedVBox.class);

    //initialized in the constructor
    protected final JVSTMDomainObject ownerObj;
    protected final String slotName;

    /* 
     This field is only to avoid computing this vbox's id on every request.
     No synchronization required.  The computation of this value always returns
     the same. It cannot be set construction of the VBox, because then the oid
     of the owner if 0.
     */
    private String id;

    public OwnedVBox(JVSTMDomainObject ownerObj, String slotName) {
        super();
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    public OwnedVBox(JVSTMDomainObject ownerObj, String slotName, E initial) {
        super(initial);
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    protected OwnedVBox(JVSTMDomainObject ownerObj, String slotName, VBoxBody<E> body) {
        super(body);
        this.ownerObj = ownerObj;
        this.slotName = slotName;
    }

    private static String makeId(String slotName, JVSTMDomainObject ownerObj) {
        return slotName + ":" + ownerObj.getExternalId();
    }

    /**
     * Get the vbox with the given id from the cache.
     * 
     * @param vboxId
     * @return The OwnedVBox if it is cached or <code>null</code> when either (1) the vboxId is not a valid {@link OwnedVBox} id
     *         or (2) the owner is not cached.
     */
    public static OwnedVBox lookupCachedVBox(String vboxId) {
        return tryGet(vboxId, true);
    }

    /**
     * Get the vbox with the given Id. It fails if the id is not a valid {@link OwnedVBox} id. If needed, if may allocate the
     * owner.
     * 
     * @param vboxId
     * @return The {@link OwnedVBox} or <code>null</code> if the Id is not valid
     */
    public static OwnedVBox fromId(String vboxId) {
        return tryGet(vboxId, false);
    }

    /**
     * Try to obtain an OwnedVBox if the given id is valid.
     * 
     * @param vboxId The vboxId to try
     * @param lookupOnly When the id is valid, whether to return the vbox only if the owner is cached
     * @return An {@link OwnedVBox} if the vboxId corresponds to a valid OwnedVBox id or <code>null</code> otherwise.
     */
    private static OwnedVBox tryGet(String vboxId, boolean lookupOnly) {
        String[] tokens = vboxId.split(":");

        if (tokens.length != 2) {
            return null;
        }

        String slotName = tokens[0];
        long oid;
        try {
            oid = Long.parseLong(tokens[1], 16);
        } catch (NumberFormatException e) {
            return null;
        }

        JVSTMDomainObject obj = null;

        if (lookupOnly) {
            obj = (JVSTMDomainObject) SharedIdentityMap.getCache().lookup(oid);
        } else {
            try {
                obj = JVSTMBackEnd.getInstance().fromOid(oid);
            } catch (Exception e) {
                // empty
                // e.g. oid was not valid after all
            }
        }

        // vbox is only available if the object was obtained (either was cache or correctly allocated 
        if (obj == null) {
            return null;
        }
        return (OwnedVBox) obj.getSlotNamed(slotName);
    }

    @Override
    public String getId() {
        String id = this.id;
        if (id == null) {
            id = this.id = makeId(slotName, ownerObj);
        }
        return id;
    }

    public JVSTMDomainObject getOwnerObject() {
        return this.ownerObj;
    }

    public String getSlotName() {
        return this.slotName;
    }

    public static <T> OwnedVBox<T> makeNew(JVSTMDomainObject ownerObj, String slotName, boolean allocateOnly, boolean isReference) {
        if (isReference) {
            if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
                return new ReferenceBox<T>(ownerObj, slotName, NOT_LOADED_BODY);
            } else {
                return new ReferenceBox<T>(ownerObj, slotName);
            }
        } else {
            if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
                return new PrimitiveBox<T>(ownerObj, slotName, NOT_LOADED_BODY);
            } else {
                return new PrimitiveBox<T>(ownerObj, slotName);
            }
        }
    }

}
