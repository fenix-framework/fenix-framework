package pt.ist.fenixframework.backend.jvstmmem;

import java.io.PrintWriter;

import pt.ist.fenixframework.backend.jvstm.JVSTMCodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

public class JVSTMMemCodeGenerator extends JVSTMCodeGenerator {

    public JVSTMMemCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
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
    protected String getBoxBaseType() {
        return "VBox";
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        // skipping call to super to avoid conflicting with VBox import.
        // super.generateFilePreamble(subPackageName, out);
        generatePackageDecl(subPackageName, out);

        println(out, "import jvstm.VBox;");
        newline(out);
    }

    @Override
    protected void generateInitInstanceMethodBody(DomainClass domClass, PrintWriter out) {
        // smf: this method restores the behavior by default in CodeGenerator, undoing what JVSTMCodeGenerator does.  If, as
        // expected, the generation of slot and roleSlots is all moved to the CodeGenerator, then this code may be deleted.
        onNewline(out);

        for (Role role : domClass.getRoleSlotsList()) {
            if (role.getName() != null) {
                generateInitRoleSlot(role, out);
            }
        }

    }

    @Override
    protected void generateInitRoleSlot(Role role, PrintWriter out) {
        // smf: this method restores the behavior by default in CodeGenerator, undoing what JVSTMCodeGenerator does.  If, as
        // expected, the generation of slot and roleSlots is all moved to the CodeGenerator, then this code may be deleted.
        if (role.getMultiplicityUpper() != 1) {
            onNewline(out);
            print(out, role.getName());
            print(out, " = ");
            print(out, getNewRoleStarSlotExpression(role));
            print(out, ";");
        }
    }

    @Override
    protected void generateSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        PrimitiveToWrapperEntry w = findWrapperEntry(slot.getTypeName());
        String defaultValue = w != null ? w.defaultPrimitiveValue : "null";
        printWords(out, "private", getBoxType(slot), slot.getName(), "= new", getBoxType(slot), "(" + defaultValue + ")");
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
//        generateGetterDAPStatement(dC, name, typeName, out);//DAP read stats update statement
        printWords(out, "return", getSlotExpression(name) + "." + accessToVBox + "();");
        endMethodBody(out);
    }

    protected void generateVBoxSlotSetter(Slot slot, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", "void", "set" + capitalize(slot.getName()), makeArg(slot.getTypeName(), slot.getName()));
        startMethodBody(out);

//        generateSetterDAPStatement(dC, slot.getName(), slot.getTypeName(), out);//DAP write stats update statement
//        generateSetterTxIntrospectorStatement(slot, out); // TxIntrospector

        printWords(out, getSlotExpression(slot.getName()) + ".put(" + slot.getName() + ");");
        endMethodBody(out);
    }

    // smf: this method restores the behavior before adding JVSTMCodeGenerator, undoing what JVSTMCodeGenerator does.  This
    // code may be deleted after replacing collections with RelationAwareSets in the generated code.
    @Override
    protected void generateRoleSlotMethodsMultStarGetter(String methodName, Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", getSetTypeDeclarationFor(role), methodName);
        startMethodBody(out);
//        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        print(out, "return new ");
        print(out, getRelationAwareTypeFor(role));
        print(out, "((");
        print(out, getTypeFullName(role.getOtherRole().getType()));
        print(out, ") this, ");
        print(out, getRelationMethodNameFor(role));
        print(out, ", this.");
        print(out, role.getName());
        print(out, ", keyFunction$$");
        print(out, role.getName());
        print(out, ");");
        endMethodBody(out);
    }

    // smf: this method restores the behavior before adding JVSTMCodeGenerator, undoing what JVSTMCodeGenerator does.  This
    // code may be deleted later
    @Override
    protected void generateRoleSlotMethodsMultStarInternalGetter(String getterName, Role role, PrintWriter out) {
    }

    @Override
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        // call the DAP CodeGen which is overridden in this method
//        generateGetterDAPStatement(dC, slotName, typeName, out);
        printWords(out, "return", getSlotExpression(slotName) + ".get();");
    }

}
