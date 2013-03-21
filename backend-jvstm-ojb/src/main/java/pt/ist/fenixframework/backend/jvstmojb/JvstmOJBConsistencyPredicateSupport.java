package pt.ist.fenixframework.backend.jvstmojb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import jvstm.cps.ConsistencyCheckTransaction;

import org.apache.ojb.broker.accesslayer.LookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.adt.bplustree.BPlusTree;
import pt.ist.fenixframework.backend.jvstmojb.pstm.AbstractDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.FenixConsistencyCheckTransaction;
import pt.ist.fenixframework.backend.jvstmojb.pstm.OneBoxDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TransactionSupport;
import pt.ist.fenixframework.backend.jvstmojb.repository.DbUtil;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicateSupport;
import pt.ist.fenixframework.consistencyPredicates.DomainDependenceRecord;

public class JvstmOJBConsistencyPredicateSupport extends ConsistencyPredicateSupport {

    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBConsistencyPredicateSupport.class);

    @Override
    public DomainMetaObject getDomainMetaObjectFor(DomainObject obj) {
        AbstractDomainObject domainObject = (AbstractDomainObject) obj;
        return domainObject.getDomainMetaObject();
    }

    @Override
    public void justSetMetaObjectForDomainObject(DomainObject domainObject, DomainMetaObject metaObject) {
        AbstractDomainObject abstractDO = (AbstractDomainObject) domainObject;
        abstractDO.justSetMetaObject(metaObject);
    }

    @Override
    public ConsistencyCheckTransaction<?> createNewConsistencyCheckTransactionForObject(DomainObject obj) {
        return new FenixConsistencyCheckTransaction(TransactionSupport.currentFenixTransaction(), obj);
    }

    /**
     * Uses a JDBC query to obtain the delete all the {@link DomainMetaObject}s of this class.
     * It also sets to null any OIDs that used to point to the deleted {@link DomainMetaObject}s,
     * both in the {@link DomainObject}'s and in the {@link DomainDependenceRecord}'s tables.
     */
    @Override
    public void removeAllMetaObjectsForMetaClass(DomainMetaClass domainMetaClass) {
        String tableName = getTableName(domainMetaClass);

        String metaObjectsToDeleteQuery =
                "select OID from DOMAIN_META_OBJECT where OID_DOMAIN_META_CLASS = '" + domainMetaClass.getExternalId() + "'";

        String clearDomainObjectsQuery =
                "update " + tableName + " set OID_DOMAIN_META_OBJECT = null " + "where OID_DOMAIN_META_OBJECT in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearDomainObjectsQuery);
        } catch (SQLException e) {
            logger.warn("The deleted DomainMetaClass " + domainMetaClass.getDomainClassName() + " had a table named " + tableName
                    + " that no longer exists.");
            e.printStackTrace();
        }

        String clearDependenceRecordsQuery =
                "delete from DOMAIN_DEPENDENCE_RECORD where OID_DEPENDENT_DOMAIN_META_OBJECT " + "in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearDependenceRecordsQuery);
        } catch (SQLException e) {
            throw new Error(e);
        }

        String clearIndirectionTable =
                "delete from DEPENDED_DOMAIN_META_OBJECTS_DEPENDING_DEPENDENCE_RECORDS" + " where OID_DOMAIN_META_OBJECT in ("
                        + metaObjectsToDeleteQuery + ")";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(clearIndirectionTable);
        } catch (SQLException e) {
            throw new Error(e);
        }

        // Delete the BPlusTree that linked to the existing DomainMetaObjects
        BPlusTree<DomainMetaObject> bPlusTree = domainMetaClass.getExistingDomainMetaObjects();
        bPlusTree.delete();
        domainMetaClass.removeExistingDomainMetaObjects();

        String deleteMetaObjectsQuery =
                "delete from DOMAIN_META_OBJECT where OID_DOMAIN_META_CLASS = '" + domainMetaClass.getExternalId() + "'";

        try {
            getCurrentJdbcConnection().createStatement().executeUpdate(deleteMetaObjectsQuery);
        } catch (SQLException e) {
            throw new Error(e);
        }
    }

    private String getTableName(DomainMetaClass domainMetaClass) {
        DomainMetaClass topMetaClass = domainMetaClass;
        while (topMetaClass.hasDomainMetaSuperclass()) {
            //skip to the next non-base superclass
            topMetaClass = topMetaClass.getDomainMetaSuperclass();
        }
        return DbUtil.convertToDBStyle(getSimpleClassName(topMetaClass.getDomainClassName()));
    }

    private String getSimpleClassName(String fullClassName) {
        String[] strings = fullClassName.split("\\.");
        return strings[strings.length - 1];
    }

    private static Connection getCurrentJdbcConnection() {
        try {
            return TransactionSupport.getOJBBroker().serviceConnectionManager().getConnection();
        } catch (LookupException e) {
            throw new Error(e);
        }
    }

    private static String getTableName(Class<? extends DomainObject> domainClass) {
        Class<?> topSuperClass = domainClass;
        while (!topSuperClass.getSuperclass().getSuperclass().equals(OneBoxDomainObject.class)) {
            //skip to the next non-base superclass
            topSuperClass = topSuperClass.getSuperclass().getSuperclass();
        }
        return DbUtil.convertToDBStyle(topSuperClass.getSimpleName());
    }

    private static final int MAX_NUMBER_OF_OBJECTS_TO_PROCESS = 10000;

    @Override
    public int getBatchSize() {
        return MAX_NUMBER_OF_OBJECTS_TO_PROCESS;
    }

    /**
     * Uses a JDBC query to obtain the OIDs of the existing {@link DomainObject}s of this class that do not yet have a
     * {@link DomainMetaObject}.<br>
     * <br>
     * This method only returns a maximum amount of OIDs, defined by the method getBatchSize().
     * 
     * @param domainClass
     *            the <code>Class</code> for which to obtain the existing
     *            objects OIDs
     * 
     * @return the <code>List</code> of <code>Strings</code> containing the OIDs
     *         of all the {@link DomainObject}s of the given class,
     *         without {@link DomainMetaObject}.
     */
    @Override
    public Collection<String> getIDsWithoutMetaObjectBatch(Class<? extends DomainObject> domainClass) {
        String tableName = getTableName(domainClass);
        String className = domainClass.getName();

        String metaObjectOidsQuery = "select OID from DOMAIN_META_OBJECT";

        String query =
                "select OID from " + tableName
                        + ", FF$DOMAIN_CLASS_INFO where OID >> 32 = DOMAIN_CLASS_ID and DOMAIN_CLASS_NAME = '" + className
                        + "' and (OID_DOMAIN_META_OBJECT is null or OID_DOMAIN_META_OBJECT not in (" + metaObjectOidsQuery
                        + ")) order by OID limit " + getBatchSize();

        ArrayList<String> oids = new ArrayList<String>();
        try {
            Statement statement = getCurrentJdbcConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                oids.add(resultSet.getString("OID"));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new Error(e);
        }

        return oids;
    }

}
