package pt.ist.fenixframework.pstm.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.metadata.CollectionDescriptor;
import org.apache.ojb.broker.metadata.FieldDescriptor;

import pt.ist.fenixframework.pstm.repository.SQLTableInfo.Column;
import dml.Role;

public class SQLTableChangeSet {
    private static final String DEFAULT_CHARSET = "utf8";

    private final SQLTableInfo table;

    private final Map<String, Column> fields = new HashMap<String, Column>();

    private final Set<Set<String>> indexes = new HashSet<Set<String>>();

    private int concreteClasses = 0;

    public SQLTableChangeSet(SQLTableInfo table) {
	this.table = table;
    }

    public void addClassDescriptor(ClassDescriptor clazz) {
	for (FieldDescriptor field : clazz.getFieldDescriptions()) {
	    fields.put(field.getColumnName(), new Column(field.getColumnName(), field.getColumnType()));
	}
	concreteClasses++;
    }

    public void addCollectionDescriptor(CollectionDescriptor collectionDescriptor) {
	String thisClass = collectionDescriptor.getFksToThisClass()[0];
	String itemClass = collectionDescriptor.getFksToItemClass()[0];
	fields.put(thisClass, new Column(thisClass, null));
	fields.put(itemClass, new Column(itemClass, null));
	indexes.add(Collections.singleton(thisClass));
	indexes.add(Collections.singleton(itemClass));
    }

    public void addIndex(Role role) {
	if (role.getMultiplicityUpper() == 1 && role.getName() != null) {
	    indexes.add(Collections.singleton(DbUtil.getFkName(role.getName())));
	}
    }

    public String generateSqlUpdates(boolean genDrops, String tableCharset) {
	if (concreteClasses > 1) {
	    fields.put("OJB_CONCRETE_CLASS", new Column("OJB_CONCRETE_CLASS", null));
	}
	StringBuilder updates = new StringBuilder();
	updates.append(table.exists ? "alter" : "create");
	updates.append(" table " + escapeName(table.tablename));
	updates.append(table.exists ? " " : " (");
	List<String> definitions = new ArrayList<String>();
	if (genDrops) {
	    for (Entry<String, Set<String>> key : table.indexes.entrySet()) {
		if (!fields.keySet().containsAll(key.getValue())) {
		    definitions.add("drop key " + key.getKey());
		}
	    }
	    for (Column column : table.columns) {
		if (!fields.containsKey(column.getName())) {
		    definitions.add("drop " + column.getName());
		}
	    }
	}
	for (String field : fields.keySet()) {
	    if (!table.containsColumn(field)) {
		definitions.add((table.exists ? "add " : "") + fields.get(field).toSqlColumnDefinition());
	    }
	}
	if (table.primaryKey.isEmpty()) {
	    if (fields.containsKey("ID_INTERNAL")) {
		definitions.add((table.exists ? "add " : "") + "primary key (ID_INTERNAL)");
	    } else {
		definitions.add((table.exists ? "add " : "") + "primary key (" + StringUtils.join(fields.keySet(), ", ") + ")");
	    }
	}
	if (fields.containsKey("OID") && !indexes.contains(Collections.singleton("OID"))) {
	    indexes.add(Collections.singleton("OID"));
	}
	for (Set<String> index : indexes) {
	    if (!table.containsIndex(index)) {
		definitions.add((table.exists ? "add " : "") + "index (" + StringUtils.join(index.toArray(), ", ") + ")");
	    }
	}
	if (!definitions.isEmpty()) {
	    updates.append(StringUtils.join(definitions, ", "));
	    if (!table.exists) {
		updates.append(") ENGINE=InnoDB, character set ");
		updates.append(tableCharset != null ? tableCharset : DEFAULT_CHARSET);
	    }
	    updates.append(";\n");
	    return updates.toString();
	} else {
	    return "";
	}
    }

    private static String escapeName(String name) {
	if (name == null || name.length() == 0)
	    return name;
	if (name.charAt(0) == '`')
	    return name; // already escaped
	return "`" + name + "`";
    }

}
