package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.MetadataManager;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstmojb.JvstmOJBConfig;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.DomainRelation;
import pt.ist.fenixframework.dml.Role;

public class SQLUpdateGenerator {
    public static String generateSqlUpdates(final URL[] modelUrls, final String db, final String dbUser, final String dbPass,
            String charset, boolean genDrops) throws SQLException, LookupException {
        FenixFramework.initialize(new JvstmOJBConfig() {
            {
                domainModelURLs = modelUrls;
                dbAlias = db;
                dbUsername = dbUser;
                dbPassword = dbPass;
            }
        });
        final PersistenceBroker persistenceBroker = PersistenceBrokerFactory.defaultPersistenceBroker();
        Connection connection = persistenceBroker.serviceConnectionManager().getConnection();
        return generateSqlUpdates(FenixFramework.getDomainModel(), connection, charset, genDrops);
    }

    public static String generateSqlUpdates(DomainModel model, Connection connection, String charset, boolean genDrops)
            throws SQLException {
        Set<String> existingTables = getExistingTables(connection);
        Map<String, SQLTableChangeSet> changes = new HashMap<String, SQLTableChangeSet>();
        for (ClassDescriptor clazz : getDescriptorTable().values()) {
            String tablename = clazz.getFullTableName();
            if (!tablename.startsWith("OJB")) {
                if (!changes.containsKey(tablename)) {
                    existingTables.remove(tablename);
                    changes.put(tablename, new SQLTableChangeSet(new SQLTableInfo(tablename, connection)));
                }
                SQLTableChangeSet change = changes.get(tablename);
                change.addClassDescriptor(clazz);
            }

            for (final Iterator iterator = clazz.getCollectionDescriptors().iterator(); iterator.hasNext();) {
                final CollectionDescriptor collectionDescriptor = (CollectionDescriptor) iterator.next();
                final String indirectionTablename = collectionDescriptor.getIndirectionTable();
                if (indirectionTablename != null) {
                    if (!changes.containsKey(indirectionTablename)) {
                        existingTables.remove(indirectionTablename);
                        changes.put(indirectionTablename,
                                new SQLTableChangeSet(new SQLTableInfo(indirectionTablename, connection)));
                    }
                    SQLTableChangeSet change = changes.get(indirectionTablename);
                    change.addCollectionDescriptor(collectionDescriptor);
                }
            }
        }
        for (DomainRelation relation : model.getDomainRelations()) {
            if (is1toNRelation(relation)) {
                {
                    ClassDescriptor clazz = getOtherRoleClassDescriptor(relation.getFirstRole());
                    SQLTableChangeSet change = changes.get(clazz.getFullTableName());
                    change.addIndex(relation.getFirstRole());
                }
                {
                    ClassDescriptor clazz = getOtherRoleClassDescriptor(relation.getSecondRole());
                    SQLTableChangeSet change = changes.get(clazz.getFullTableName());
                    change.addIndex(relation.getSecondRole());
                }
            }
        }
        StringBuilder updates = new StringBuilder();
        if (genDrops) {
            for (String table : existingTables) {
                if (!table.startsWith("OJB_") && !table.startsWith("FF$")) {
                    updates.append("drop table " + table + ";\n");
                }
            }
        }
        for (SQLTableChangeSet change : changes.values()) {
            updates.append(change.generateSqlUpdates(genDrops, charset));
        }
        return updates.toString();
    }

    private static Set<String> getExistingTables(Connection connection) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        Set<String> existing = new HashSet<String>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("show tables");
            while (resultSet.next()) {
                existing.add(resultSet.getString(1));
            }
            return existing;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
        }
    }

    private static ClassDescriptor getOtherRoleClassDescriptor(Role role) {
        String classname = role.getOtherRole().getType().getFullName();
        return getDescriptorTable().get(classname);
    }

    private static boolean is1toNRelation(DomainRelation domRelation) {
        int multiplicity1 = domRelation.getFirstRole().getMultiplicityUpper();
        int multiplicity2 = domRelation.getSecondRole().getMultiplicityUpper();
        return ((multiplicity1 == 1) && (multiplicity2 != 1)) || ((multiplicity1 != 1) && (multiplicity2 == 1));
    }

    public static Map<String, ClassDescriptor> getDescriptorTable() {
        final MetadataManager metadataManager = MetadataManager.getInstance();
        final DescriptorRepository descriptorRepository = metadataManager.getGlobalRepository();
        return descriptorRepository.getDescriptorTable();
    }

}