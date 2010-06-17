package pt.ist.fenixframework.pstm.repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

public class SQLTableInfo {
    public static class Column {
	private static final Map<String, String> mySqlTypeTranslation = new HashMap<String, String>();
	static {
	    mySqlTypeTranslation.put("BIT", "tinyint(1)");
	    mySqlTypeTranslation.put("CHAR", "varchar(20)");
	    mySqlTypeTranslation.put("DATE", "date");
	    mySqlTypeTranslation.put("DOUBLE", "double");
	    mySqlTypeTranslation.put("FLOAT", "float(10,2)");
	    mySqlTypeTranslation.put("INTEGER", "int(11)");
	    mySqlTypeTranslation.put("LONGVARCHAR", "text");
	    mySqlTypeTranslation.put("TIME", "time");
	    mySqlTypeTranslation.put("TIMESTAMP", "timestamp NULL default NULL");
	    mySqlTypeTranslation.put("VARCHAR", "text");
	    mySqlTypeTranslation.put("BLOB", "blob");
	    mySqlTypeTranslation.put("BIGINT", "bigint(20)");

	    mySqlTypeTranslation.put(null, "tinyint(1)");
	}

	private final String name;

	private final String type;

	public Column(String name, String type) {
	    this.name = name;
	    this.type = type;
	}

	public String getName() {
	    return name;
	}

	public String toSqlColumnDefinition() {
	    return escapeName(name) + " " + sqlType();
	}

	private String sqlType() {
	    if (name.equals("ID_INTERNAL")) {
		return "int(11) NOT NULL auto_increment";
	    } else if (name.equals("OJB_CONCRETE_CLASS")) {
		return "varchar(255) NOT NULL DEFAULT ''";
	    } else if (name.startsWith("OID")) {
		return "bigint unsigned";
	    } else {
		return mySqlTypeTranslation.get(type);
	    }
	}

	@Override
	public boolean equals(Object obj) {
	    if (obj instanceof Column) {
		Column other = (Column) obj;
		return name.equals(other.name) && type.equals(other.type);
	    }
	    return false;
	}
    }

    public boolean exists;

    public final String tablename;

    public final Set<Column> columns = new HashSet<Column>();

    public final Set<String> primaryKey = new HashSet<String>();

    public final Map<String, Set<String>> indexes = new HashMap<String, Set<String>>();

    public SQLTableInfo(final String tablename, final Connection connection) throws SQLException {
	this.tablename = tablename;
	Statement statement = null;
	ResultSet resultSet = null;
	try {
	    statement = connection.createStatement();
	    resultSet = statement.executeQuery("show create table " + escapeName(tablename));
	    if (resultSet.next()) {
		exists = true;
		final String[] tableParts = extractParts(normalize(resultSet.getString(2)));
		for (final String part : tableParts) {
		    final String tablePart = part.trim();
		    if (tablePart.startsWith("PRIMARY KEY")) {
			if (!primaryKey.isEmpty()) {
			    throw new Error("More than one primary key for: " + tablename);
			}
			getSet(primaryKey, tablePart);
		    } else if (tablePart.startsWith("UNIQUE KEY")) {
			indexes.putAll(getNamedSet(tablePart));
		    } else if (tablePart.startsWith("KEY ")) {
			indexes.putAll(getNamedSet(tablePart));
		    } else {
			final int indexOfFirstSpace = tablePart.indexOf(' ');
			final String columnName = tablePart.substring(0, indexOfFirstSpace);
			columns.add(new Column(columnName, tablePart.substring(indexOfFirstSpace, tablePart.length()).trim()));
		    }
		}
	    } else {
		exists = false;
	    }
	} catch (final MySQLSyntaxErrorException mySQLSyntaxErrorException) {
	    exists = false;
	} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException ex) {
	    exists = false;
	} finally {
	    if (resultSet != null) {
		resultSet.close();
	    }
	    if (statement != null) {
		statement.close();
	    }
	}
    }

    public boolean containsColumn(String columnName) {
	for (Column column : columns) {
	    if (column.name.equals(columnName))
		return true;
	}
	return false;
    }

    public boolean containsIndex(Set<String> newIndex) {
	return indexes.values().contains(newIndex);
    }

    private void getSet(final Set<String> set, final String tablePart) {
	final String[] setParts = extractParts(tablePart);
	for (final String part : setParts) {
	    set.add(part.trim());
	}
    }

    private String[] extractParts(final String string) {
	final int indexOfOpenP = string.indexOf('(');
	final int indexOfCloseP = string.lastIndexOf(')');
	final String relevantParts = string.substring(indexOfOpenP + 1, indexOfCloseP).trim();
	final List<String> strings = new ArrayList<String>();
	boolean insideP = false;
	for (final String possiblePart : relevantParts.split(",")) {
	    if (isOpenP(possiblePart)) {
		insideP = true;
		strings.add(possiblePart);
	    } else if (isCloseP(possiblePart)) {
		insideP = false;
		final int lastPos = strings.size() - 1;
		final String last = strings.get(lastPos);
		strings.remove(lastPos);
		strings.add(last + ", " + possiblePart);
	    } else {
		if (insideP) {
		    final int lastPos = strings.size() - 1;
		    final String last = strings.get(lastPos);
		    strings.remove(lastPos);
		    strings.add(last + ", " + possiblePart);
		} else {
		    strings.add(possiblePart);
		}
	    }
	}
	final String[] result = new String[strings.size()];
	for (int i = 0; i < strings.size(); result[i] = strings.get(i++))
	    ;
	return result;
    }

    private boolean isOpenP(final String possiblePart) {
	int openCount = 0;
	for (final char c : possiblePart.toCharArray()) {
	    if (c == '(') {
		openCount++;
	    } else if (c == ')') {
		openCount--;
	    }
	}
	return openCount > 0;
    }

    private boolean isCloseP(final String possiblePart) {
	int closeCount = 0;
	for (final char c : possiblePart.toCharArray()) {
	    if (c == '(') {
		closeCount--;
	    } else if (c == ')') {
		closeCount++;
	    }
	}
	return closeCount > 0;
    }

    private String normalize(final String string) {
	return string.replace('`', ' ').replace('\n', ' ').replace('\t', ' ').replace("  ", " ").toUpperCase().trim();
    }

    private static String escapeName(String name) {
	if (name == null || name.length() == 0)
	    return name;
	if (name.charAt(0) == '`')
	    return name; // already escaped
	return "`" + name + "`";
    }

    public Map<String, Set<String>> getNamedSet(String source) {
	Map<String, Set<String>> map = new HashMap<String, Set<String>>();
	Pattern namedSet = Pattern.compile("([^\\s]+?)\\s*\\((.*)\\)");
	Matcher m = namedSet.matcher(source);
	if (m.find()) {
	    map.put(m.group(1), new HashSet<String>(Arrays.asList(m.group(2).replaceAll("\\s*", "").split(","))));
	}
	return map;
    }
}
