package pt.ist.fenixframework.pstm.ojb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.PersistenceBrokerSQLException;
import org.apache.ojb.broker.accesslayer.JdbcAccessImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.util.logging.Logger;

import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.Transaction;

public class FenixJdbcAccessImpl extends JdbcAccessImpl {

    public FenixJdbcAccessImpl(PersistenceBroker broker) {
        super(broker);
    }

    // copied and adapted from the superclass
    public Object materializeObject(ClassDescriptor cld, Identity oid) throws PersistenceBrokerException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try
        {
            stmt = broker.serviceStatementManager().getSelectByPKStatement(cld);
            if (stmt == null)
            {
                if (logger.isEnabledFor(Logger.ERROR)) {
                    logger.error("getSelectByPKStatement returned a null statement");
                }
                throw new PersistenceBrokerException("getSelectByPKStatement returned a null statement");
            }
            broker.serviceStatementManager().bindSelect(stmt, oid, cld);
            rs = stmt.executeQuery();
            // data available read object, else return null
            if (rs.next())
            {
                AbstractDomainObject materializedObject = readObjectFromRs(rs);
                if (materializedObject != null) {
                    return materializedObject;
                }

                // If we got here, then it's because it is not a
                // domain object, but an OJB specific object.  So,
                // fallback to the OJB's default loading mechanism
                java.util.Map row = new java.util.HashMap();
                cld.getRowReader().readObjectArrayFrom(rs, row);
                return cld.getRowReader().readObjectFrom(row);
            }
            else
            {
                return null;
            }
        }
        catch (PersistenceBrokerException e)
        {
            if (logger.isEnabledFor(Logger.ERROR)) {
                logger.error(
                        "PersistenceBrokerException during the execution of materializeObject: "
                        + e.getMessage(),
                        e);
            }
            throw e;
        }
        catch (SQLException e)
        {
            if (logger.isEnabledFor(Logger.ERROR)) {
                logger.error(
                        "SQLException during the execution of materializeObject (for a "
                        + cld.getClassOfObject().getName()
                        + "): "
                        + e.getMessage(),
                        e);
            }
            throw new PersistenceBrokerSQLException(e);
        }
        finally
        {
            broker.serviceStatementManager().closeResources(stmt, rs);
        }
    }

    public static AbstractDomainObject readObjectFromRs(ResultSet rs) {
        // this method tries to load the object from its OID
        // see whether the OID column in the resultSet is already filled
        try {
            long objectOid = rs.getLong("OID");
            if (objectOid != 0) {
                // if it is, then we may get the object by its OID and skip all the old stuff
                AbstractDomainObject materializedObject = AbstractDomainObject.fromOID(objectOid);
                materializedObject.readFromResultSet(rs);
                return materializedObject;
            }
        } catch (SQLException sqle) {
            // we may have an SQLException if, for example, there is no OID column
            // in that case, simply ignore this and continue with the old code
            sqle.printStackTrace();
        }

        // null means that something failed
        return null;
    }
}
