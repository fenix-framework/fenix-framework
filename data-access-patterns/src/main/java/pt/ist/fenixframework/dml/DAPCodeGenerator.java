package pt.ist.fenixframework.dml;

import java.io.*;
import java.net.*;

import java.util.Iterator;

import pt.ist.dap.implementation.DAPConfig;
import pt.ist.fenixframework.FenixFramework;

/**
 * This code generator adds the possibility of collecting information about the data access
 * patterns performed by any target application built with the fenix-framework. To do so, it adds,
 * at the start of each setter and getter method an invocation to a static method in the DAP framework
 * that updates the statistical information regarding that particular read/write access operation.
 * @author syg
 */
public class DAPCodeGenerator extends DefaultCodeGenerator {
    
    public static final String DAP_COMPILE_ARG = "pt.ist.dap";
    public static final String DAP_COMPILE_ENABLE = "on";
    private static boolean DAP_ENABLED = false;
    protected static DomainClass dC = null;

    public DAPCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
        //System.out.println("\n\n__DAPCodeGenerator__\n\n");
        
        String state = compArgs.getParams().get(DAP_COMPILE_ARG);
        
        if (state != null) DAP_ENABLED = state.equals(DAP_COMPILE_ENABLE);
    }

    public static final String getGetterDAPStatement(DomainClass domainClass, String slotName, String typeName) {
        //System.out.println("\tGenerating getter DAP statement for " + domainClass.getFullName() + "."  + slotName + " of type " + typeName);
        if (DAP_ENABLED) return "pt.ist.dap.implementation.simple.SimpleContextManager.updateReadStatisticsWithoutContext(\"" + domainClass.getFullName() + "\", \"" + slotName + "\");";
        else return "";
    }

    public static final String getSetterDAPStatement(DomainClass domainClass, String slotName, String typeName) {
        //System.out.println("\tGenerating setter DAP statement for " + domainClass.getFullName() + "."  + slotName + " of type " + typeName);
        if (DAP_ENABLED) return "pt.ist.dap.implementation.simple.SimpleContextManager.updateWriteStatisticsWithoutContext(\"" + domainClass.getFullName() + "\", \"" + slotName + "\");";
        else return "";
    }
    
    protected final void generateGetterDAPStatement(DomainClass domainClass, String slotName, String typeName, PrintWriter out) {
        if (DAP_ENABLED) println(out, getGetterDAPStatement(domainClass, slotName, typeName));
    }
    
    protected final void generateSetterDAPStatement(DomainClass domainClass, String slotName, String typeName, PrintWriter out) {
        if (DAP_ENABLED) println(out, getSetterDAPStatement(domainClass, slotName, typeName));
    }
    
    @Override
    protected void generateClasses(Iterator classesIter) {
        //System.out.println("DAPCodeGenerator::generateClasses");
        while (classesIter.hasNext()) {
            dC = (DomainClass) classesIter.next();
            //System.out.println("\nGenerating domainClass " + dC.getFullName());
            generateOneClass(dC);
        }
    }
    
    @Override
    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        //System.out.println("\tGenerating getter body for " + slotName + " of type " + typeName);
        generateGetterDAPStatement(dC, slotName, typeName, out);
        //if (DAP_ENABLED) {
            //print(out, "pt.ist.dap.implementation.simple.SimpleContextManager.updateReadStatisticsWithoutContext(\""+ dC.getFullName() +"\", \"");
            //print(out, slotName);
            //println(out, "\");");
        //}
        super.generateGetterBody(slotName, typeName, out);
    }
    
    @Override
    protected void generateSetterBody(DomainClass domainClass, String setterName, Slot slot, PrintWriter out) {
        //System.out.println("\tGenerating setter body named " + setterName + "() for slot " + slot.getName() + " of type " + slot.getTypeName());
        generateSetterDAPStatement(domainClass, slot.getName(), slot.getTypeName(), out);
        //if (DAP_ENABLED) {
            //generateSetterDAPStatement(domainClass, slot.getName(), slot.getTypeName(), out);
            //print(out, "pt.ist.dap.implementation.simple.SimpleContextManager.updateWriteStatisticsWithoutContext(\""+ domainClass.getFullName() +"\", \"");
            //print(out, slot.getName());
            //println(out, "\");");
        //}
        super.generateSetterBody(domainClass, setterName, slot, out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultOneHas(Role role, PrintWriter out) {
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String getterName = "get" + capitalizedSlotName;
        String methodModifiers = getMethodModifiers();

        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName);
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return (");
        print(out, getterName);
        print(out, "() != null);");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultOneRemove(Role role, PrintWriter out) {
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String setterName = "set" + capitalizedSlotName;
        String methodModifiers = getMethodModifiers();

        newline(out);
        printMethod(out, methodModifiers, "void", "remove" + capitalizedSlotName);
        startMethodBody(out);
        
        generateSetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, setterName);
        print(out, "(null);");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultStarCount(Role role, PrintWriter out, String methodModifiers, String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "int", "get" + capitalizedSlotName + "Count");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ".size();");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultStarHasAnyChild(Role role, PrintWriter out, String methodModifiers, String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "hasAny" + capitalizedSlotName);
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return (! ");
        print(out, slotAccessExpression);
        print(out, ".isEmpty());");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultStarHasChild(Role role, PrintWriter out, String methodModifiers, String capitalizedSlotName, String slotAccessExpression, String typeName, String slotName, boolean isIndexed, String indexGetterCall) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName, makeArg(typeName, slotName));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ".");
        print(out, (isIndexed ? "containsKey(" : "contains("));
        print(out, slotName);
        if (isIndexed) {
            print(out, ".");
            print(out, indexGetterCall);
        }
        print(out, ");");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRoleSlotMethodsMultStarSet(Role role, PrintWriter out, String methodModifiers, String capitalizedSlotName, String slotAccessExpression, String slotName, String typeName) {
        newline(out);
        printMethod(out, methodModifiers, makeGenericType("java.util.Set", typeName), "get" + capitalizedSlotName + "Set");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ";");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    @Override
    protected void generateRelationGetter(String getterName, String valueToReturn, Role role, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", typeName, getterName);
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        print(out, "return ");
        print(out, valueToReturn);
        print(out, ";");
        endMethodBody(out);
    }
    
    //N.B: the only difference between this method and the one in the super CodeGenerator is the single invocation to the DAP framework
    protected void generateIteratorMethod(Role role, PrintWriter out, final String slotAccessExpression) {
        newline(out);
        printFinalMethod(out, "public", makeGenericType("java.util.Iterator", getTypeFullName(role.getType())), "get" + capitalize(role.getName()) + "Iterator");
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        
        printWords(out, "return", slotAccessExpression);
        print(out, ".iterator();");
        endMethodBody(out);
    }

    
    
    @Override
    protected void generateRelationAddMethodCall(Role role, String otherArg, String indexParam, PrintWriter out) {
        generateSetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        super.generateRelationAddMethodCall(role, otherArg, indexParam, out);
    }
    
    @Override
    protected void generateRelationRemoveMethodCall(Role role, String otherArg, PrintWriter out) {
        generateSetterDAPStatement(dC, role.getName(), role.getType().getFullName(), out);
        super.generateRelationRemoveMethodCall(role, otherArg, out);
    }
    
}
