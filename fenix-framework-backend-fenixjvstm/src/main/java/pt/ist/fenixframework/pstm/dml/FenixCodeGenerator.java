package pt.ist.fenixframework.pstm.dml;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import pt.ist.fenixframework.pstm.ToSqlConverter;
import pt.ist.fenixframework.pstm.repository.DbUtil;
import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DomainClass;
import dml.DomainEntity;
import dml.DomainModel;
import dml.ExternalizationElement;
import dml.Role;
import dml.Slot;
import dml.ValueType;

public class FenixCodeGenerator extends CodeGenerator {

    protected static final String TO_SQL_CONVERTER_CLASS = ToSqlConverter.class.getName();

    protected static final String RESULT_SET_READER_CLASS = "pt.ist.fenixframework.pstm.ResultSetReader";

    protected static final String IMPORTS_COMMA_SEPARATED = "pt.ist.fenixframework.pstm.VBox,pt.ist.fenixframework.pstm.RelationList,pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper,pt.ist.fenixframework.ValueTypeSerializationGenerator.*";
    protected static final String DOMAIN_CLASS_ROOT = "pt.ist.fenixframework.pstm.AbstractDomainObject";
    protected static final String DIRECT_RELATION_TYPE_CLASS = "pt.ist.fenixframework.pstm.LoggingRelation";
    protected static final String TRANSACTION_CLASS = "pt.ist.fenixframework.pstm.Transaction";

