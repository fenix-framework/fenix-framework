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

import org.apache.commons.lang.StringUtils;

import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.dml.FenixDomainModelWithOCC;
import pt.ist.fenixframework.pstm.repository.database.DatabaseDescriptorFactory;
import pt.ist.fenixframework.pstm.repository.database.SqlTable;

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

	    FenixFramework.initialize(new Config() {{
                domainModelClass = FenixDomainModelWithOCC.class;
		domainModelPaths = domainModelFiles;
		dbAlias = dbAliasArg;
		dbUsername = dbUserArg;
		dbPassword = dbPassArg;
	    }});

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

    public static void checkOids(Connection connection) throws Exception {
        Map<String,ClassDescriptor> ojbMetadata = DatabaseDescriptorFactory.getDescriptorTable();

        DomainModel domainModel = FenixFramework.getDomainModel();
        List<String> classnames = new ArrayList<String>();
        for (DomainClass domClass : domainModel.getDomainClasses()) {
            classnames.add(domClass.getFullName());
        }
        
        Collections.sort(classnames);

        System.out.println("Total classes = " + classnames.size());

        int num = 0;
        for (String classname : classnames) {
            if (classname.equals("net.sourceforge.fenixedu.domain.Enrolment")) {
                continue;
            }

            ClassDescriptor cd = ojbMetadata.get(classname);
            if (cd == null) {
                System.err.println(" ##### Couldn't find a classDescriptor for class " + classname);
            } else {
                String tableName = cd.getFullTableName();
                System.out.print("Checking oids for " + classname);
                System.out.print(" # = " + num++);
                System.out.println(" (table = " + tableName + ")");
                checkOidsForTable(connection, tableName, domainModel.findClass(classname), cd);
	    }
	}
    }

    private static void checkOidsForTable(Connection connection,
                                          String tableName,
                                          DomainClass domClass,
                                          ClassDescriptor classDesc) throws Exception {

        String where = "";
        
        if (classDesc.getFieldDescriptorByName("ojbConcreteClass") != null) {
            where = " where OJB_CONCRETE_CLASS = '" + domClass.getFullName() + "'";
        }

        int numRows = countRows(connection, tableName, where);

        System.out.println("Total rows = " + numRows);

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

        int num = 0;
        int tot = 0;
        while (rs.next()) {
            num++;
            tot++;
            if ((numRows > 100000) && (num == (numRows / 10))) {
                System.out.println("   (progress = " + tot + ")");
                num = 0;
            }

            int idInternal = rs.getInt(1);
            long oid = rs.getLong(2);

            if (! same(idInternal, oid)) {
                System.err.println(" #################### mismatch between ID_INTERNAL and OID: " + idInternal + " != " + oid);
            }

            // check all the foreign keys, also
            int pos = 3;
            for (String fk : foreignKeys) {
                int key = rs.getInt(pos++);
                long oidKey = rs.getLong(pos++);

                if (! same(key, oidKey)) {
                    System.err.print(" ######### mismatch between KEY and OID: " + key + " != " + oidKey);
                    System.err.println(" (colName = " + fk + ", OID = " + oid + ")");
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
