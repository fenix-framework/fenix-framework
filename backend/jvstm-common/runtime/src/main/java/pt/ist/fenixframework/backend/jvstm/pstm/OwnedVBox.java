package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static OwnedVBox lookupCachedVBox(String vboxId) {
        String[] tokens = vboxId.split(":");
        String slotName = tokens[0];
        long oid = Long.parseLong(tokens[1], 16);

        JVSTMDomainObject obj = (JVSTMDomainObject) SharedIdentityMap.getCache().lookup(oid);

        // vbox is only available if the object was cached
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
                return new ReferenceBox<T>(ownerObj, slotName, makeNewBody((T) NOT_LOADED_VALUE, 0, null));
            } else {
                return new ReferenceBox<T>(ownerObj, slotName);
            }
        } else {
            if (allocateOnly) {
                // when a box is allocated, it is safe 
                // to say that the version number is 0
                return new PrimitiveBox<T>(ownerObj, slotName, makeNewBody((T) NOT_LOADED_VALUE, 0, null));
            } else {
                return new PrimitiveBox<T>(ownerObj, slotName);
            }
        }
    }

}
