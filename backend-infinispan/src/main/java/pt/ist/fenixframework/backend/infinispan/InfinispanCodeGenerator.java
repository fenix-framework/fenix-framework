package pt.ist.fenixframework.backend.infinispan;

import java.io.PrintWriter;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.atomic.DefaultContextFactory;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DAPCodeGenerator;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;
import pt.ist.fenixframework.dml.ValueType;
import pt.ist.fenixframework.dml.ValueTypeSerializationGenerator;

public class InfinispanCodeGenerator extends IndexesCodeGenerator {
//public class InfinispanCodeGenerator extends DAPCodeGenerator{

    protected static final String VT_SERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.SERIALIZATION_METHOD_PREFIX;

    protected static final String VT_DESERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.DESERIALIZATION_METHOD_PREFIX;

    public InfinispanCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
     }

    @Override
    protected String getDomainClassRoot() {
        return InfinispanDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return InfinispanBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return InfinispanConfig.class.getName();
    }

    @Override
    protected Class<? extends ContextFactory> getAtomicContextFactoryClass() {
        return DefaultContextFactory.class;
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        super.generateFilePreamble(subPackageName, out);
        println(out, "import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;");
        println(out, "import pt.ist.fenixframework.backend.infinispan.OID;");
        println(out, "import pt.ist.fenixframework.core.Externalization;");
        println(out, "import pt.ist.fenixframework.core.adt.bplustree.BPlusTree;");
        println(out, "import " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ";");
        println(out, "import static " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ".*;");
        newline(out);
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        generateStaticSlots(domClass, out);
        newline(out);

        generateInitInstance(domClass, out);
        
        generateDefaultConstructor(domClass.getBaseName(), out);
        generateSlotsAccessors(domClass, out);
        super.generateIndexMethods(domClass, out);
        generateRoleSlotsMethods(domClass.getRoleSlots(), out);
    }

    @Override
    protected void generateInitInstanceBody(DomainClass domClass, PrintWriter out) { }

    protected void generateDefaultConstructor(String classname, PrintWriter out) {
        printMethod(out, "public", "", classname);
        startMethodBody(out);
        endMethodBody(out);
    }

    @Override
    protected void generateStaticRoleSlotsMultOne(Role role, Role otherRole, PrintWriter out) {
        generateRoleMethodAdd(role, otherRole, out);
        generateRoleMethodRemove(role, otherRole, out);
    }

    protected void generateRoleMethodAdd(Role role, Role otherRole, PrintWriter out) {
        boolean multOne = (role.getMultiplicityUpper() == 1);
        
        String otherRoleTypeFullName = getTypeFullName(otherRole.getType());
        String roleTypeFullName = getTypeFullName(role.getType());

        printMethod(out, "public", "void", "add",
                    makeArg(otherRoleTypeFullName, "o1"),
                    makeArg(roleTypeFullName, "o2"),
                    makeArg(makeGenericType("pt.ist.fenixframework.dml.runtime.Relation",
                                            otherRoleTypeFullName,roleTypeFullName), "relation"));
        startMethodBody(out);
        print(out, "if (o1 != null)");
        newBlock(out);
        println(out, roleTypeFullName + " old2 = o1.get" + capitalize(role.getName()) + "();");
        print(out, "if (o2 != old2)");
        newBlock(out);
        println(out, "relation.remove(o1, old2);");
        print(out, "o1.set" + capitalize(role.getName()) + "$unidirectional(o2);");
        closeBlock(out, false);
        closeBlock(out, false);
        endMethodBody(out);
    }

    protected void generateRoleMethodRemove(Role role, Role otherRole, PrintWriter out) {
        boolean multOne = (role.getMultiplicityUpper() == 1);
        
        String otherRoleTypeFullName = getTypeFullName(otherRole.getType());
        String roleTypeFullName = getTypeFullName(role.getType());

        printMethod(out, "public", "void", "remove",
                    makeArg(otherRoleTypeFullName, "o1"),
                    makeArg(roleTypeFullName, "o2"));
        startMethodBody(out);
        print(out, "if (o1 != null)");
        newBlock(out);
        print(out, "o1.set" + capitalize(role.getName()) + "$unidirectional(null);");
        closeBlock(out, false);
        endMethodBody(out);
    }

    @Override
    protected void generateSlotAccessors(DomainClass domainClass, Slot slot, PrintWriter out) {
        generateInfinispanGetter(domainClass, slot, out);
        generateInfinispanSetter(domainClass, slot, out);
    }

    protected void generateInfinispanGetter(DomainClass domainClass, Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", slot.getTypeName(), "get" + capitalize(slot.getName()));
        startMethodBody(out);
        generateInfinispanGetterBody(domainClass, slot, out);
        endMethodBody(out);
    }

    protected void generateInfinispanSetter(DomainClass domainClass, Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", "void", "set" + capitalize(slot.getName()), makeArg(slot.getTypeName(), slot.getName()));
        startMethodBody(out);
        generateInfinispanSetterBody(domainClass, slot, out);
        endMethodBody(out);
    }

    protected void generateInfinispanGetterBody(DomainClass domainClass, Slot slot, PrintWriter out) {
        generateGetterDAPStatement(domainClass, slot.getName(), slot.getTypeName(), out);//DAP read stats update statement

        println(out, "Object obj = InfinispanBackEnd.getInstance().cacheGet(getOid().getFullId() + \":" + slot.getName() + "\");");
        
        String defaultValue;
        PrimitiveToWrapperEntry wrapperEntry = findWrapperEntry(slot.getTypeName());
        if (wrapperEntry != null) { // then it is a primitive type
            defaultValue = wrapperEntry.defaultPrimitiveValue;
        } else {
            defaultValue = "null";
        }
        println(out, "if (obj == null || obj instanceof Externalization.NullClass) { return " + defaultValue + "; }");
        String returnExpression = "return (" + getReferenceType(slot.getTypeName()) + ")";
        ValueType vt = slot.getSlotType();
        if (vt.isBuiltin() || vt.isEnum()) {
            returnExpression += "obj";
        } else {
            returnExpression += VT_DESERIALIZER +
                ValueTypeSerializationGenerator.makeSafeValueTypeName(vt) + "((" +
                getReferenceType(ValueTypeSerializationGenerator.getSerializedFormTypeName(vt)) +
                ")obj)";
        }
        returnExpression += ";";
        print(out, returnExpression);
    }

    protected void generateInfinispanSetterBody(DomainClass domainClass, Slot slot, PrintWriter out) {
        generateSetterDAPStatement(domainClass, slot.getName(), slot.getTypeName(), out);//DAP write stats update statement

        String slotName = slot.getName();
        String setterExpression;
	if (findWrapperEntry(slot.getTypeName()) != null) { // then it is a primitive type
            setterExpression = slotName;
        } else {
            setterExpression = "(" + slotName + " == null ? Externalization.NULL_OBJECT : ";
            ValueType vt = slot.getSlotType();
            if (vt.isBuiltin() || vt.isEnum()) {
                setterExpression += slotName;
            } else { // derived value type must be externalized
                setterExpression +=  VT_SERIALIZER +
                    ValueTypeSerializationGenerator.makeSafeValueTypeName(vt) + "(" + slotName + ")";
            }
            setterExpression += ")";
        }
	super.generateIndexationInSetter(domainClass, slot, out);
        print(out, "InfinispanBackEnd.getInstance().cachePut(getOid().getFullId() + \":" + slotName
              + "\", " + setterExpression + ");");
    }

    @Override
    protected void generateRoleSlotMethodsMultOne(Role role, PrintWriter out) {
        super.generateRoleSlotMethodsMultOne(role, out);
        generateRoleSlotMethodsMultOneInternalSetter(role, out);
    }

    protected void generateRoleSlotMethodsMultOneInternalSetter(Role role, PrintWriter out) {
        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String setterName = "set" + capitalizedSlotName;

        String methodModifiers = getMethodModifiers();

        // internal setter, which does not inform the relation
        newline(out);
        printMethod(out, methodModifiers, "void", setterName + "$unidirectional", makeArg(typeName,
                                                                                          slotName));
        startMethodBody(out);
        print(out, "InfinispanBackEnd.getInstance().cachePut(getOid().getFullId() + \":" + slotName +
              "\", (" + slotName + " == null ? Externalization.NULL_OBJECT : " + slotName +
              ".getOid()));");
        endMethodBody(out);
    }

    @Override
    protected void generateRoleSlotMethodsMultOneGetter(String slotName, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", typeName, "get" + capitalize(slotName));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, slotName, typeName, out);//DAP read stats update statement
        
        println(out, "Object oid = InfinispanBackEnd.getInstance().cacheGet(getOid().getFullId() + \":" + slotName + "\");");
        print(out, "return (oid == null || oid instanceof Externalization.NullClass ? null : (" + typeName + ")InfinispanBackEnd.getInstance().fromOid(oid));");
        endMethodBody(out);
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {

        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String methodModifiers = getMethodModifiers();

        generateRoleSlotMethodsMultStarGetter(role, out);
        generateRoleSlotMethodsMultStarSetter(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateRoleSlotMethodsMultStarRemover(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateRoleSlotMethodsMultStarSet(role, out, methodModifiers, capitalizedSlotName, typeName);
        generateRoleSlotMethodsMultStarCount(role, out, methodModifiers, capitalizedSlotName);
        generateRoleSlotMethodsMultStarHasAnyChild(role, out, methodModifiers, capitalizedSlotName);
        generateRoleSlotMethodsMultStarHasChild(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateIteratorMethod(role, out);
    }

    protected void generateRoleSlotMethodsMultStarGetter(Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", getSetTypeDeclarationFor(role), "get" + capitalize(role.getName()));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement

        println(out, "BPlusTree internalSet;");
        println(out, "Object oid = InfinispanBackEnd.getInstance().cacheGet(getOid().getFullId() + \":" + role.getName() + "\");");
        print(out, "if (oid == null || oid instanceof Externalization.NullClass)");
        newBlock(out);
        println(out, "internalSet = new BPlusTree();");
        print(out, "InfinispanBackEnd.getInstance().cachePut(getOid().getFullId() + \":" + role.getName() + "\", internalSet.getOid());");
        closeBlock(out, false);
        print(out, " else");
        newBlock(out);
        print(out, "internalSet = (BPlusTree)InfinispanBackEnd.getInstance().fromOid(oid);");
        // print(out, "// no need to test for null.  The entry must exist for sure.");
        closeBlock(out);
        print(out, "return new " + getRelationAwareBaseTypeFor(role) + "(this, " + getRelationSlotNameFor(role) + ", internalSet);");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarSetter(Role role, PrintWriter out, String methodModifiers,
                                                         String capitalizedSlotName, String typeName, String slotName) {
        newline(out);
        String adderMethodName = getAdderMethodName(role);
        printFinalMethod(out, methodModifiers, "void", adderMethodName,
                         makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationAddMethodCall(role, slotName, null, out);
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarRemover(Role role, PrintWriter out, String methodModifiers,
                                                          String capitalizedSlotName, String typeName, String slotName) {
        String removerMethodName = getRemoverMethodName(role);

        newline(out);
        printMethod(out, methodModifiers, "void", removerMethodName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationRemoveMethodCall(role, slotName, out);
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarCount(Role role, PrintWriter out,
                                                        String methodModifiers, String capitalizedSlotName) {
        newline(out);
        printMethod(out, methodModifiers, "int", "get" + capitalizedSlotName + "Count");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        
        printWords(out, "return get" + capitalizedSlotName + "().size();");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasAnyChild(Role role, PrintWriter out,
                                                              String methodModifiers, String capitalizedSlotName) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "hasAny" + capitalizedSlotName);
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        
        printWords(out, "return (get" + capitalizedSlotName + "().size() != 0);");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasChild(Role role, PrintWriter out, String methodModifiers,
                                                           String capitalizedSlotName, String typeName, String slotName) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName, makeArg(typeName, slotName));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        
        printWords(out, "return get" + capitalizedSlotName + "().contains(" + slotName + ");");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarSet(Role role, PrintWriter out, String methodModifiers,
                                                      String capitalizedSlotName, String typeName) {
        newline(out);
        printMethod(out, methodModifiers, makeGenericType("java.util.Set", typeName), "get" + capitalizedSlotName + "Set");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        
        print(out, "return get" + capitalizedSlotName + "();");
        endMethodBody(out);
    }

    protected void generateIteratorMethod(Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", makeGenericType("java.util.Iterator", getTypeFullName(role.getType())), "get"
                         + capitalize(role.getName()) + "Iterator");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        
        printWords(out, "return get" + capitalize(role.getName()) + "().iterator();");
        endMethodBody(out);
    }

    @Override
    protected String getNewRoleStarSlotExpression(Role role) {
        return getNewRoleStarSlotExpressionWithBackingSet(role, role.getName());
    }

    protected String getNewRoleStarSlotExpressionWithBackingSet(Role role, String theSet) {
        StringBuilder buf = new StringBuilder();

        // generate the relation aware collection
        String thisType = getTypeFullName(role.getOtherRole().getType());
        buf.append("new ");
        buf.append(getRelationAwareTypeFor(role));
        buf.append("(");
        buf.append(theSet);
        buf.append(", ");
        buf.append("(");
        buf.append(thisType);
        buf.append(")this, ");
        buf.append(getRelationSlotNameFor(role));
        buf.append(")");

        return buf.toString();
    }

    @Override
    protected String getRoleOneBaseType() {
        return "pt.ist.fenixframework.dml.runtime.Role";
    }

    @Override
    protected String getRelationAwareBaseTypeFor(Role role) {
        // FIXME: handle other types of collections other than sets
        return "pt.ist.fenixframework.backend.infinispan.RelationSet";
    }

}
