package pt.ist.fenixframework.backend.infinispan;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.UUID;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.exception.MissingObjectException;

/**
 *  This class provides the internal representation of an DomainObject's identifier in Infinispan.
 */
class OID implements Comparable<OID>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(OID.class);

    private static final String OID_SEPARATOR = "@";
    private static final String EXTERNAL_ID_ERROR = "Could not process externalId: ";

    static final OID ROOT_OBJECT_ID = new OID(DomainRoot.class, "ROOT_OBJECT");

    private final Class objClass;
    private final String objId;

    OID(Class objClass, String objId) {
        this.objClass = objClass;
        this.objId = objId;
    }

    OID(String externalId) {
        String[] tokens = externalId.split(OID_SEPARATOR);
        try {
            this.objClass = Class.forName(tokens[0]);
            this.objId = tokens[1];
        } catch (Exception e) {
            // e.g. index out of bounds, class not found
            logger.error(EXTERNAL_ID_ERROR + externalId);
            throw new MissingObjectException(EXTERNAL_ID_ERROR + externalId, e);
        }
    }

    Class getObjClass() {
        return this.objClass;
    }

    String getObjId() {
        return this.objId;
    }

    String toExternalId() {
        return objClass.getName() + OID_SEPARATOR + objId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OID) {
            OID other = (OID)o;
            return (this.objClass.equals(other.objClass)
                    && this.objId.equals(other.objId));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return objClass.hashCode() + objId.hashCode();
    }

    @Override
    public int compareTo(OID other) {
        int classes = this.objClass.getName().compareTo(other.objClass.getName());
        if (classes != 0) {  // having different classes is enough to order them
            return classes;
        }
        return this.objId.compareTo(other.objId);
    }
}
