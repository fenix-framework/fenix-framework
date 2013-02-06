package pt.ist.fenixframework.pstm.repository.database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.ExternalizationElement;
import pt.ist.fenixframework.dml.ValueType;

public class JDBCTypeMap {

    private static Map<String, String> BUILT_IN_JDBC_MAP = new HashMap<String, String>();

    static {
        // replace the inherited built-in value-types with the following

        // primitive types
        BUILT_IN_JDBC_MAP.put("boolean", "BIT");
        BUILT_IN_JDBC_MAP.put("byte", "INTEGER");
        BUILT_IN_JDBC_MAP.put("char", "CHAR");
        BUILT_IN_JDBC_MAP.put("short", "INTEGER");
        BUILT_IN_JDBC_MAP.put("int", "INTEGER");
        BUILT_IN_JDBC_MAP.put("float", "FLOAT");
        BUILT_IN_JDBC_MAP.put("long", "BIGINT");
        BUILT_IN_JDBC_MAP.put("double", "DOUBLE");

        // their wrappers
        BUILT_IN_JDBC_MAP.put("Boolean", "BIT");
        BUILT_IN_JDBC_MAP.put("Byte", "INTEGER");
        BUILT_IN_JDBC_MAP.put("Character", "CHAR");
        BUILT_IN_JDBC_MAP.put("Short", "INTEGER");
        BUILT_IN_JDBC_MAP.put("Integer", "INTEGER");
        BUILT_IN_JDBC_MAP.put("Float", "FLOAT");
        BUILT_IN_JDBC_MAP.put("Long", "BIGINT");
        BUILT_IN_JDBC_MAP.put("Double", "DOUBLE");

        // String is, of course, essential
        BUILT_IN_JDBC_MAP.put("String", "LONGVARCHAR");

        // we need something binary, also
        BUILT_IN_JDBC_MAP.put("bytearray", "BLOB");

        // JodaTime types
        BUILT_IN_JDBC_MAP.put("DateTime", "TIMESTAMP");
        BUILT_IN_JDBC_MAP.put("LocalDate", "VARCHAR");
        BUILT_IN_JDBC_MAP.put("LocalTime", "TIME");
        BUILT_IN_JDBC_MAP.put("Partial", "LONGVARCHAR");

        BUILT_IN_JDBC_MAP.put("Serializable", "BLOB");
    }

    public static String getJdbcTypeFor(DomainModel model, String valueType) {
        ValueType vt = model.findValueType(valueType);

        String jdbcType = null;

        if (vt.isEnum()) {
            jdbcType = "VARCHAR";
        } else if (vt.isBuiltin()) {
            jdbcType = BUILT_IN_JDBC_MAP.get(valueType);
        } else {
            List<ExternalizationElement> extElems = vt.getExternalizationElements();
            if (extElems.size() != 1) {
                throw new Error("Can't handle ValueTypes with more than one externalization element, yet!");
            }
            jdbcType = getJdbcTypeFor(model, extElems.get(0).getType().getDomainName());
        }

        if (jdbcType == null) {
            throw new Error("Couldn't find a JDBC type for the value type " + valueType);
        }

        return jdbcType;
    }
}
