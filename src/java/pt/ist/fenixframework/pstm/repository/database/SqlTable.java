package pt.ist.fenixframework.pstm.repository.database;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SqlTable {

    private static final Map<String, String> mySqlTypeTranslation = new HashMap<String, String>();
    static {
        mySqlTypeTranslation.put("BIT", "tinyint(1)");
        mySqlTypeTranslation.put("CHAR", "char");
        mySqlTypeTranslation.put("DATE", "date");
        mySqlTypeTranslation.put("DOUBLE", "double");
        mySqlTypeTranslation.put("FLOAT", "float(10,2)");
        mySqlTypeTranslation.put("INTEGER", "int(11)");
        mySqlTypeTranslation.put("LONGVARCHAR", "longtext");
        mySqlTypeTranslation.put("TIME", "time");
        mySqlTypeTranslation.put("TIMESTAMP", "timestamp NULL default NULL");
        mySqlTypeTranslation.put("VARCHAR", "text");
        mySqlTypeTranslation.put("BLOB", "longblob");
        mySqlTypeTranslation.put("BIGINT", "bigint(20)");

        mySqlTypeTranslation.put(null, "tinyint(1)");
    }


    public class Column {
        final String name;
        final String type;

        private Column(final String name, final String type) {
            this.name = name;
            this.type = type;
        }

        public void appendCreateTableMySql(final StringBuilder stringBuilder) {
            stringBuilder.append("`");
            stringBuilder.append(name);
            stringBuilder.append("` ");
            String typeTranslated=mySqlTypeTranslation.get(type);
            if(typeTranslated==null)
            {
            	System.out.println("No mapping defined for generic type "+type+" for the current database! Assuming that the db type will be the same as the generic type... Please review the resulting sql file for the table "+SqlTable.this.tablename+" and for field "+name);
            	typeTranslated=type;
            }
            stringBuilder.append(typeTranslated);
            if (name.equals("ID_INTERNAL")) {
        	stringBuilder.append(" NOT NULL auto_increment");
        	//stringBuilder.append(", OID bigint unsigned default null");
            }
        }

        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Column) {
                final Column column = (Column) obj;
                return name.equals(column.name);
            }
            return false;
        }

        public int hashCode() {
            return name.hashCode();
        }
    }


    final String tablename;

    final Set<Column> columns = new TreeSet<Column>(new Comparator() {
        public int compare(Object o1, Object o2) {
            final Column column1 = (Column) o1;
            final Column column2 = (Column) o2;
            return column1.name.compareTo(column2.name);
        }
    });

    final Set<String> indexes = new TreeSet<String>();

    String[] primaryKey = null;

    /*
     * The default character set for each table, unless otherwise
     * specified, is "utf8".
     *
     * Actually, according to the description made in the comments at
     * the end of the appendCreateTableMySql method, the only correct
     * way of dealing with encodings in the present combination of
     * technologies used is by using always UTF-8.  So, this should be
     * left unchanged.
     *
     * Yet, because of legacy reasons, the Fenix database has all its
     * tables in latin1, so we must provide some way of dealing with
     * that legacy.  Changing this value is one such way...
     */
    String defaultCharacterSet = "utf8";

    public SqlTable(final String tablename) {
        this.tablename = tablename;
    }

    public void addColumn(final String name, final String type) {
        columns.add(new Column(name, type));
    }

    public void index(final String columnName) {
        indexes.add(columnName);
    }

    public void primaryKey(final String[] primaryKey) {
        this.primaryKey = primaryKey;
    }

    public void setDefaultCharacterSet(String defaultCharacterSet) {
        this.defaultCharacterSet = defaultCharacterSet;
    }

    public void appendCreateTableMySql(final StringBuilder stringBuilder) {
        stringBuilder.append("create table `");
        stringBuilder.append(tablename);
        stringBuilder.append("` (\n");

        for (final Iterator iterator = columns.iterator(); iterator.hasNext();) {
            final Column column = (Column) iterator.next();
            stringBuilder.append("  ");
            column.appendCreateTableMySql(stringBuilder);
            if (iterator.hasNext()) {
                stringBuilder.append(",");
                stringBuilder.append("\n");
            }
        }

        if (primaryKey != null) {
            stringBuilder.append(",\n  primary key (");
            for (int i = 0; i < primaryKey.length; i++) {
                if (i > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(primaryKey[i]);
            }
            stringBuilder.append(")");

            stringBuilder.append(",\n  index (OID)");
        } else {
            System.out.println("No primary key for table " + tablename);
        }

        for (final String columnName : indexes) {
            stringBuilder.append(",\n  index (");
            stringBuilder.append(columnName);
            stringBuilder.append(")");
        }
        stringBuilder.append("\n");

	/* We must ensure that the tables and the connection are in UTF-8.  This is so because:
	 * - strings are being stored as LONGVARCHAR, which in mysql maps to TEXT
	 * - OJB calls setCharacterStream (in prepared statementes)
	 * - connector/J sends hexadecimal strings (binary-strings) when storing a character stream
	 * - binary-strings are not processed for enconding, being stored as they were sent
	 *
	 * -> thus, currently the format (encoding) in which we send strings must match the column's encoding
	 */
	stringBuilder.append(") ENGINE=InnoDB, character set ");
	stringBuilder.append(defaultCharacterSet);
	stringBuilder.append(" ;\n\n");
    }
}
