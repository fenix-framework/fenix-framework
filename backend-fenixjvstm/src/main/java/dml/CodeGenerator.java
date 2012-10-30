package dml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class CodeGenerator {

    static class PrimitiveToWrapperEntry {
	public final String primitiveType;
	public final String wrapperType;
	public final String defaultPrimitiveValue;

	PrimitiveToWrapperEntry(String primitiveType, String wrapperType, String defaultPrimitiveValue) {
	    this.primitiveType = primitiveType;
	    this.wrapperType = wrapperType;
	    this.defaultPrimitiveValue = defaultPrimitiveValue;
	}
    }

    protected static PrimitiveToWrapperEntry[] primitiveToWrapperTypes = {
	new PrimitiveToWrapperEntry("boolean", "Boolean", "false"),
	new PrimitiveToWrapperEntry("byte", "Byte", "(byte)0"),
	new PrimitiveToWrapperEntry("char", "Character", "'\\u0000'"),
	new PrimitiveToWrapperEntry("short", "Short", "(short)0"),
	new PrimitiveToWrapperEntry("int", "Integer", "0"),
	new PrimitiveToWrapperEntry("float", "Float", "0.0f"),
	new PrimitiveToWrapperEntry("long", "Long", "0L"),
	new PrimitiveToWrapperEntry("double", "Double", "0.0d")
    };

    private CompilerArgs compArgs;
    private DomainModel domainModel;
    private File destDirectory;
    private File destDirectoryBase;

    public CodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        this.compArgs = compArgs;
        this.domainModel = domainModel;
        this.destDirectory = getPackageDirectory(compArgs.destDirectory, compArgs.packageName);
        this.destDirectoryBase = getPackageDirectory((compArgs.destDirectoryBase == null)
                                                     ? compArgs.destDirectory
                                                     : compArgs.destDirectoryBase,
                                                     compArgs.packageName);
    }

    public DomainModel getDomainModel() {
        return domainModel;
    }

    public String getPackageName() {
        return compArgs.packageName;
    }

    protected File getDirectoryFor(String packageName) {
        return getPackageDirectory(destDirectory, packageName);
    }

    protected File getBaseDirectoryFor(String packageName) {
        return getPackageDirectory(destDirectoryBase, packageName);
    }

    protected String getPackagePrefix(String packageName) {
        return packageName + ((packageName.length() > 0) ? "." : "");
    }

    protected String getEntityFullName(DomainEntity domEntity) {
        if (domEntity == null) {
            return null;
        } else {        
            return domEntity.getFullName(getPackageName());
        }
    }

    protected String getTypeFullName(DomainEntity domEntity) {
        return getEntityFullName(domEntity);
    }

    protected String getDomainClassRoot() {
        return "java.lang.Object";
    }

    public void generateCode() {
        generateClasses(getDomainModel().getClasses());
    }

    static interface WriteProcedure {
        public void doIt(PrintWriter out);
    }

    protected void writeToFile(File file, WriteProcedure proc) {
        file.getParentFile().mkdirs();

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            proc.doIt(new PrintWriter(fileWriter, true));
        } catch (IOException ioe) {
            throw new Error("Can't open file " + file);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected void generateClasses(Iterator classesIter) {
        while (classesIter.hasNext()) {
            generateOneClass((DomainClass) classesIter.next());
        }
    }

    protected void generateOneClass(final DomainClass domClass) {
        final String packageName = domClass.getPackageName();
        
        writeToFile(new File(getBaseDirectoryFor(packageName), domClass.getBaseName() + ".java"),
                    new WriteProcedure() {
                        public void doIt(PrintWriter out) {
                            generateFilePreamble(packageName, out);
                            generateBaseClass(domClass, out);
                        }
                    });

        //No need to generate non-base classes since they are already defined in the jar
        if(domClass.getSourceFile().toExternalForm().startsWith("jar:file"))
            return;
        
        File leafClassFile = new File(getDirectoryFor(packageName), domClass.getName() + ".java");
        if (! leafClassFile.exists()) {
            writeToFile(leafClassFile,
                        new WriteProcedure() {
                            public void doIt(PrintWriter out) {
                                generatePublicFilePreamble(packageName, out);
                                generatePublicClass(domClass, out);
                            }
                        });
        }
    }


    static File getPackageDirectory(File destDirectory, String packageName) {
        if ((packageName == null) || (packageName.equals(""))) {
            return destDirectory;
        } else {
            int pos = packageName.indexOf('.');
            String subdir = (pos == -1) ? packageName : packageName.substring(0, pos);
            String restPackageName = (pos == -1) ? "" : packageName.substring(pos + 1);
            return getPackageDirectory(new File(destDirectory, subdir), restPackageName);
        }
    }

    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        generatePackageDecl(subPackageName, out);
    }

    protected void generatePublicFilePreamble(String subPackageName, PrintWriter out) {
        generatePackageDecl(subPackageName, out);
    }

    protected void generatePackageDecl(String subPackageName, PrintWriter out) {
        String topPackageName = getPackageName();

        if ((topPackageName.length() + subPackageName.length()) > 0) {
            printWords(out, "package");
            printWords(out, topPackageName);
            if ((topPackageName.length() > 0) && (subPackageName.length() > 0)) {
                print(out, ".");
            }
            print(out, subPackageName);
            print(out, ";");
            newline(out);
            newline(out);
        }
    }

    protected void generatePublicClass(DomainClass domClass, PrintWriter out) {
        String leafClassName = domClass.getName();
        printWords(out, "public", "class", leafClassName, "extends", domClass.getBaseName());
        newBlock(out);

        generatePublicClassConstructors(leafClassName, out);

        closeBlock(out);
    }

    protected void generatePublicClassConstructors(String className, PrintWriter out) {
        newline(out);
        printMethod(out, "public", "", className);
        startMethodBody(out);
        print(out, "super();");
        endMethodBody(out);
    }


    protected void generateBaseClass(DomainClass domClass, PrintWriter out) {
        printWords(out, "public", "abstract", "class", domClass.getBaseName(), "extends");
        String superclassName = getEntityFullName(domClass.getSuperclass());
        printWords(out, (superclassName == null) ? getDomainClassRoot() : superclassName);

        final List interfacesNames = domClass.getInterfacesNames();
        if (interfacesNames != null && !interfacesNames.isEmpty()) {
            printWords(out, "implements");
            for (final Object ifsn : interfacesNames) {
        	printWords(out, ifsn.toString());
            }
        }
        
        newBlock(out);
        generateBaseClassBody(domClass, out);
        closeBlock(out);
    }


    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        generateStaticSlots(domClass, out);
        newline(out);

        generateSlots(domClass.getSlots(), out);
        newline(out);

        generateRoleSlots(domClass.getRoleSlots(), out);
        newline(out);

        generateInitInstance(domClass, out);

        // constructors
        newline(out);
        printMethod(out, "protected", "", domClass.getBaseName());
        startMethodBody(out);
        generateBaseClassConstructorsBody(domClass, out);
        endMethodBody(out);
        
        // slots getters/setters
        generateSlotsAccessors(domClass.getSlots(), out);
        
        // roles methods
        generateRoleSlotsMethods(domClass.getRoleSlots(), out);

        // generate slot consistency predicates
        generateSlotConsistencyPredicates(domClass, out);
    }

    protected void generateBaseClassConstructorsBody(DomainClass domClass, PrintWriter out) {
	print(out, "super();");
    }

    protected void generateSlots(Iterator slotsIter, PrintWriter out) {
        while (slotsIter.hasNext()) {
            generateSlot((Slot) slotsIter.next(), out);
        }
    }


    protected void generateSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        printWords(out, "private", getBoxType(slot), slot.getName());
        print(out, ";");
    }


    protected PrimitiveToWrapperEntry findWrapperEntry(String type) {
	for (PrimitiveToWrapperEntry entry : primitiveToWrapperTypes) {
	    if (entry.primitiveType.equals(type)) {
		return entry;
	    }
	}
	return null;
    }

    protected String getReferenceType(String type) {
	PrimitiveToWrapperEntry wrapperEntry = findWrapperEntry(type);
	if (wrapperEntry != null) {
	    return wrapperEntry.wrapperType;
	} else {
	    return type;
	}
    }

    protected void generateStaticSlots(DomainClass domClass, PrintWriter out) {
        Iterator roleSlotsIter = domClass.getRoleSlots();
        while (roleSlotsIter.hasNext()) {
            Role role = (Role) roleSlotsIter.next();
            if (role.getName() != null) {
                generateStaticRoleSlots(role, out);
            }
        }
        
        roleSlotsIter = domClass.getRoleSlots();
        while (roleSlotsIter.hasNext()) {
            Role role = (Role) roleSlotsIter.next();
            if (role.getName() != null) {
                generateStaticRelationSlots(role, out);
            }
        }
    }

    protected void generateStaticRoleSlots(Role role, PrintWriter out) {
        onNewline(out);

        Role otherRole = role.getOtherRole();

        // The Role slot
        String roleType = getRoleType(role);
        printWords(out, "public", "final", "static", roleType, getRoleHandlerName(role, false), "=", "new", roleType);
        print(out, "(");
        print(out, getRoleArgs(role));
        print(out, ")");
        newBlock(out);
        generateRoleClassGetter(role, otherRole, out);
        
        // the getInverseRole method
        String inverseRoleType = makeGenericType("dml.runtime.Role",
                                                 getTypeFullName(role.getType()),
                                                 getTypeFullName(otherRole.getType()));
        printMethod(out, "public", inverseRoleType, "getInverseRole");
        startMethodBody(out);
        print(out, "return ");
        if (otherRole.getName() == null) {
            print(out, "new ");
            print(out, getRoleType(otherRole));
            print(out, "(this)");
        } else {
            print(out, getRoleHandlerName(otherRole, true));
        }
        print(out, ";");
        endMethodBody(out);
        closeBlock(out, false);
        println(out, ";");
    }

    protected void generateRoleClassGetter(Role role, Role otherRole, PrintWriter out) {
        boolean multOne = (role.getMultiplicityUpper() == 1);

        print(out, "public ");
        print(out, makeGenericType((multOne ? getBoxBaseType() : "dml.runtime.RelationBaseSet"), getTypeFullName(role.getType())));
        print(out, " ");
        print(out, multOne ? "getBox(" : "getSet(");
        print(out, getTypeFullName(otherRole.getType()));
        print(out, " o1)");
        startMethodBody(out);
        print(out, "return ((");
        print(out, otherRole.getType().getBaseName());
        print(out, ")o1).");
        print(out, role.getName());
        print(out, ";");
        endMethodBody(out);
    }

    protected String getRoleHandlerName(Role role, boolean otherClass) {
        StringBuilder buf = new StringBuilder();
        
        if (otherClass) {
            buf.append(getTypeFullName(role.getOtherRole().getType()));
            buf.append(".");
        }
        
        buf.append("role$$");
        buf.append(role.getName());

        return buf.toString();
    }

    protected String getDirectRelationType() {
        return dml.runtime.DirectRelation.class.getName();
    }

    protected void generateStaticRelationSlots(Role role, PrintWriter out) {
        onNewline(out);

        Role otherRole = role.getOtherRole();

        String genericType = 
            "<" 
            + getTypeFullName(otherRole.getType())
            + ","
            + getTypeFullName(role.getType())
            + ">";

        boolean multOne = (role.getMultiplicityUpper() == 1);

        // The relation slot
        String relationSlotName = getRelationSlotNameFor(role);
        String directRelationType = getDirectRelationType();

        boolean isDirectRelation = (role.isFirstRole() || (otherRole.getName() == null));

        print(out, isDirectRelation ? "public final static " : "public static ");
        print(out, isDirectRelation ? directRelationType : "dml.runtime.Relation");
        print(out, genericType);
        print(out, " ");
        print(out, relationSlotName);
        if (isDirectRelation) {
            print(out, " = ");
            print(out, "new ");
            print(out, directRelationType);
            print(out, genericType);
            print(out, "(");
            print(out, getRoleHandlerName(role, false));
            println(out, ");");
            if (otherRole.getName() != null) {
                print(out, "static");
                newBlock(out);
                print(out, getTypeFullName(role.getType()));
                print(out, ".");
                print(out, getRelationSlotNameFor(otherRole));
                print(out, " = ");
                print(out, relationSlotName);
                print(out, ".getInverseRelation();");
                closeBlock(out);
            }
        } else {
            println(out, ";");
        }
    }

    protected String getRoleType(Role role) {
        return makeGenericType(getRoleBaseType(role),
                               getTypeFullName(role.getOtherRole().getType()),
                               getTypeFullName(role.getType()));
    }

    protected String getRoleBaseType(Role role) {
        return ((role.getName() == null)
                ? "dml.runtime.RoleEmpty"
                : ((role.getMultiplicityUpper() == 1) 
                   ? getRoleOneBaseType()
                   : getRoleManyBaseType()));
    }

    protected String getRoleArgs(Role role) {
        return "";
    }

    protected String getRoleOneBaseType() {
        return "dml.runtime.RoleOne";
    }

    protected String getRoleManyBaseType() {
        return "dml.runtime.RoleMany";
    }

    protected void generateRoleSlots(Iterator roleSlotsIter, PrintWriter out) {
        while (roleSlotsIter.hasNext()) {
            Role role = (Role) roleSlotsIter.next();
            if (role.getName() != null) {
                generateRoleSlot(role, out);
            }
        }
    }

    protected String getCollectionTypeFor(Role role) {
        boolean indexed = (role.getIndexProperty() != null);
        boolean ordered = role.getOrdered();
        if (indexed && ordered) {
            throw new Error("Can't handle roles that are both indexed and ordered");
        }

        String elemsType = getTypeFullName(role.getType());

        if (indexed) {
            // FIXME: the type of the key should correspond to the type of the index property
            return makeGenericType("java.util.Map", "java.lang.Object", elemsType);
        } else {
            return makeGenericType("java.util.Set", elemsType);
        }
    }

    protected String getRelationAwareTypeFor(Role role) {
        String elemType = getTypeFullName(role.getType());
        String thisType = getTypeFullName(role.getOtherRole().getType());
        return makeGenericType(getRelationAwareBaseTypeFor(role), thisType, elemType);
    }

    protected String getRelationAwareBaseTypeFor(Role role) {
        // FIXME: handle other types of collections other than sets
        return "dml.runtime.RelationAwareSet";
    }

    protected String getBoxBaseType() {
        return "jvstm.VBox";
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

    protected void generateRoleSlot(Role role, PrintWriter out) {
        onNewline(out);
        if (role.getMultiplicityUpper() == 1) {
            printWords(out, "private", getBoxType(role), role.getName());
        } else {
            printWords(out, "private", getRelationAwareTypeFor(role), role.getName());
        }
        println(out, ";");
    }

    protected String getNewSlotExpression(Slot slot) {
        return "new " + getBoxType(slot) + "()";
    }

    protected String getNewRoleOneSlotExpression(Role role) {
        return "new " + getBoxType(role) + "()";
    }

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
        buf.append(")");

        return buf.toString();
    }

    protected String getRelationSlotNameFor(Role role) {
        // when the two roles of a relation are played by the same class, 
        // we need to give different names to the relation slots because both
        // will be in the same class

        if ((role.getType() == role.getOtherRole().getType()) && (! role.isFirstRole())) {
            return (role.getRelation().getName() + "$Inverse");
        } else {
            return role.getRelation().getName();
        }
    }

    protected void generateInitInstance(DomainClass domClass, PrintWriter out) {
        // generate initInstance method
        onNewline(out);
        newline(out);
        printMethod(out, "private", "void", "initInstance", makeArg("boolean", "allocateOnly"));
        startMethodBody(out);

        for (Slot slot : domClass.getSlotsList()) {
            generateInitSlot(slot, out);
        }

        for (Role role : domClass.getRoleSlotsList()) {
            if (role.getName() != null) {
                generateInitRoleSlot(role, out);
            }
        }
        
        endMethodBody(out);

        // add instance initializer block that calls the initInstance method
        newline(out);
        newBlock(out);
        print(out, "initInstance(false);");
        closeBlock(out);
    }

    protected void generateInitSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        printWords(out, slot.getName());
        print(out, " = ");
        print(out, getNewSlotExpression(slot));
        print(out, ";");

        // initialize primitive slots with their default value
        generateInitializePrimitiveIfNeeded(slot, out);
    }

    protected void generateInitializePrimitiveIfNeeded(Slot slot, PrintWriter out) {
	PrimitiveToWrapperEntry wrapperEntry = findWrapperEntry(slot.getTypeName());
	if (wrapperEntry != null) { // then it is a primitive type
	    onNewline(out);
	    print(out, "if (!allocateOnly) this.");
	    print(out, slot.getName() + ".put(this, \"" + slot.getName() + "\", ");
	    println(out, wrapperEntry.defaultPrimitiveValue + ");");
	}
    }

    protected void generateInitRoleSlot(Role role, PrintWriter out) {
        onNewline(out);
        print(out, role.getName());
        print(out, " = ");
        if (role.getMultiplicityUpper() == 1) {
            print(out, getNewRoleOneSlotExpression(role));
        } else {
            print(out, getNewRoleStarSlotExpression(role));
        }
        print(out, ";");
    }

    protected void generateSlotsAccessors(Iterator slotsIter, PrintWriter out) {
        while (slotsIter.hasNext()) {
            generateSlotAccessors((Slot) slotsIter.next(), out);
        }
    }

    protected void generateSlotAccessors(Slot slot, PrintWriter out) {
        generateSlotGetter(slot.getName(), slot.getTypeName(), out);
        generateSlotSetter(slot.getName(), slot.getTypeName(), out);
    }

    protected String getSlotExpression(String slotName) {
        return "this." + slotName;
    }

    protected void generateSlotGetter(String slotName, String typeName, PrintWriter out) {
        generateGetter("public", "get" + capitalize(slotName), slotName, typeName, out);
    }

    protected void generateGetter(String visibility, String getterName, String slotName, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, visibility, typeName, getterName);
        startMethodBody(out);
        generateGetterBody(slotName, typeName, out);
        endMethodBody(out);
    }

    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        printWords(out, "return", getSlotExpression(slotName));
        print(out, ".get();");
    }


    protected void generateSlotSetter(String slotName, String typeName, PrintWriter out) {
        generateSetter("public", "set" + capitalize(slotName), slotName, typeName, out);
    }

    protected void generateInternalSetter(String visibility, String setterName, String slotName, String typeName, PrintWriter out) {
        generateSetter(visibility, setterName, slotName, typeName, out);
    }

    protected void generateSetter(String visibility, String setterName, String slotName, String typeName, PrintWriter out) {
        newline(out);

        printFinalMethod(out, visibility, "void", setterName, makeArg(typeName, slotName));

        startMethodBody(out);
        generateSetterBody(setterName, slotName, typeName, out);
        endMethodBody(out);            
    }

    protected void generateRoleGetter(String slotName, String typeName, PrintWriter out) {
        generateGetter("public", "get" + capitalize(slotName), slotName, typeName, out);
    }

    protected void generateSetterBody(String setterName, String slotName, String typeName, PrintWriter out) {
        print(out, getSlotExpression(slotName));
        print(out, ".put(");
        print(out, slotName);
        print(out, ");");
    }

    protected void generateRoleSlotsMethods(Iterator roleSlotsIter, PrintWriter out) {
        while (roleSlotsIter.hasNext()) {
            Role role = (Role) roleSlotsIter.next();
            if (role.getName() != null) {
                generateRoleSlotMethods(role, out);
            }
        }
    }

    protected void generateRoleSlotMethods(Role role, PrintWriter out) {
        if (role.getMultiplicityUpper() == 1) {
            generateRoleSlotMethodsMultOne(role, out);
        } else {
            generateRoleSlotMethodsMultStar(role, out);
        }

        if (role.needsMultiplicityChecks()) {
            generateMultiplicityConsistencyPredicate(role, out);
        }
    }


    protected void generateRoleSlotMethodsMultOne(Role role, PrintWriter out) {
        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String getterName = "get" + capitalizedSlotName;
        String setterName = "set" + capitalizedSlotName;

        String methodModifiers = getMethodModifiers();

        generateRoleGetter(slotName, typeName, out);

        // public setter
        newline(out);
        printMethod(out, methodModifiers, "void", setterName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationAddMethodCall(role, slotName, null, out);
        endMethodBody(out);
        
        
        // hasXpto
        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName);
        startMethodBody(out);
        print(out, "return (");
        print(out, getterName);
        print(out, "() != null);");
        endMethodBody(out);

                
        // removeXpto
        newline(out);
        printMethod(out, methodModifiers, "void", "remove" + capitalizedSlotName);
        startMethodBody(out);
        print(out, setterName);
        print(out, "(null);");
        endMethodBody(out);
    }

    protected void generateRelationAddMethodCall(Role role, String otherArg, String indexParam, PrintWriter out) {
        print(out, getRelationSlotNameFor(role));
        print(out, ".add((");
        print(out, getEntityFullName(role.getOtherRole().getType()));
        print(out, ")this, ");
        print(out, otherArg);
        print(out, ");");
    }


    protected void generateRelationRemoveMethodCall(Role role, String otherArg, PrintWriter out) {
        print(out, getRelationSlotNameFor(role));
        print(out, ".remove((");
        print(out, getEntityFullName(role.getOtherRole().getType()));
        print(out, ")this, ");
        print(out, otherArg);
        print(out, ");");
    }

    protected String getAdderMethodName(Role role) {
        return "add" + capitalize(role.getName());
    }

    protected String getRemoverMethodName(Role role) {
        return "remove" + capitalize(role.getName());
    }

    protected String getMethodModifiers() {
        return (compArgs.generateFinals ? "public final" : "public");
    }

    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {
        String indexProp = role.getIndexProperty();
        boolean isIndexed = (indexProp != null);
        String indexGetterCall = (isIndexed ? "get" + capitalize(indexProp) + "()" : "");

        boolean isOrdered = role.getOrdered();

        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String slotAccessExpression = getSlotExpression(slotName);
        String capitalizedSlotName = capitalize(slotName);
        String collectionType = getCollectionTypeFor(role);

        String posVar = (slotName.equals("index") ? "pos" : "index");

        String methodModifiers = getMethodModifiers();


//         int getChildCount() {
//             return setOfChild.size();
//         }
        // getXptoCount
        generateRoleSlotMethodsMultStarCount(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression);
        
        
//         boolean hasAnyChild() {
//             return (! setOfChild.isEmpty());
//         }
        // hasAnyXpto
        generateRoleSlotMethodsMultStarHasAnyChild(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression);
        
        
//         boolean hasChild(SiteElement child) {
//             return setOfChild.contains(child);
//         }
        // hasXpto
        generateRoleSlotMethodsMultStarHasChild(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression, typeName, slotName, isIndexed, indexGetterCall);


        if (isIndexed) {
            //         SiteElement getChild(String name) {
            //             return (SiteElement)mapOfChild.get(name);
            //         }
            // getXpto
            newline(out);
            printMethod(out, methodModifiers, typeName, "get" + capitalizedSlotName, makeArg("java.lang.String", indexProp));
            startMethodBody(out);
            print(out, "return (");
            print(out, typeName);
            print(out, ")");
            print(out, slotAccessExpression);
            print(out, ".get(");
            print(out, indexProp);
            print(out, ");");
            endMethodBody(out);
        }

        if (isOrdered) {
            //         SiteElement getChild(int index) {
            //             return (SiteElement)listOfChild.get(index);
            //         }
            // getXpto
            newline(out);
            printMethod(out, methodModifiers, typeName, "get" + capitalizedSlotName, makeArg("int", "index"));
            startMethodBody(out);
            print(out, "return (");
            print(out, typeName);
            print(out, ")");
            print(out, slotAccessExpression);
            print(out, ".get(index);");
            endMethodBody(out);


            //         void switchChild(int index1, int index2) {
            //             List collection = getCollectionOfChild();
            //             Object el1 = collection.get(index1);
            //             collection.set(index1, collection.get(index2));
            //             collection.set(index2, el1);
            //         }
            // getXpto
            newline(out);
            printMethod(out, methodModifiers, "void", "switch" + capitalizedSlotName, makeArg("int", "index1"), makeArg("int", "index2"));
            startMethodBody(out);
            print(out, getCollectionTypeFor(role));
            print(out, " collection = ");
            print(out, slotAccessExpression);
            println(out, ";");
            print(out, getTypeFullName(role.getType()));
            println(out, " el1 = collection.get(index1);");
            println(out, "collection.set(index1, collection.get(index2));");
            print(out, "collection.set(index2, el1);");
            endMethodBody(out);
        }
        
        
        
        // getXptoSet
        // FIXME: This deals only with the normal case of a Set (without considering ordered or indexed by)
        generateRoleSlotMethodsMultStarSet(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression, slotName, typeName);
        
//         void addChild(SiteElement child) {
//             SiteHierarchy.add(this, child);
//         }
        // addXpto
        String adderMethodName = getAdderMethodName(role);

        newline(out);
        printMethod(out, methodModifiers, "void", adderMethodName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationAddMethodCall(role, slotName, (isOrdered ? "-1" : null), out);
        endMethodBody(out);
        
        if (isOrdered) {
            //         void addChild(SiteElement child, int index) {
            //             SiteHierarchy.add(this, index, child);
            //         }
            // addXpto
            String indexParam = (slotName.equals("index") ? "pos" : "index");
            newline(out);
            printMethod(out, methodModifiers, "void", adderMethodName, makeArg(typeName, slotName), makeArg("int", indexParam));
            startMethodBody(out);
            generateRelationAddMethodCall(role, slotName, indexParam, out);
            endMethodBody(out);
        }

        
//         void removeChild(SiteElement child) {
//             SiteHierarchy.remove(this, child);
//         }
        // removeXpto
        String removerMethodName = getRemoverMethodName(role);

        newline(out);
        printMethod(out, methodModifiers, "void", removerMethodName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationRemoveMethodCall(role, slotName, out);
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarCount(Role role, PrintWriter out,
	    String methodModifiers, String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "int", "get" + capitalizedSlotName + "Count");
        startMethodBody(out);
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ".size();");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasAnyChild(Role role, PrintWriter out,
	    String methodModifiers, String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "hasAny" + capitalizedSlotName);
        startMethodBody(out);
        print(out, "return (! ");
        print(out, slotAccessExpression);
        print(out, ".isEmpty());");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasChild(Role role, PrintWriter out, String methodModifiers,
	    String capitalizedSlotName, String slotAccessExpression, String typeName, String slotName, boolean isIndexed,
	    String indexGetterCall) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName, makeArg(typeName, slotName));
        startMethodBody(out);
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

    protected void generateRoleSlotMethodsMultStarSet(Role role, PrintWriter out, String methodModifiers,
	    String capitalizedSlotName, String slotAccessExpression, String slotName, String typeName) {
        newline(out);
        printMethod(out, methodModifiers, makeGenericType("java.util.Set", typeName), "get" + capitalizedSlotName + "Set");
        startMethodBody(out);
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ";");
        endMethodBody(out);
    }

    protected void generateMultiplicityConsistencyPredicate(Role role, PrintWriter out) {
        String slotName = role.getName();
        String slotAccessExpression = getSlotExpression(slotName);
        String capitalizedSlotName = capitalize(slotName);

        newline(out);
        println(out, "@jvstm.cps.ConsistencyPredicate");
        printMethod(out, "public final", "boolean", "checkMultiplicityOf" + capitalizedSlotName);
        startMethodBody(out);

        int lower = role.getMultiplicityLower();
        int upper = role.getMultiplicityUpper();

        if (lower > 0) {
            print(out, "if (");
            if (upper == 1) {
                print(out, "! has");
                print(out, capitalizedSlotName);
                print(out, "()");
            } else {
                print(out, slotAccessExpression);
                print(out, ".size() < " + lower);
            }
            println(out, ") return false;");
        }

        if ((upper > 1) && (upper != Role.MULTIPLICITY_MANY)) {
            print(out, "if (");
            print(out, slotAccessExpression);
            print(out, ".size() > " + upper);
            println(out, ") return false;");
        }

        print(out, "return true;");
        endMethodBody(out);
    }

    protected void generateSlotConsistencyPredicates(DomainClass domClass, PrintWriter out) {
        if (domClass.hasSlotWithOption(Slot.Option.REQUIRED)) {
            generateRequiredConsistencyPredicate(domClass, out);
        }
    }

    protected void generateRequiredConsistencyPredicate(DomainClass domClass, PrintWriter out) {
        newline(out);
        println(out, "@jvstm.cps.ConsistencyPredicate");
        printMethod(out, "private", "boolean", "checkRequiredSlots");
        startMethodBody(out);

        for (Slot slot : domClass.getSlotsList()) {
            if (slot.hasOption(Slot.Option.REQUIRED)) {
                String slotName = slot.getName();

                print(out, "dml.runtime.ConsistencyChecks.checkRequired(this, \"");
                print(out, slotName);
                print(out, "\", get");
                print(out, capitalize(slotName));
                println(out, "());");
            }
        }

        print(out, "return true;");
        endMethodBody(out);
    }

    public static String makeGenericType(String baseType, String... argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append(baseType);

        if (argTypes.length > 0) {
            buf.append("<");
            
            String sep = "";
            for (String argType : argTypes) {
                buf.append(sep);
                buf.append(argType);
                sep = ",";
            }
            buf.append(">");
        }

        return buf.toString();
    }

    protected void printMethod(PrintWriter out, String mods, String type, String name, String... args) {
        printWords(out, mods, type, name);

        print(out, "(");
        String sep = "";
        for (String arg : args) {
            print(out, sep);
            print(out, arg);
            sep = ", ";
        }
        print(out, ")");
    }

    protected void printFinalMethod(PrintWriter out, String mods, String type, String name, String... args) {
        if (compArgs.generateFinals) {
            mods += " final";
        }
        printMethod(out, mods, type, name, args);
    }

    protected void startMethodBody(PrintWriter out) {
        newBlock(out);
    }

    protected void endMethodBody(PrintWriter out) {
        closeBlock(out);
    }

    protected String makeArg(String type, String arg) {
        return type + " " + arg;
    }

    protected void printWords(PrintWriter out, String... words) {
        String sep = (afterSpace ? "" : " ");
        for (String w : words) {
            print(out, sep);
            print(out, w);
            sep = " ";
        }
    }


    protected void print(PrintWriter out, String text) {
        out.print(text);
        onNewline = false;
        afterSpace = text.endsWith(" ");
    }

    protected void println(PrintWriter out, String text) {
        print(out, text);
        newline(out);
    }

    protected void newBlock(PrintWriter out) {
        if (! afterSpace) {
            print(out, " ");
        }
        print(out, "{");
        indentMore();
        newline(out);
    }

    protected void closeBlock(PrintWriter out) {
        closeBlock(out, true);
    }

    protected void closeBlock(PrintWriter out, boolean withNewLine) {
        indentLess();
        newline(out);
        print(out, "}");
        if (withNewLine) {
            newline(out);
        }
    }

    protected void comment(PrintWriter out, String msg) {
        print(out, "// ");
        print(out, msg);
        newline(out);
    }

    protected void onNewline(PrintWriter out) {
        if (! onNewline) {
            newline(out);
        }
    }

    public static String capitalize(String str) {
        if ((str == null) || Character.isUpperCase(str.charAt(0))) {
            return str;
        } else {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }


    private int indent = 0;
    private boolean onNewline = true;
    private boolean afterSpace = true;
    
    private void indentMore() {
        indent += 4;
    }
    
    private void indentLess() {
        indent -= 4;
    }
    
    protected void newline(PrintWriter out) {
        out.println();
        for (int i = 0; i < indent; i++) {
            out.print(" ");
        }
        onNewline = true;
        afterSpace = true;
    }    
}
