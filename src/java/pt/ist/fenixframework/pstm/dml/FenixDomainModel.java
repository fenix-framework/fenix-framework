package pt.ist.fenixframework.pstm.dml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dml.DomainModel;
import dml.ExternalizationElement;
import dml.PlainValueType;
import dml.ValueType;

public class FenixDomainModel extends DomainModel {

    private static Map<String, String> BUILT_IN_JDBC_MAP = new HashMap<String, String>();

    private static final String TX_NUMBER_CLASS = "pt.ist.fenixframework.TxNumber";

    private static String[] NON_NULLABLE_TYPES = { "boolean", "byte", "char", "short", "int", "float", "long", "double" };

    public static boolean isNullableType(ValueType vt) {
	String vtFullName = vt.getFullname();
	for (String nonNullableType : NON_NULLABLE_TYPES) {
	    if (nonNullableType.equals(vtFullName)) {
		return false;
	    }
	}
	return true;
    }

    public FenixDomainModel() {
	super();
	initializeDerivedValueTypes();
    }

    @Override
    protected void initializeBuiltinValueTypes() {
	// replace the inherited built-in value-types with the following

	// primitive types
	registerFenixValueType("boolean", "boolean", "BIT");
	registerFenixValueType("byte", "byte", "INTEGER");
	registerFenixValueType("char", "char", "CHAR");
	registerFenixValueType("short", "short", "INTEGER");
	registerFenixValueType("int", "int", "INTEGER");
	registerFenixValueType("float", "float", "FLOAT");
	registerFenixValueType("long", "long", "BIGINT");
	registerFenixValueType("double", "double", "DOUBLE");

	// their wrappers
	registerFenixValueType("java.lang.Boolean", "Boolean", "BIT");
	registerFenixValueType("java.lang.Byte", "Byte", "INTEGER");
	registerFenixValueType("java.lang.Character", "Character", "CHAR");
	registerFenixValueType("java.lang.Short", "Short", "INTEGER");
	registerFenixValueType("java.lang.Integer", "Integer", "INTEGER");
	registerFenixValueType("java.lang.Float", "Float", "FLOAT");
	registerFenixValueType("java.lang.Long", "Long", "BIGINT");
	registerFenixValueType("java.lang.Double", "Double", "DOUBLE");

	// String is, of course, essential
	registerFenixValueType("java.lang.String", "String", "LONGVARCHAR");

	// we need something binary, also
	registerFenixValueType("byte[]", "bytearray", "BLOB");

	// JodaTime types
	registerFenixValueType("org.joda.time.DateTime", "DateTime", "TIMESTAMP");
	registerFenixValueType("org.joda.time.LocalDate", "LocalDate", "VARCHAR");
	registerFenixValueType("org.joda.time.LocalTime", "LocalTime", "TIME");
	registerFenixValueType("org.joda.time.Partial", "Partial", "LONGVARCHAR");

	// The JodaTime's Period class is dealt with in the Fenix app code base
	// for the time being
	// registerFenixValueType("org.joda.time.Period", "Period", "");
    }

    protected void initializeDerivedValueTypes() {
	String txNumberClassName = TX_NUMBER_CLASS;
	PlainValueType txNumType = new PlainValueType(txNumberClassName);

	String externalizeName = txNumberClassName + ".externalize";
	ValueType longType = findValueType("Long");
	txNumType.addExternalizationElement(new ExternalizationElement(longType, externalizeName));

	String internalizeName = txNumberClassName + ".internalize";
	txNumType.setInternalizationMethodName(internalizeName);

	newValueType("TxNumber", txNumType);
    }

    protected void registerFenixValueType(String valueTypeName, String aliasName, String jdbcType) {
	newValueType(aliasName, valueTypeName);
	BUILT_IN_JDBC_MAP.put(aliasName, jdbcType);
    }

    /*
     * This method will need to be changed once we get rid of OJB.
     */
    public String getJdbcTypeFor(String valueType) {
	ValueType vt = findValueType(valueType);

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
	    jdbcType = getJdbcTypeFor(extElems.get(0).getType().getDomainName());
	}

	if (jdbcType == null) {
	    throw new Error("Couldn't find a JDBC type for the value type " + valueType);
	}

	return jdbcType;
    }
}
