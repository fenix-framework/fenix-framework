package pt.ist.fenixframework.pstm.repository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;
import org.apache.ojb.broker.metadata.ObjectReferenceDescriptor;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.repository.database.DatabaseDescriptorFactory;

public class AddOIDColumns {

    private static class Table implements Comparable<Table> {

	private static final Set<Table> tables = new TreeSet<Table>();

	private final boolean isIndirectionTable;
	private final String tableName;
	private final Set<String> keys = new TreeSet<String>();

	Table(final String tableName, final boolean isIndirectionTable) {
	    this.tableName = tableName;
	    this.isIndirectionTable = isIndirectionTable;
	}

	@Override
	public int compareTo(final Table table) {
	    return tableName.compareTo(table.tableName);
	}

	void addKey(final String key) {
	    keys.add(key);
	}

	void appendAlterTable(final StringBuilder stringBuilder) {
	    stringBuilder.append("alter table ");
	    stringBuilder.append(tableName);
	    if (!isIndirectionTable) {
		stringBuilder.append("\n\tadd column OID varchar(128), add index (OID)");
	    }
	    for (final String key : keys) {
		if (!isIndirectionTable) {
		    stringBuilder.append(",");
		}
		final String oidKey = key.replace("KEY_", "OID_");
		stringBuilder.append("\n\tadd column ");
		stringBuilder.append(oidKey);
		stringBuilder.append(" varchar(128), add index (");
		stringBuilder.append(oidKey);
		stringBuilder.append(")");
	    }
	    stringBuilder.append(";\n");
	}

	static void register(final ClassDescriptor classDescriptor) {
	    final String tableName = classDescriptor.getFullTableName();
	    final Table table = findTable(tableName);
	    final Table arg = table == null ? new Table(tableName, false) : table;
	    register(arg, classDescriptor);
	}

	private static void register(final CollectionDescriptor collectionDescriptor) {
	    final String tableName = collectionDescriptor.getIndirectionTable();
	    if (tableName != null) {
		final Table table = findTable(tableName);
		final Table arg = table == null ? new Table(tableName, true) : table;
		register(arg, collectionDescriptor);
	    }
	}

	private static void register(final Table table, final ClassDescriptor classDescriptor) {
	    tables.add(table);
	    for (final Iterator iterator = classDescriptor.getObjectReferenceDescriptors().iterator(); iterator.hasNext();) {
		final ObjectReferenceDescriptor objectReferenceDescriptor = (ObjectReferenceDescriptor) iterator.next();
		final String foreignKeyField = (String) objectReferenceDescriptor.getForeignKeyFields().get(0);
		final FieldDescriptor fieldDescriptor = classDescriptor.getFieldDescriptorByName(foreignKeyField);
		final String columnName = fieldDescriptor.getColumnName();
		table.addKey(columnName);
	    }

	    for (final Iterator iterator = classDescriptor.getCollectionDescriptors().iterator(); iterator.hasNext();) {
		final CollectionDescriptor collectionDescriptor = (CollectionDescriptor) iterator.next();
		register(collectionDescriptor);
	    }
	}

	private static void register(final Table table, final CollectionDescriptor collectionDescriptor) {
	    tables.add(table);
	    register(table, collectionDescriptor.getFksToThisClass());
	    register(table, collectionDescriptor.getFksToItemClass());
	}

	private static void register(final Table table, final String[] fks) {
	    if (fks != null) {
		for (final String key : fks) {
		    table.addKey(key);
		}
	    }
	}

	static Table findTable(final String tableName) {
	    for (final Table table : tables) {
		if (table.tableName.equals(tableName)) {
		    return table;
		}
	    }
	    return null;
	}

	static String generateUpdateInstructions() {
	    final StringBuilder stringBuilder = new StringBuilder();
	    for (final Table table : tables) {
		table.appendAlterTable(stringBuilder);
	    }
	    return stringBuilder.toString();
	}
    }

    public static void main(String[] args) {
	try {
	    final String domainModelArg = args[0];
	    final String dbAliasArg = args[1];
	    final String dbUserArg = args[2];
	    final String dbPassArg = args[3];

	    FenixFramework.initialize(new Config() {{
		domainModelPath = domainModelArg;
		dbAlias = dbAliasArg;
		dbUsername = dbUserArg;
		dbPassword = dbPassArg;
	    }});

	    final PrintWriter printWriter = new PrintWriter("/tmp/addOIDColumns.sql");
	    printWriter.write(generate());
	    printWriter.close();
	} catch (final IOException ex) {
	    ex.printStackTrace();
	}
	System.out.println("Generation Complete.");
	System.exit(0);
    }

    public static String generate() {
	final Map<String, ClassDescriptor> classDescriptorMap = DatabaseDescriptorFactory.getDescriptorTable();
	final Set<String> processedTables = new HashSet<String>();
	for (final ClassDescriptor classDescriptor : classDescriptorMap.values()) {
	    final String tableName = classDescriptor.getFullTableName();
	    if (tableName != null && !tableName.startsWith("OJB") && !processedTables.contains(tableName)) {
		Table.register(classDescriptor);
	    }
	}
	return Table.generateUpdateInstructions();
    }

}
