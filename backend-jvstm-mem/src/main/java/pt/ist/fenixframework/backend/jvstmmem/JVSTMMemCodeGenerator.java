package pt.ist.fenixframework.backend.jvstmmem;

import java.io.PrintWriter;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.atomic.DefaultContextFactory;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

public class JVSTMMemCodeGenerator extends IndexesCodeGenerator {

    public JVSTMMemCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
        String collectionName = compArgs.getParams().get(COLLECTION_CLASS_NAME_KEY);
        if (collectionName == null || collectionName.isEmpty()) {
	    setCollectionToUse("pt.ist.fenixframework.adt.bplustree.BPlusTree");
	}
    }

    @Override
    protected String getDomainClassRoot() {
	return JVSTMMemDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
	return JVSTMMemBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
	return JVSTMMemConfig.class.getName();
    }

    @Override
    protected Class<? extends ContextFactory> getAtomicContextFactoryClass() {
	return DefaultContextFactory.class;
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
	super.generateFilePreamble(subPackageName, out);
	println(out, "import jvstm.VBox;");
	newline(out);
    }

    @Override
    protected void generateSlot(Slot slot, PrintWriter out) {
	onNewline(out);
	PrimitiveToWrapperEntry w = findWrapperEntry(slot.getTypeName());
	String defaultValue = w != null ? w.defaultPrimitiveValue : "null";
	printWords(out, "private", getVBoxType(slot), slot.getName(), "= new", getVBoxType(slot), "(" + defaultValue + ")");
	print(out, ";");
    }

    @Override
    protected void generateRoleSlot(Role role, PrintWriter out) {
	onNewline(out);
	if (role.getMultiplicityUpper() == 1) {
	    PrimitiveToWrapperEntry w = findWrapperEntry(getTypeFullName(role.getType()));
	    String defaultValue = w != null ? w.defaultPrimitiveValue : "null";
	    String t = makeGenericType("VBox", getReferenceType(getTypeFullName(role.getType())));
	    printWords(out, "private", t, role.getName(), "= new", t, "(" + defaultValue + ")");
	} else {
	    printWords(out, "private", getDefaultCollectionFor(role), role.getName());
	}
	println(out, ";");
    }

    private String getVBoxType(Slot slot) {
	return makeGenericType("VBox", getReferenceType(slot.getTypeName()));
    }

    @Override
    protected void generateRoleSlotMethodsMultOneGetter(String slotName, String typeName, PrintWriter out) {
	generateVBoxSlotGetter("get" + capitalize(slotName), "get", slotName, typeName, out);
    }

    @Override
    protected void generateSlotAccessors(Slot slot, PrintWriter out) {
	generateVBoxSlotGetter("get" + capitalize(slot.getName()), "get", slot.getName(), slot.getTypeName(), out);
	generateVBoxSlotSetter(slot, out);
    }

    protected void generateVBoxSlotGetter(String methodName, String accessToVBox, String name, String typeName, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", typeName, methodName);
	startMethodBody(out);
	generateGetterDAPStatement(dC, name, typeName, out);//DAP read stats update statement
	printWords(out, "return", getSlotExpression(name) + "." + accessToVBox + "();");
	endMethodBody(out);
    }

    protected void generateVBoxSlotSetter(Slot slot, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", "void", "set" + capitalize(slot.getName()), makeArg(slot.getTypeName(), slot.getName()));
	startMethodBody(out);

	generateSetterDAPStatement(dC, slot.getName(), slot.getTypeName(), out);//DAP write stats update statement
	generateSetterTxIntrospectorStatement(slot, out); // TxIntrospector

	printWords(out, getSlotExpression(slot.getName()) + ".put(" + slot.getName() + ");");
	endMethodBody(out);
    }

    @Override
    protected String getNewRoleStarSlotExpression(Role role) {
	StringBuilder buf = new StringBuilder();

	// generate the relation aware collection
	buf.append("new ");
	buf.append(getDefaultCollectionFor(role));
	buf.append("()");

	return buf.toString();
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {

	String typeName = getTypeFullName(role.getType());
	String slotName = role.getName();
	String capitalizedSlotName = capitalize(slotName);
	String methodModifiers = getMethodModifiers();
	boolean isIndexed = role.isIndexed();

	generateRoleSlotMethodsMultStarGetter("get" + capitalize(role.getName()), role, out);
        
        if (isIndexed) {
            generateRoleSlotMethodsMultStarIndexed(role, out, methodModifiers, capitalizedSlotName, "get" + capitalize(role.getName()), typeName, slotName, false);
        }

	generateRoleSlotMethodsMultStarSetter(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
	generateRoleSlotMethodsMultStarRemover(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
	generateRoleSlotMethodsMultStarSet(role, out, methodModifiers, capitalizedSlotName, typeName);
	generateRoleSlotMethodsMultStarCount(role, out, methodModifiers, capitalizedSlotName);
	generateRoleSlotMethodsMultStarHasAnyChild(role, out, methodModifiers, capitalizedSlotName);
	generateRoleSlotMethodsMultStarHasChild(role, out, methodModifiers, capitalizedSlotName, typeName, slotName);
	generateIteratorMethod(role, out);
    }

    protected void generateRoleSlotMethodsMultStarGetter(String methodName, Role role, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", getSetTypeDeclarationFor(role), methodName);
	startMethodBody(out);
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
	print(out, "return new ");
	print(out, getRelationAwareTypeFor(role));
	print(out, "((");
	print(out, getTypeFullName(role.getOtherRole().getType()));
	print(out, ") this, ");
	print(out, getRelationSlotNameFor(role));
	print(out, ", this.");
	print(out, role.getName());
	print(out, ", keyFunction$$");
	print(out, role.getName());
	print(out, ");");
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

    protected void generateRoleSlotMethodsMultStarSet(Role role, PrintWriter out, String methodModifiers,
	    String capitalizedSlotName, String typeName) {
	newline(out);
	printMethod(out, methodModifiers, makeGenericType("java.util.Set", typeName), "get" + capitalizedSlotName + "Set");
	startMethodBody(out);

	generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
	print(out, "return get" + capitalizedSlotName + "();");
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

    protected void generateRoleSlotMethodsMultStarHasChild(Role role, PrintWriter out, String methodModifiers,
	    String capitalizedSlotName, String typeName, String slotName) {
	newline(out);
	printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName, makeArg(typeName, slotName));
	startMethodBody(out);

	generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
	printWords(out, "return get" + capitalizedSlotName + "().contains(" + slotName + ");");
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
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
	// call the DAP CodeGen which is overridden in this method
	generateGetterDAPStatement(dC, slotName, typeName, out);
	printWords(out, "return", getSlotExpression(slotName) + ".get();");
    }

    @Override
    protected void generateStaticRoleSlotsMultOne(Role role, Role otherRole, PrintWriter out) {
	printMethod(out, "public", getTypeFullName(role.getType()), "getValue", makeArg(getTypeFullName(otherRole.getType()), "o1"));
	startMethodBody(out);
	printWords(out, "return", "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + ".get();");
	endMethodBody(out);

	printMethod(out, "public", "void",  "setValue",
		makeArg(getTypeFullName(otherRole.getType()), "o1"),
		makeArg(getTypeFullName(role.getType()), "o2"));
	startMethodBody(out);
	printWords(out, "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + ".put(o2);");
	endMethodBody(out);
    }

}
