package pt.ist.fenixframework.pstm.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.dml.FenixDomainModelWithOCC;
import pt.ist.fenixframework.pstm.repository.database.DatabaseDescriptorFactory;
import dml.DomainClass;
import dml.DomainModel;
import dml.Role;

public class CheckOids {

    public static void main(String[] args) {
	Connection connection = null;
	try {
            final String dbAliasArg = getArg(args, 0);
            final String dbUserArg = getArg(args, 1);
            final String dbPassArg = getArg(args, 2);

            // all the remaining args are DML files
	    final String[] domainModelFiles = Arrays.copyOfRange(args, 3, args.length);

	    FenixFramework.bootStrap(new Config() {{
                domainModelClass = FenixDomainModelWithOCC.class;
		domainModelPaths = domainModelFiles;
		dbAlias = dbAliasArg;
		dbUsername = dbUserArg;
		dbPassword = dbPassArg;
	    }});
	    FenixFramework.initialize();

            connection = PersistenceBrokerFactory.defaultPersistenceBroker().serviceConnectionManager().getConnection();
	    checkOids(connection);
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    if (connection != null) {
		try {
		    connection.close();
		} catch (SQLException e) {
		    // nothing can be done.
		}
	    }
	}
    }


    private static List<String> getAllClassnames(DomainModel domainModel) {
        List<String> classnames = new ArrayList<String>();
        for (DomainClass domClass : domainModel.getDomainClasses()) {
            classnames.add(domClass.getFullName());
        }
        
        Collections.sort(classnames);
        return classnames;
    }


    public static void checkOids(Connection connection) throws Exception {
        checkOidsOfObjects(connection);
        checkOidsOfIndirectionTables(connection);
    }

    public static void checkOidsOfObjects(Connection connection) throws Exception {
        DomainModel domainModel = FenixFramework.getDomainModel();
        List<String> classnames = getAllClassnames(domainModel);

        System.out.println("Total classes = " + classnames.size());

        Map<String,ClassDescriptor> ojbMetadata = DatabaseDescriptorFactory.getDescriptorTable();

        int num = 0;
        for (String classname : classnames) {
            ClassDescriptor cd = ojbMetadata.get(classname);
            if (cd == null) {
                System.err.println(" ##### Couldn't find a classDescriptor for class " + classname);
            } else {
                System.out.printf("%5d - ", num++);
                System.out.print("Checking oids for " + classname);
                checkOidsForClass(connection, domainModel.findClass(classname), cd);
	    }
	}
    }

    private static void checkOidsForClass(Connection connection,
                                          DomainClass domClass,
                                          ClassDescriptor classDesc) throws Exception {

        String where = "";

        // handle the special case of Fenix, where we are still using
        // the OJB_CONCRETE_CLASS column to identify the class of the objects
        if (classDesc.getFieldDescriptorByName("ojbConcreteClass") != null) {
            where = " where OJB_CONCRETE_CLASS = '" + domClass.getFullName() + "'";
        }

        String tableName = classDesc.getFullTableName();
        int numRows = countRows(connection, tableName, where);

        System.out.printf(" (table = %s, rows = %d)\n", tableName, numRows);

        StringBuilder selectStmt = new StringBuilder();
        selectStmt.append("select ID_INTERNAL,OID");

        List<String> foreignKeys = getForeignKeys(domClass, classDesc);

        // append all the KEYs and matching OIDs
        for (String fk : foreignKeys) {
            selectStmt.append(",");
            selectStmt.append(fk);
            selectStmt.append(",OID_");
            selectStmt.append(fk.substring(4));
        }

        selectStmt.append(" from ");
        selectStmt.append(tableName);
        selectStmt.append(where);

        // Now, execute the select and check the results
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectStmt.toString());

        while (rs.next()) {
            int idInternal = rs.getInt(1);
            long oid = rs.getLong(2);

            if (! same(idInternal, oid)) {
                System.err.println("        !!! mismatch between ID_INTERNAL and OID: " + idInternal + " != " + oid);
            }

            // check all the foreign keys, also
            int pos = 3;
            for (String fk : foreignKeys) {
                int key = rs.getInt(pos++);
                long oidKey = rs.getLong(pos++);

                if (! same(key, oidKey)) {
                    System.err.print("        ### mismatch between KEY and OID: " + key + " != " + oidKey);
                    System.err.println(" (for colName = " + fk + " in object with OID = " + oid + ")");
                }
            }
        }

