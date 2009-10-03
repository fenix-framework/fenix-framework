package pt.ist.fenixframework.pstm.repository.database;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;

public class DatabaseDescriptorFactory {

    private DatabaseDescriptorFactory() {
    }

    public static Map<String, SqlTable> getSqlTables() {
        final Map<String, SqlTable> sqlTables = newSqlTableMap();

        final Map<String, ClassDescriptor> classDescriptorMap = getDescriptorTable();
        for (final ClassDescriptor classDescriptor : classDescriptorMap.values()) {
            addSqlTableDescription(sqlTables, classDescriptor);
        }

        return sqlTables;
    }

    private static final Map<String, SqlTable> newSqlTableMap() {
        return new TreeMap<String, SqlTable>(new Comparator() {
            public int compare(Object o1, Object o2) {
                final String tablename1 = (String) o1;
                final String tablename2 = (String) o2;
                return tablename1.compareTo(tablename2);
            }
        });
    }

    public static Map<String, ClassDescriptor> getDescriptorTable() {
        final MetadataManager metadataManager = MetadataManager.getInstance();
        final DescriptorRepository descriptorRepository = metadataManager.getGlobalRepository();
        return descriptorRepository.getDescriptorTable();
    }

    private static void addSqlTableDescription(final Map<String, SqlTable> sqlTables,
            final ClassDescriptor classDescriptor) {
        final String tablename = classDescriptor.getFullTableName();
        final String classname = classDescriptor.getClassNameOfObject();
        if (!classname.startsWith("pt.utl.ist.berserk")
                && tablename != null
                && !tablename.startsWith("OJB")
            /*
                && !tablename.equals("ROLE")
                && !tablename.equals("ROOT_DOMAIN_OBJECT")*/) {
            final SqlTable sqlTable = obtainSQLTable(sqlTables, tablename);

            addColumns(sqlTable, classDescriptor.getFieldDescriptions());
            setPrimaryKey(sqlTable, classDescriptor.getPkFields());

            processCollectionDescriptors(sqlTables, classDescriptor);
        }
    }

    private static SqlTable obtainSQLTable(final Map<String, SqlTable> sqlTables, final String tablename) {
        final SqlTable sqlTable;

        if (sqlTables.containsKey(tablename)) {
            sqlTable = sqlTables.get(tablename);
        } else {
            sqlTable = new SqlTable(tablename);
            sqlTables.put(tablename, sqlTable);
        }

        return sqlTable;
    }

    private static void addColumns(final SqlTable sqlTable, final FieldDescriptor[] fieldDescriptions) {
        if (fieldDescriptions != null) {
            for (final FieldDescriptor fieldDescriptor : fieldDescriptions) {
                sqlTable.addColumn(fieldDescriptor.getColumnName(), fieldDescriptor.getColumnType());
            }
        }
    }

    private static void setPrimaryKey(final SqlTable sqlTable, final FieldDescriptor[] pkFields) {
        final String[] primaryKey = new String[pkFields.length];
        for (int i = 0; i < pkFields.length; i++) {
            primaryKey[i] = pkFields[i].getColumnName();
        }
        sqlTable.primaryKey(primaryKey);
    }

    private static void processCollectionDescriptors(Map<String, SqlTable> sqlTables, ClassDescriptor classDescriptor) {
        for (CollectionDescriptor cod : (Iterable<CollectionDescriptor>)classDescriptor.getCollectionDescriptors()) {
            if (cod.getIndirectionTable() != null) {
                // many-to-many relation
                addSqlIndirectionTableDescription(sqlTables, cod);
            } else {
                // one-to-many means that we should index the foreign key in the other table
                ClassDescriptor otherClass = classDescriptor.getRepository().getDescriptorFor(cod.getItemClass());
                FieldDescriptor[] foreignKeys = cod.getForeignKeyFieldDescriptors(otherClass);

                SqlTable otherSqlTable = obtainSQLTable(sqlTables, otherClass.getFullTableName());
                // add index
                otherSqlTable.index(foreignKeys[0].getColumnName());
            }
        }
    }


    private static void addSqlIndirectionTableDescription(final Map<String, SqlTable> sqlTables,
            final CollectionDescriptor collectionDescriptor) {
        final String indirectionTablename = collectionDescriptor.getIndirectionTable();
        final SqlTable indirectionSqlTable = obtainSQLTable(sqlTables, indirectionTablename);

        final String foreignKeyToThis = collectionDescriptor.getFksToThisClass()[0];
        final String foreignKeyToOther = collectionDescriptor.getFksToItemClass()[0];
        
        indirectionSqlTable.addColumn(foreignKeyToThis, "BIGINT");
        indirectionSqlTable.addColumn(foreignKeyToOther, "BIGINT");

        indirectionSqlTable.primaryKey(new String[] { foreignKeyToThis, foreignKeyToOther });
    }
}