    public FenixCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
	return DOMAIN_CLASS_ROOT;
    }

    protected String[] getImportFiles() {
	return IMPORTS_COMMA_SEPARATED.split(",");
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
	generatePackageDecl(subPackageName, out);
	for (String importFile : getImportFiles()) {
	    println(out, "import " + importFile + ";");
	}
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
	super.generateBaseClassBody(domClass, out);
	generateCheckDisconnected(domClass, out);
	generateDatabaseReader(domClass, out);
    }

    @Override
    protected void generateBaseClassConstructorsBody(DomainClass domClass, PrintWriter out) {
	super.generateBaseClassConstructorsBody(domClass, out);
	final Slot ojbConcreteClassSlot = domClass.findSlot("ojbConcreteClass");
	if (ojbConcreteClassSlot != null && calculateHierarchyLevel(domClass) == 1) {
	    newline(out);
	    print(out, "setOjbConcreteClass(getClass().getName());");
	}
    }

    private int calculateHierarchyLevel(DomainEntity domainEntity) {
	return hasSuperclass(domainEntity) ? calculateHierarchyLevel(((DomainClass) domainEntity).getSuperclass()) + 1 : 0;
    }

    private boolean hasSuperclass(DomainEntity domainEntity) {
	return domainEntity != null && domainEntity instanceof DomainClass
		&& ((DomainClass) domainEntity).getSuperclass() != null;
    }

    @Override
    protected String getDirectRelationType() {
	return DIRECT_RELATION_TYPE_CLASS;
    }

    @Override
    protected void generateStaticRelationSlots(Role role, PrintWriter out) {
	super.generateStaticRelationSlots(role, out);

	if (role.isFirstRole() || (role.getOtherRole().getName() == null)) {
	    String relationName = getRelationSlotNameFor(role);

	    // set the relationName of the LoggingRelation object
	    newline(out);
	    printWords(out, "static");
	    newBlock(out);
	    printWords(out, relationName);
	    print(out, ".setRelationName(\"");
	    print(out, getTypeFullName(role.getOtherRole().getType()));
	    print(out, ".");
	    print(out, role.getRelation().getName());
	    print(out, "\");");

	    if ((role.getMultiplicityUpper() != 1) && (role.getOtherRole().getMultiplicityUpper() != 1)) {

		// a relation many-to-many need a listener...
		Role otherRole = role.getOtherRole();
		String firstType = getTypeFullName(otherRole.getType());
		String secondType = getTypeFullName(role.getType());

		newline(out);
		printWords(out, relationName);
		print(out, ".addListener(new ");
		print(out, makeGenericType("dml.runtime.RelationAdapter", firstType, secondType));
		print(out, "()");
		newBlock(out);

		println(out, "@Override");
		printMethod(out, "public", "void", "beforeAdd", makeArg(firstType, "arg0"), makeArg(secondType, "arg1"));
		startMethodBody(out);
		generateRelationRegisterCall("addRelationTuple", role, otherRole, out);
		endMethodBody(out);

		println(out, "@Override");
		printMethod(out, "public", "void", "beforeRemove", makeArg(firstType, "arg0"), makeArg(secondType, "arg1"));
		startMethodBody(out);
		generateRelationRegisterCall("removeRelationTuple", role, otherRole, out);
		endMethodBody(out);

		closeBlock(out);
		print(out, ");");
	    }

	    // close the static block
	    closeBlock(out);
	}
    }

    protected void generateRelationRegisterCall(String regMethodName, Role r0, Role r1, PrintWriter out) {
	String r0name = r0.getName();
	String r1name = r1.getName();

	print(out, TRANSACTION_CLASS);
	print(out, ".");
	print(out, regMethodName);
	print(out, "(\"");
	print(out, getEntityFullName(r0.getRelation()));
	print(out, "\", arg1, \"");
	print(out, (r1name == null) ? "" : r1name);
	print(out, "\", arg0, \"");
	print(out, (r0name == null) ? "" : r0name);
	print(out, "\");");
    }

    @Override
    protected void generateSetterBody(String setterName, String slotName, String typeName, PrintWriter out) {
	if (!setterName.startsWith("set$")) {
	    print(out, getSlotExpression(slotName));
	    print(out, ".put(this, \"");
	    print(out, slotName);
	    print(out, "\", ");
	    print(out, slotName);
	    print(out, ");");
	} else {
	    super.generateSetterBody(setterName, slotName, typeName, out);
	}
    }

    @Override
    protected void generateInitInstance(DomainClass domClass, PrintWriter out) {
	// generate initInstance method to be used by OJB
	onNewline(out);
	newline(out);
	printMethod(out, "private", "void", "initInstance");
	startMethodBody(out);
	print(out, "initInstance(true);");
	endMethodBody(out);

	super.generateInitInstance(domClass, out);
    }

    @Override
    protected void generateInitializePrimitiveIfNeeded(Slot slot, PrintWriter out) {
	super.generateInitializePrimitiveIfNeeded(slot, out);
	if (findWrapperEntry(slot.getTypeName()) == null) {
	    generateSlotInitialization(slot.getName(), out);
	}
    }

    protected void generateSlotInitialization(String name, PrintWriter out) {
	// This method generates the code that initializes all of the
	// non-primitive slots to null, so that the corresponding vbox
	// appears in the TxIntrospector.getWriteSetLog() set of
	// entries. This will probably disappear once we have the
	// HBase-based version of the fenix-framework, where each box
	// knows to which object and attribute it belongs to. If we
	// had that, the getWriteSetLog method could use the JVSTM
	// write-set instead of the DBChanges.attrChangeLogs. This,
	// of course, assuming that creating a box always calls its
	// put method, with the consequence of making that box go into
	// the write-set. Even though I've not checked this rigth
	// now, I strongly believe that's the behavior of the JVSTM.
	onNewline(out);
	print(out, "if (!allocateOnly) this.");
	print(out, name + ".put(this, \"" + name);
	println(out, "\", null);");
    }

    @Override
    protected String getNewSlotExpression(Slot slot) {
	return "VBox.makeNew(this, \"" + slot.getName() + "\", allocateOnly, false)";
    }

    @Override
    protected String getNewRoleOneSlotExpression(Role role) {
	return "VBox.makeNew(this, \"" + role.getName() + "\", allocateOnly, false)";
    }

    @Override
    protected String getNewRoleStarSlotExpression(Role role) {
	StringBuilder buf = new StringBuilder();

	// generate the relation aware collection
	String thisType = getTypeFullName(role.getOtherRole().getType());
	buf.append("new ");
	buf.append(getRelationAwareTypeFor(role));
	buf.append("((");
	buf.append(thisType);
	buf.append(")this, ");
	buf.append(getRelationSlotNameFor(role));
	buf.append(", \"");
	buf.append(role.getName());
	buf.append("\", allocateOnly)");

	return buf.toString();
    }

    @Override
    protected String getRelationAwareBaseTypeFor(Role role) {
	// FIXME: handle other types of collections other than sets
	return "RelationList";
    }

    @Override
    protected String getBoxBaseType() {
	return "VBox";
    }

    @Override
    protected String getRoleArgs(Role role) {
	String args = super.getRoleArgs(role);
	if ((role.getName() != null) && (role.getMultiplicityUpper() == 1)) {
	    if (args.length() > 0) {
		args += ", ";
	    }
	    args += "\"" + role.getName() + "\"";
	}
	return args;
    }

    @Override
    protected String getRoleOneBaseType() {
	return "dml.runtime.RoleOneFenix";
    }

    @Override
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
	print(out, "pt.ist.fenixframework.pstm.DataAccessPatterns.noteGetAccess(this, \"");
	print(out, slotName);
	println(out, "\");");
	print(out, "return ");
	generateGetSlotExpression(slotName, out);
	print(out, ";");
    }

    protected void generateGetSlotExpression(String slotName, PrintWriter out) {
	print(out, getSlotExpression(slotName));
	print(out, ".get(this, \"");
	print(out, slotName);
	print(out, "\")");
    }

    protected void generateRelationGetter(String getterName, Role role, PrintWriter out) {
	String paramListType = makeGenericType("java.util.List", getTypeFullName(role.getType()));
	generateRelationGetter(role, paramListType, out);
    }

    protected void generateRelationGetter(Role role, String paramListType, PrintWriter out) {
	generateRelationGetter("get" + capitalize(role.getName()), getSlotExpression(role.getName()), paramListType, out);
    }

    protected void generateRelationGetter(String getterName, String valueToReturn, String typeName, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", typeName, getterName);

	startMethodBody(out);
	print(out, "return ");
	print(out, valueToReturn);
	print(out, ";");
	endMethodBody(out);
    }

    @Override
    protected void generateInitRoleSlot(Role role, PrintWriter out) {
	super.generateInitRoleSlot(role, out);
	if (role.getMultiplicityUpper() == 1) {
	    generateSlotInitialization(role.getName(), out);
	}
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {
	super.generateRoleSlotMethodsMultStar(role, out);
	generateRoleSlotMethodsMultStarGettersAndIterators(role, out);
    }

    protected void generateRoleSlotMethodsMultStarGettersAndIterators(Role role, PrintWriter out) {
	generateRelationGetter("get" + capitalize(role.getName()), role, out);
	generateOJBSetter(role.getName(), "OJBFunctionalSetWrapper", out);
	generateIteratorMethod(role, out);
    }

    protected void generateIteratorMethod(Role role, PrintWriter out) {
	generateIteratorMethod(role, out, getSlotExpression(role.getName()));
    }

    protected void generateIteratorMethod(Role role, PrintWriter out, final String slotAccessExpression) {
	newline(out);
	printFinalMethod(out, "public", makeGenericType("java.util.Iterator", getTypeFullName(role.getType())), "get"
		+ capitalize(role.getName()) + "Iterator");
	startMethodBody(out);
	printWords(out, "return", slotAccessExpression);
	print(out, ".iterator();");
	endMethodBody(out);
    }

    @Override
    protected void generateSlotAccessors(Slot slot, PrintWriter out) {
	super.generateSlotAccessors(slot, out);
	generateExternalizationGetter(slot.getName(), slot.getSlotType(), out);
	generateInternalizationSetter(slot.getName(), slot.getSlotType(), out);
    }

    protected void generateExternalizationGetter(String name, ValueType type, PrintWriter out) {
	newline(out);
	ValueType vt = getExternalizationType(type);
	String returnType = getSqlTypeName(type);
	printFinalMethod(out, "private", returnType, "get$" + name);

	startMethodBody(out);
	// handle nulls (if the value is null, then return null)
	print(out, type.getFullname());
	print(out, " value = ");
	generateGetSlotExpression(name, out);
	println(out, ";");

	print(out, "return ");

	if (FenixDomainModel.isNullableType(type)) {
	    print(out, "(value == null) ? null : ");
	}

	print(out, getExternalizationExpression(type));
	print(out, ";");
	endMethodBody(out);
    }

    protected String getExternalizationExpression(ValueType vt) {
	StringBuilder expression = new StringBuilder();

	// start with the variable holding the slot value (not null)
	expression.append("value");

	// now, go through the externalization elements, externalizing this
	// value
	while (!(vt.isBuiltin() || vt.isEnum())) {
	    List<ExternalizationElement> extElems = vt.getExternalizationElements();
	    if (extElems.size() != 1) {
		throw new Error("Can't handle value-types with more than one externalization element yet...");
	    }

	    ExternalizationElement extElem = extElems.get(0);
	    String extMethodName = extElem.getMethodName();

	    if (extMethodName.contains(".")) {
		// a static method
		expression.insert(0, extMethodName + "(");
		expression.append(")");
	    } else {
		// a class-member method
		expression.append(".");
		expression.append(extMethodName);
		expression.append("()");
	    }

	    vt = extElem.getType();
	}

	// wrap the expression with the final converter method call
	// note that this is being constructed backwards...
	if (vt.isEnum()) {
	    expression.insert(0, "Enum(");
	} else {
	    expression.insert(0, vt.getDomainName() + "(");
	}

	expression.insert(0, ".getValueFor");
	expression.insert(0, TO_SQL_CONVERTER_CLASS);

	// close the wrap-up
	expression.append(")");

	return expression.toString();
    }

    protected void generateInternalizationSetter(String name, ValueType type, PrintWriter out) {
	newline(out);
	print(out, "private final void set$");
	print(out, name);
	print(out, "(");

	ValueType vt = getExternalizationType(type);
	print(out, vt.getFullname());
	print(out, " arg0, int txNumber)");

	startMethodBody(out);
	print(out, "this.");
	print(out, name);
	print(out, ".persistentLoad(");

	if (FenixDomainModel.isNullableType(vt)) {
	    print(out, "(arg0 == null) ? null : ");
	}

	print(out, getRsReaderExpression(type));
	print(out, ", txNumber);");
	endMethodBody(out);
    }

    protected ValueType getExternalizationType(ValueType vt) {
	while (!(vt.isBuiltin() || vt.isEnum())) {
	    List<ExternalizationElement> extElems = vt.getExternalizationElements();
	    if (extElems.size() != 1) {
		throw new Error("Can't handle value-types with more than one externalization element yet...");
	    }

	    ExternalizationElement extElem = extElems.get(0);
	    vt = extElem.getType();
	}

	return vt;
    }

    protected String getSqlTypeName(ValueType vt) {
	ValueType extType = getExternalizationType(vt);
	String toSqlMethodName = "getValueFor" + (extType.isEnum() ? "Enum" : extType.getDomainName());

	for (Method m : ToSqlConverter.class.getDeclaredMethods()) {
	    if (m.getName().equals(toSqlMethodName)) {
		return m.getReturnType().getName();
	    }
	}

	throw new Error("Something's wrong.  Couldn't find the appropriate base value type.");
    }

    protected String getRsReaderExpression(ValueType type) {
	StringBuilder buf = new StringBuilder();
	buildReconstructionExpression(buf, type, 0);
	return buf.toString();
    }

    protected int buildReconstructionExpression(StringBuilder buf, ValueType vt, int colNum) {

	// first, check if is a built-in value type
	// if it is, then process it and return
	if (vt.isBuiltin()) {
	    buf.append("arg" + colNum);
	    return colNum + 1;
	}

	// it is not built-in, process it normally
	String intMethodName = vt.getInternalizationMethodName();

	// if no internalizationMethodName is present, then use the constructor
	if (intMethodName == null) {
	    buf.append("new ");
	    buf.append(vt.getFullname());
	} else {
	    if (!intMethodName.contains(".")) {
		// assume that non-dotted names correspond to static methods of
		// the ValueType vt
		buf.append(vt.getFullname());
		buf.append(".");
	    }

	    buf.append(intMethodName);
	}

	buf.append("(");

	for (ExternalizationElement extElem : vt.getExternalizationElements()) {
	    if (colNum > 0) {
		buf.append(", ");
	    }
	    colNum = buildReconstructionExpression(buf, extElem.getType(), colNum);
	}

	buf.append(")");

	return colNum;
    }

    protected void generateOJBSetter(String slotName, String typeName, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", "void", "set$" + slotName, makeArg(typeName, slotName));

	startMethodBody(out);
	printWords(out, getSlotExpression(slotName));
	print(out, ".setFromOJB(this, \"");
	print(out, slotName);
	print(out, "\", ");
	print(out, slotName);
	print(out, ");");
	endMethodBody(out);
    }

    @Override
    protected void generateRoleSlotMethodsMultOne(Role role, PrintWriter out) {
	super.generateRoleSlotMethodsMultOne(role, out);

	String typeName = getTypeFullName(role.getType());
	String slotName = role.getName();
	// generateGetter("public", "get$" + slotName, slotName, typeName, out);
	// generateOJBSetter(slotName, typeName, out);
	generateOidOJBGetter(slotName, out);
    }

    protected void generateOidOJBGetter(String name, PrintWriter out) {
	newline(out);
	printMethod(out, "private", "java.lang.Long", "get$oid" + capitalize(name));
	startMethodBody(out);
	print(out, DOMAIN_CLASS_ROOT);
	print(out, " value = ");
	generateGetSlotExpression(name, out);
	println(out, ";");
	print(out, "return (value == null) ? null : value.getOid();");
	endMethodBody(out);
    }

    protected void generateCheckDisconnected(DomainClass domClass, PrintWriter out) {
	newline(out);
	printMethod(out, "protected", "void", "checkDisconnected");
	startMethodBody(out);

	if (domClass.hasSuperclass()) {
	    println(out, "super.checkDisconnected();");
	}

	Iterator<Role> roleSlotsIter = domClass.getRoleSlots();
	while (roleSlotsIter.hasNext()) {
	    Role role = roleSlotsIter.next();

	    if (role.getName() != null) {
		onNewline(out);

		print(out, "if (");
		if (role.getMultiplicityUpper() == 1) {
		    print(out, "has");
		} else {
		    print(out, "hasAny");
		}
		print(out, capitalize(role.getName()));
		println(out, "()) handleAttemptToDeleteConnectedObject();");
	    }
	}

	endMethodBody(out);
    }

    // -----------------------------------------------------------------------------------
    // code related to the database reading/writing

    protected void generateDatabaseReader(DomainClass domClass, PrintWriter out) {
	newline(out);
	printMethod(out, "protected", "void", "readSlotsFromResultSet", makeArg("java.sql.ResultSet", "rs"), makeArg("int",
		"txNumber"));
	print(out, " throws java.sql.SQLException");
	startMethodBody(out);

	if (domClass.hasSuperclass()) {
	    println(out, "super.readSlotsFromResultSet(rs, txNumber);");
	}

	for (Slot slot : domClass.getSlotsList()) {
	    generateOneSlotRsReader(out, slot.getName(), slot.getSlotType());
	}

	for (Role role : domClass.getRoleSlotsList()) {
	    if ((role.getName() != null) && (role.getMultiplicityUpper() == 1)) {
		generateOneRoleSlotRsReader(out, role.getName());
	    }
	}

	endMethodBody(out);
    }

    protected void generateOneSlotRsReader(PrintWriter out, String name, ValueType type) {
	onNewline(out);
	print(out, "set$");
	print(out, name);
	print(out, "(");
	printRsReaderExpressions(out, type, DbUtil.convertToDBStyle(name), 0);
	print(out, ", txNumber);");
    }

    protected int printRsReaderExpressions(PrintWriter out, ValueType vt, String colBaseName, int colNum) {
	if (vt.isBuiltin()) {
	    printBuiltinReadExpression(out, vt, colBaseName, colNum);
	    return colNum + 1;
	}

	for (ExternalizationElement extElem : vt.getExternalizationElements()) {
	    colNum = printRsReaderExpressions(out, extElem.getType(), colBaseName, colNum);
	}

	return colNum;
    }

    protected void generateOneRoleSlotRsReader(PrintWriter out, String name) {
	onNewline(out);

	print(out, "this.");
	print(out, name);
	print(out, ".persistentLoad(");

	print(out, RESULT_SET_READER_CLASS);
	print(out, ".readDomainObject(rs, \"OID_");
	print(out, DbUtil.convertToDBStyle(name));
	print(out, "\"), txNumber);");
    }

    protected void printBuiltinReadExpression(PrintWriter out, ValueType vt, String colBaseName, int colNum) {
	print(out, RESULT_SET_READER_CLASS);
	print(out, ".read");

	if (vt.isEnum()) {
	    print(out, "Enum(");
	    print(out, vt.getFullname());
	    print(out, ".class, ");
	} else {
	    print(out, vt.getDomainName());
	    print(out, "(");
	}

	print(out, "rs, \"");
	print(out, colBaseName);
	if (colNum > 0) {
	    print(out, "__" + colNum);
	}
	print(out, "\")");
    }

    protected void generateSlotDeclaration(PrintWriter out, String type, String name) {
	printWords(out, "private", type, name);
	println(out, ";");
    }
}
