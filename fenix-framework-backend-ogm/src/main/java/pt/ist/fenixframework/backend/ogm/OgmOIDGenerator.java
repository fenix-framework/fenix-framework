package pt.ist.fenixframework.backend.ogm;

import java.io.Serializable;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import pt.ist.fenixframework.DomainRoot;

public class OgmOIDGenerator implements IdentifierGenerator {

    public Serializable generate(SessionImplementor sessionImplementor, Object object)
        throws HibernateException {
        if (object.getClass() == DomainRoot.class) {
            return OgmOID.ROOT_PK;
        }
        return UUID.randomUUID().toString();

        // String uuid = UUID.randomUUID().toString();
        // return new OgmOID(object.getClass(), uuid);
        // return null;
    }
}