        rs.close();
        stmt.close();
    }


    private static final class IndirectionTable implements Comparable<IndirectionTable> {
        private final String table;
        private final CollectionDescriptor colDesc;

        IndirectionTable(String table, CollectionDescriptor colDesc) {
            this.table = table;
            this.colDesc = colDesc;
        }

        public int compareTo(IndirectionTable other) {
            return this.table.compareTo(other.table);
        }

        public boolean equals(Object other) {
            return (other != null) && (other.getClass() == this.getClass()) && ((IndirectionTable)other).table.equals(this.table);
        }

        public int hashCode() {
            return table.hashCode();
        }
    }
        

    public static void checkOidsOfIndirectionTables(Connection connection) throws Exception {
        Set<IndirectionTable> tablesToCheck = new TreeSet<IndirectionTable>();
	for (ClassDescriptor classDesc : DatabaseDescriptorFactory.getDescriptorTable().values()) {
	    for (CollectionDescriptor colDesc : (Iterable<CollectionDescriptor>)classDesc.getCollectionDescriptors()) {
		String tableName = colDesc.getIndirectionTable();
                if (tableName != null) {
                    tablesToCheck.add(new IndirectionTable(tableName, colDesc));
                }
            }
        }

        System.out.println("Total indirection tables = " + tablesToCheck.size());

        int num = 0;
        for (IndirectionTable indTable : tablesToCheck) {
            System.out.printf("%5d - ", num++);
            System.out.print("Checking oids for " + indTable.table);
            checkOidsForIndTable(connection, indTable.colDesc);
	}
    }

    public static void checkOidsForIndTable(Connection connection, CollectionDescriptor colDesc) throws Exception {
        String tableName = colDesc.getIndirectionTable();
        int numRows = countRows(connection, tableName, "");

        System.out.printf(" (rows = %d)\n", numRows);

        StringBuilder selectStmt = new StringBuilder();
        selectStmt.append("select ");

        String firstKey = colDesc.getFksToThisClass()[0];
	selectStmt.append(firstKey);
	selectStmt.append(",");
	selectStmt.append(firstKey.replace("KEY_", "OID_"));
	selectStmt.append(",");

        String secondKey = colDesc.getFksToItemClass()[0];
	selectStmt.append(secondKey);
	selectStmt.append(",");
	selectStmt.append(secondKey.replace("KEY_", "OID_"));

        selectStmt.append(" from ");
        selectStmt.append(tableName);

        String[] foreignKeys = new String[] { firstKey, secondKey };

        // Now, execute the select and check the results
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectStmt.toString());

        while (rs.next()) {
            int pos = 1;
            for (String fk : foreignKeys) {
                int key = rs.getInt(pos++);
                long oidKey = rs.getLong(pos++);

                if (! same(key, oidKey)) {
                    System.err.print("        ### mismatch between KEY and OID: " + key + " != " + oidKey);
                    System.err.println(" (for colName = " + fk + ")");
                }
            }
        }

        rs.close();
        stmt.close();
    }

    private static boolean same(int id, long oid) {
        return ((int)(oid & 0x7FFFFFFF)) == id;
    }

    private static int countRows(Connection connection, String tableName, String where) throws Exception {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("select count(*) from " + tableName + where);
        rs.next();
        int res = rs.getInt(1);
        rs.close();
        stmt.close();
        return res;
    }

    private static List<String> getForeignKeys(DomainClass domClass, ClassDescriptor classDesc) {
        List<String> fks = new ArrayList<String>();

        while (domClass != null) {
            for (Role role : domClass.getRoleSlotsList()) {
                String roleName = role.getName();
                if ((role.getMultiplicityUpper() == 1) && (roleName != null)) {
                    String foreignKeyField = "key" + StringUtils.capitalize(roleName);
                    FieldDescriptor fd = classDesc.getFieldDescriptorByName(foreignKeyField);
                    fks.add(fd.getColumnName());
                }
            }

            domClass = (DomainClass)domClass.getSuperclass();
        }
        
        return fks;
    }
 
    private static String getArg(String[] args, int index) {
        if (args.length < index) {
            System.out.println("Usage: CheckOids <dbAlias> <dbUser> <dbPasswd> <dmlFile>+");
            System.exit(1);
        }

        return args[index];
    }
}
