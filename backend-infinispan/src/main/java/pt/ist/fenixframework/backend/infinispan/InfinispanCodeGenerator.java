package pt.ist.fenixframework.backend.infinispan;

import java.io.PrintWriter;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.atomic.DefaultContextFactory;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;
import pt.ist.fenixframework.dml.ValueType;
import pt.ist.fenixframework.dml.ValueTypeSerializationGenerator;

public class InfinispanCodeGenerator extends IndexesCodeGenerator {

    protected static final String VT_SERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.SERIALIZATION_METHOD_PREFIX;

    protected static final String VT_DESERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.DESERIALIZATION_METHOD_PREFIX;

    public InfinispanCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
        String collectionName = compArgs.getParams().get(COLLECTION_CLASS_NAME_KEY);
        if (collectionName == null || collectionName.isEmpty()) {
            setCollectionToUse("pt.ist.fenixframework.adt.bplustree.BPlusTree");
        }
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
        println(out, "import " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ";");
        println(out, "import static " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ".*;");
        newline(out);
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        generateStaticSlots(domClass, out);
        newline(out);

        generateInitInstance(domClass, out);

        generateDefaultConstructor(domClass, out);
        generateSlotsAccessors(domClass, out);
        generateRoleSlotsMethods(domClass.getRoleSlots(), out);
    }

    @Override
    protected void generateInitInstanceMethodBody(DomainClass domClass, PrintWriter out) { }

    protected void generateDefaultConstructor(DomainClass domClass, PrintWriter out) {
        printMethod(out, "public", "", domClass.getBaseName());
        startMethodBody(out);
        generateBaseClassConstructorsBody(domClass, out);
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

        printMethod(out, "public", "boolean", "add",
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
        newline(out);
        print(out, "return true;");
        endMethodBody(out);
    }

    protected void generateRoleMethodRemove(Role role, Role otherRole, PrintWriter out) {
        boolean multOne = (role.getMultiplicityUpper() == 1);
        
        String otherRoleTypeFullName = getTypeFullName(otherRole.getType());
        String roleTypeFullName = getTypeFullName(role.getType());

        printMethod(out, "public", "boolean", "remove",
                    makeArg(otherRoleTypeFullName, "o1"),
                    makeArg(roleTypeFullName, "o2"));
        startMethodBody(out);
        print(out, "if (o1 != null)");
        newBlock(out);
        print(out, "o1.set" + capitalize(role.getName()) + "$unidirectional(null);");
        closeBlock(out, false);
        newline(out);
        print(out, "return true;");
        endMethodBody(out);
    }

    @Override
    protected void generateSlotAccessors(Slot slot, PrintWriter out) {
        generateInfinispanGetter(slot, out);
        // also generate the get shadow methods
        if (generateShadowMethods()) {
            generateInfinispanShadowGetter(slot, out);
            generateInfinispanRegisterGet(slot.getName(), "registerGet" + capitalize(slot.getName()), out);
        }
        generateInfinispanSetter(slot, out);
    }

    protected void generateInfinispanGetter( Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", slot.getTypeName(), "get" + capitalize(slot.getName()));
        startMethodBody(out);
        generateInfinispanGetterBody(slot, out, "cacheGet");
        endMethodBody(out);
    }
    
    protected void generateInfinispanShadowGetter(Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", slot.getTypeName(), "get" + capitalize(slot.getName() + "Shadow"));
        startMethodBody(out);
        generateInfinispanGetterBody(slot, out, "cacheGetShadow");
        endMethodBody(out);
    }
    
    protected void generateInfinispanRegisterGet(String access, String methodName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", "void", methodName);
        startMethodBody(out);
        print(out, "InfinispanBackEnd.getInstance().registerGet(getOid().getFullId() + \":" + access + "\");");
        endMethodBody(out);
    }

    protected void generateInfinispanSetter(Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", "void", "set" + capitalize(slot.getName()), makeArg(slot.getTypeName(), slot.getName()));
        startMethodBody(out);
        generateInfinispanSetterBody(slot, out);
        endMethodBody(out);
    }

    protected void generateInfinispanGetterBody(Slot slot, PrintWriter out, String cacheGetMethod) {
        generateGetterDAPStatement(dC, slot.getName(), slot.getTypeName(), out);//DAP read stats update statement

        println(out, "Object obj = InfinispanBackEnd.getInstance()." + cacheGetMethod + "(getOid().getFullId() + \":" + slot.getName() + "\");");
        
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

    protected void generateInfinispanSetterBody(Slot slot, PrintWriter out) {
        generateSetterDAPStatement(dC, slot.getName(), slot.getTypeName(), out);//DAP write stats update statement
        generateSetterTxIntrospectorStatement(slot, out); // TxIntrospector

        onNewline(out);
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
        
        // also generate the Shadow getter
        if (generateShadowMethods()) {
            newline(out);
            printFinalMethod(out, "public", typeName, "get" + capitalize(slotName) + "Shadow");
            startMethodBody(out);
            generateGetterDAPStatement(dC, slotName, typeName, out);//DAP read stats update statement
        
            println(out, "Object oid = InfinispanBackEnd.getInstance().cacheGetShadow(getOid().getFullId() + \":" + slotName + "\");");
            print(out, "return (oid == null || oid instanceof Externalization.NullClass ? null : (" + typeName + ")InfinispanBackEnd.getInstance().fromOid(oid));");
            endMethodBody(out);

            // and the register method
            newline(out);
            printFinalMethod(out, "public", "void", "registerGet" + capitalize(slotName));
            startMethodBody(out);
            print(out, "InfinispanBackEnd.getInstance().registerGet(getOid().getFullId() + \":" + slotName + "\");");
            endMethodBody(out);
        }
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {

        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String methodModifiers = getMethodModifiers();
        boolean isIndexed = role.isIndexed();

        generateRoleSlotMethodsMultStarGetter(role, out, false);
        if (generateShadowMethods()) {
            generateRoleSlotMethodsMultStarGetter(role, out, true);
            generateRoleSlotMethodsMultStarRegisterShadowGet(role, out);
        }
        
        if (isIndexed) {
            generateRoleSlotMethodsMultStarIndexed(role, out, methodModifiers, capitalizedSlotName, "get" + capitalize(role.getName()), typeName, slotName);
        }
        
        generateRoleSlotMethodsMultStarSetter(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateRoleSlotMethodsMultStarRemover(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateRoleSlotMethodsMultStarSet(role, out, methodModifiers, capitalizedSlotName, typeName);
        generateRoleSlotMethodsMultStarCount(role, out, methodModifiers, capitalizedSlotName);
        generateRoleSlotMethodsMultStarHasAnyChild(role, out, methodModifiers, capitalizedSlotName);
        generateRoleSlotMethodsMultStarHasChild(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
        generateIteratorMethod(role, out);
    }

    protected void generateRoleSlotMethodsMultStarGetter(Role role, PrintWriter out, boolean shadow) {
        newline(out);
        printFinalMethod(out, "public", getSetTypeDeclarationFor(role), "get" + capitalize(role.getName()) + (shadow ? "Shadow" : ""));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement

        String collectionType = getDefaultCollectionFor(role);
        println(out, collectionType + " internalSet;");
        println(out, "Object oid = InfinispanBackEnd.getInstance().cacheGet" + (shadow ? "Shadow" : "") + "(getOid().getFullId() + \":" + role.getName() + "\");");
        print(out, "if (oid == null || oid instanceof Externalization.NullClass)");
        newBlock(out);
        println(out, "internalSet = new " + collectionType + "();");
        print(out, "InfinispanBackEnd.getInstance().cachePut(getOid().getFullId() + \":" + role.getName() + "\", internalSet.getOid());");
        closeBlock(out, false);
        print(out, " else");
        newBlock(out);
        print(out, "internalSet = (" + collectionType + ")InfinispanBackEnd.getInstance().fromOid(oid);");
        // print(out, "// no need to test for null.  The entry must exist for sure.");
        closeBlock(out);
        print(out, "return new ");
        print(out, getRelationAwareTypeFor(role));
        print(out, "((");
        print(out, getTypeFullName(role.getOtherRole().getType()));
        print(out, ") this, ");
        print(out, getRelationSlotNameFor(role));
        print(out, ", internalSet, keyFunction$$");
        print(out, role.getName());
        print(out, ");");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarRegisterShadowGet(Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", "void", "registerGet" + capitalize(role.getName()));
        startMethodBody(out);
        print(out, "InfinispanBackEnd.getInstance().registerGet(getOid().getFullId() + \":" + role.getName() + "\");");
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
	generateIteratorMethod(role, out, "get" + capitalize(role.getName()) + "Iterator", "get" + capitalize(role.getName()) + "()");
    }
    
    protected void generateIteratorMethod(Role role, PrintWriter out, String methodName, final String slotAccessExpression) {
        newline(out);
        printFinalMethod(out, "public", makeGenericType("java.util.Iterator", getTypeFullName(role.getType())), methodName);
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        printWords(out, "return", slotAccessExpression);
        print(out, ".iterator();");
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
        buf.append(", keyFunction$$");
        buf.append(role.getName());
        buf.append(")");

        return buf.toString();
    }

    @Override
    protected String getRoleOneBaseType() {
        return "pt.ist.fenixframework.dml.runtime.Role";
    }

}
