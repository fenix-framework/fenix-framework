/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import java.io.PrintWriter;

import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;

public class JVSTMCodeGenerator extends IndexesCodeGenerator {

    public JVSTMCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
        String collectionName = compArgs.getParams().get(COLLECTION_CLASS_NAME_KEY);
        if (collectionName == null || collectionName.isEmpty()) {
            setCollectionToUse("pt.ist.fenixframework.adt.bplustree.BPlusTree");
        }
    }

    @Override
    protected String getBackEndName() {
        return JVSTMBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return JVSTMConfig.class.getName();
    }

    @Override
    protected String getDomainClassRoot() {
        return JVSTMDomainObject.class.getName();
    }

    @Override
    protected void generateSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        printWords(out, "private", getBoxType(slot), slot.getName());
        print(out, ";");
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        super.generateFilePreamble(subPackageName, out);
        println(out, "import pt.ist.fenixframework.FenixFramework;");
        println(out, "import pt.ist.fenixframework.backend.jvstm.pstm.VBox;");
        newline(out);
    }

    //    // smf: maybe to delete? /replace with getboxtype or similar?
//    protected String getVBoxType(Slot slot) {
//        return makeGenericType("VBox", getReferenceType(slot.getTypeName()));
//    }
//
    protected String getBoxBaseType() {
        return "VBox";
    }

    protected String getBoxType(String elemType) {
        return makeGenericType(getBoxBaseType(), elemType);
    }

    protected String getBoxType(Slot slot) {
        return getBoxType(getReferenceType(slot.getTypeName()));
    }

    protected String getBoxType(Role role) {
        return getBoxType(getTypeFullName(role.getType()));
    }

    protected String getNewSlotExpression(String slotName, boolean isReference) {
        return "VBox.makeNew(this, \"" + slotName + "\", allocateOnly, " + isReference + ")";
    }

    protected String getNewSlotExpression(Slot slot) {
        return getNewSlotExpression(slot.getName(), false);
    }

    protected String decideRoleVBoxName(Role role) {
        return (role.getMultiplicityUpper() == 1 ? role.getName() : makeDomainBasedMapVBoxInternalName(role));
    }

    protected String getNewRoleExpression(Role role) {
        return getNewSlotExpression(decideRoleVBoxName(role), true);
    }

    protected String getSlotGetterExpression(String slotName) {
        return getSlotExpression(slotName) + ".get()";
    }

    @Override
    protected String getSlotSetterExpression(Slot slot, String value) {
        return getSlotExpression(slot.getName()) + ".put(" + value + ")";
    }

    @Override
    protected void generateRoleSlot(Role role, PrintWriter out) {
        onNewline(out);
        if (role.getMultiplicityUpper() == 1) {
            printWords(out, "private", getBoxType(role), role.getName());
        } else {
            printWords(out, "private", makeGenericType(getBoxBaseType(), getDefaultCollectionFor(role)),
                    makeDomainBasedMapVBoxInternalName(role) + ";");
            onNewline(out);
            printWords(out, "private", getRelationAwareTypeFor(role), role.getName());
        }
        println(out, ";");
    }

    @Override
    protected void generateInitSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        printWords(out, slot.getName());
        print(out, " = ");
        print(out, getNewSlotExpression(slot));
        print(out, ";");

        // initialize primitive slots with their default value
        generateInitializePrimitiveIfNeeded(slot, out);
    }

    // smf: It might make sense to define this method in the CodeGenerator class 
    protected void generateInitializePrimitiveIfNeeded(Slot slot, PrintWriter out) {
        PrimitiveToWrapperEntry wrapperEntry = findWrapperEntry(slot.getTypeName());
        String initialValue = wrapperEntry == null ? null : wrapperEntry.defaultPrimitiveValue;
        // we always set the value to ensure that the box goes to the write-set.
        generateNewSlotInitialization(slot.getName(), initialValue, false, out);
    }

    protected void generateNewSlotInitialization(String slotName, String slotValue, boolean bypassCheckAllocateOnly,
            PrintWriter out) {
        onNewline(out);
        if (!bypassCheckAllocateOnly) {
            print(out, "if (!allocateOnly) ");
        }
        print(out, "this.");
        print(out, slotName + ".put(");
        println(out, slotValue + ");");
    }

    @Override
    protected void generateInitRoleSlot(Role role, PrintWriter out) {
        onNewline(out);
        printWords(out, decideRoleVBoxName(role));
        print(out, " = ");
        print(out, getNewRoleExpression(role));
        print(out, ";");

        if (role.getMultiplicityUpper() == 1) {
            generateInitRoleSlotMulOne(role, out);
        } else {
            generateInitRoleSlotMulStar(role, out);
        }
    }

    protected void generateInitRoleSlotMulOne(Role role, PrintWriter out) {
        // initialize slots with their default value
        // we always set the value to ensure that the box goes to the write-set.
        generateNewSlotInitialization(role.getName(), "null", false, out);

    }

    protected void generateInitRoleSlotMulStar(Role role, PrintWriter out) {
        onNewline(out);
        print(out, "if (allocateOnly)");
        newBlock(out);
        // create RelationAwareSet with the MAPPER_GETTER
        generateInitRoleSlotStarAllocateOnlyTrue(role, out);
        closeBlock(out, false);
        print(out, " else");
        newBlock(out);
        // create RelationAwareSet with the internalMap instance
        generateInitRoleSlotStarAllocateOnlyFalse(role, out);
        closeBlock(out);
    }

    @Override
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        printWords(out, "return", getSlotGetterExpression(slotName) + ";");
    }

    protected void generateRoleStarGetterBody(String slotName, String typeName, PrintWriter out) {
        printWords(out, "return", getSlotExpression(slotName) + ";");
    }

    protected String makeDomainBasedMapInternalGetterName(Role role) {
        return "internal$get" + capitalize(role.getName()) + "$collection";
    }

    protected String makeDomainBasedMapVBoxInternalName(Role role) {
        return "internal$" + role.getName() + "$collectionBox";
    }

    protected void generateInitRoleSlotStarAllocateOnlyTrue(Role role, PrintWriter out) {
        String getterType = getDefaultCollectionGetterFor(role);
        String roleName = role.getName();
        print(out, "final " + getterType + " mapGetter" + " = new " + getterType + "()");
        newBlock(out);
        printMethod(out, "public final", getDefaultCollectionFor(role), "get");
        print(out, " { return " + makeDomainBasedMapInternalGetterName(role) + "(); }");
        closeBlock(out, false);
        println(out, ";");
        print(out, roleName + " = ");
        print(out, getInitRoleSlotStarExpression(role, "mapGetter", out) + ";");
    }

    protected void generateInitRoleSlotStarAllocateOnlyFalse(Role role, PrintWriter out) {
        String collectionToUse = getDefaultCollectionFor(role);
        println(out, collectionToUse + " internalMap = new " + collectionToUse + "();");
        generateNewSlotInitialization(makeDomainBasedMapVBoxInternalName(role), "internalMap", true, out);
        print(out, role.getName() + " = ");
        print(out, getInitRoleSlotStarExpression(role, "internalMap", out) + ";");
    }

    protected String getInitRoleSlotStarExpression(Role role, String initExpression, PrintWriter out) {
        StringBuilder buf = new StringBuilder();

        // generate the relation aware collection
        String thisType = getTypeFullName(role.getOtherRole().getType());
        buf.append("new ");
        buf.append(getRelationAwareTypeFor(role));
        buf.append("((");
        buf.append(thisType);
        buf.append(")this, ");
        buf.append(getRelationMethodNameFor(role));
        buf.append(", " + initExpression);
        buf.append(", keyFunction$$");
        buf.append(role.getName());
        buf.append(")");

        return buf.toString();
    }

    @Override
    protected void generateStaticRoleSlotsMultOneGetterBody(Role role, Role otherRole, PrintWriter out) {
        printWords(out, "return", "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + ".get();");
    }

    @Override
    protected void generateStaticRoleSlotsMultOneSetterBody(Role role, Role otherRole, PrintWriter out) {
        printWords(out, "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + ".put(o2);");
    }

    @Override
    protected void generateRoleSlotMethodsMultStarGetters(Role role, PrintWriter out) {
        generateRoleSlotMethodsMultStarInternalGetter(makeDomainBasedMapInternalGetterName(role), role, out);

        generateRoleSlotMethodsMultStarGetter("get" + capitalize(role.getName()) + "Set", role, out);
    }

    protected void generateRoleSlotMethodsMultStarInternalGetter(String getterName, Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", getDefaultCollectionFor(role), getterName);
        startMethodBody(out);

        print(out, "return (" + getDefaultCollectionFor(role) + ")" + getSlotGetterExpression(decideRoleVBoxName(role)) + ";");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarGetter(String methodName, Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", getSetTypeDeclarationFor(role), methodName);
        startMethodBody(out);
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);//DAP read stats update statement
        generateRoleStarGetterBody(role.getName(), getTypeFullName(role.getType()), out);
        endMethodBody(out);
    }

}
