package pt.ist.fenixframework.backend.ogm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.exception.MissingObjectException;

/**
 *  This class provides the internal representation of an DomainObject's identifier in OGM.
 */
public class OgmOID implements Comparable<OgmOID>, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OgmOID.class);

    private static final String OID_SEPARATOR = "@";
    private static final String EXTERNAL_ID_ERROR = "Could not process externalId: ";
    
    public static final String ROOT_PK = "ROOT_OBJECT";
    static final OgmOID ROOT_OBJECT_ID = new OgmOID(DomainRoot.class, ROOT_PK);
    // static final OgmOID ROOT_OBJECT_ID = new OgmOID(DomainRoot.class, 1L);

    private final Class objClass;
    private final String primaryKey;
    // private final Long primaryKey;

    OgmOID(Class objClass, String primaryKey) {
    // OgmOID(Class objClass, Long primaryKey) {
        this.objClass = objClass;
        this.primaryKey = primaryKey;
    }

    OgmOID(String externalId) {
        String[] tokens = externalId.split(OID_SEPARATOR);
        try {
            this.objClass = Class.forName(tokens[0]);
            this.primaryKey = tokens[1];
            // this.primaryKey = Long.valueOf(tokens[1]);
        } catch (Exception e) {
            // e.g. index out of bounds, class not found, etc.
            if (logger.isErrorEnabled()) {
                logger.error(EXTERNAL_ID_ERROR + externalId);
            }
            throw new MissingObjectException(EXTERNAL_ID_ERROR + externalId, e);
        }
    }

    Class getObjClass() {
        return this.objClass;
    }

    public String getPrimaryKey() {
    // public Long getPrimaryKey() {
        return this.primaryKey;
    }

    String toExternalId() {
        return objClass.getName() + OID_SEPARATOR + primaryKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof OgmOID) {
            OgmOID other = (OgmOID)o;
            return (this.objClass.equals(other.objClass)
                    && this.primaryKey.equals(other.primaryKey));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return objClass.hashCode() + primaryKey.hashCode();
    }

    @Override
    public int compareTo(OgmOID other) {
        int classes = this.objClass.getName().compareTo(other.objClass.getName());
        if (classes != 0) {  // having different classes is enough to order them
            return classes;
        }
        return this.primaryKey.compareTo(other.primaryKey);
    }

    @Override
    public String toString() {
        return toExternalId();
    }
}
