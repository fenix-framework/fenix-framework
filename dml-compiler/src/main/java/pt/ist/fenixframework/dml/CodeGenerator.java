package pt.ist.fenixframework.dml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.dml.runtime.RelationAwareSet;
import eu.cloudtm.LocalityHints;

/**
 * Top-level class of all DML code generators.
 */
public abstract class CodeGenerator {
    public static final String BACKEND_PACKAGE = "pt.ist.fenixframework.backend";
    public static final String ABSTRACT_BACKEND_ID_CLASS = "BackEndId";
    public static final String CURRENT_BACKEND_ID_CLASS = "CurrentBackEndId";
    public static final String CURRENT_BACKEND_ID_FULL_CLASS = BACKEND_PACKAGE + "." + CURRENT_BACKEND_ID_CLASS;
    public static final String COLLECTION_CLASS_NAME_KEY = "collectionClassName";
    protected static final String LOCALITY_HINTS_TYPE = LocalityHints.class.getCanonicalName();

    protected static class PrimitiveToWrapperEntry {
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
            new PrimitiveToWrapperEntry("boolean", "Boolean", "false"), new PrimitiveToWrapperEntry("byte", "Byte", "(byte)0"),
            new PrimitiveToWrapperEntry("char", "Character", "'\\u0000'"),
            new PrimitiveToWrapperEntry("short", "Short", "(short)0"), new PrimitiveToWrapperEntry("int", "Integer", "0"),
            new PrimitiveToWrapperEntry("float", "Float", "0.0f"), new PrimitiveToWrapperEntry("long", "Long", "0L"),
            new PrimitiveToWrapperEntry("double", "Double", "0.0d") };

    private final CompilerArgs compArgs;
    private final DomainModel domainModel;
    private final File destDirectory;
    private final File destDirectoryBase;
    private String collectionToUse;

    public CodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        this.compArgs = compArgs;
        this.domainModel = domainModel;
        this.destDirectory = getPackageDirectory(compArgs.destDirectory, compArgs.packageName);
        this.destDirectoryBase =
                getPackageDirectory((compArgs.destDirectoryBase == null) ? compArgs.destDirectory : compArgs.destDirectoryBase,
                        compArgs.packageName);
        String collectionName = compArgs.getParams().get(COLLECTION_CLASS_NAME_KEY);
        if (collectionName == null || collectionName.isEmpty()) {
            this.collectionToUse = "java.util.HashSet";
        } else {
            this.collectionToUse = compArgs.getParams().get(COLLECTION_CLASS_NAME_KEY);
        }
    }

    public void setCollectionToUse(String newCollectionName) {
        this.collectionToUse = newCollectionName;
    }

    public String getCollectionToUse(Role role) {
	String collectionName = role.getCollection();
	if (collectionName == null) {
	    collectionName = collectionToUse;
	}
        return collectionName;
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
        return pt.ist.fenixframework.core.AbstractDomainObject.class.getName();
    }

    /**
     * Generate the backend-specific code for the domain model.
     */
    public void generateCode() {
        // used by the value-type generator
        ValueTypeSerializationGenerator valueTypeGenerator = new ValueTypeSerializationGenerator(compArgs, domainModel);
        valueTypeGenerator.generateCode();
        generateClasses(getDomainModel().getClasses());
        generateBackEndId();
    }

    /**
     * Generate the class that identifies the backend to which this code generator creates the
     * code. The generated class must be named {@link pt.ist.fenixframework.backend.CurrentBackEndId} and extend the
     * {@link pt.ist.fenixframework.backend.BackEndId} class.
     */
    protected void generateBackEndId() {
        writeToFile(new File(getBaseDirectoryFor(BACKEND_PACKAGE), CURRENT_BACKEND_ID_CLASS + ".java"), new WriteProcedure() {
            @Override
            public void doIt(PrintWriter out) {
                generateFilePreamble(BACKEND_PACKAGE, out);
                generateCurrentBackEndIdClass(CURRENT_BACKEND_ID_CLASS, out);
            }
        });
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

    protected void generateClasses(Iterator<DomainClass> classesIter) {
        while (classesIter.hasNext()) {
            generateOneClass(classesIter.next());
        }
    }

    protected void generateOneClass(final DomainClass domClass) {
        final String packageName = domClass.getPackageName();

        writeToFile(new File(getBaseDirectoryFor(packageName), domClass.getBaseName() + ".java"), new WriteProcedure() {
            @Override
            public void doIt(PrintWriter out) {
                generateFilePreamble(packageName, out);
                generateBaseClass(domClass, out);
            }
        });

        // don't generate non-base classes for an external definition.
        if (compArgs.isExternalDefinition(domClass.getSourceFile())) {
            return;
        }

        File leafClassFile = new File(getDirectoryFor(packageName), domClass.getName() + ".java");
        if (!leafClassFile.exists()) {
            writeToFile(leafClassFile, new WriteProcedure() {
                @Override
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
        print(out, "this((" + LOCALITY_HINTS_TYPE + ")null);");
        endMethodBody(out);

        printMethod(out, "public", "", className, makeArg(LOCALITY_HINTS_TYPE, "hints"));
        startMethodBody(out);
        print(out, "super(hints);");
        endMethodBody(out);
    }

    protected void generateCurrentBackEndIdClass(String className, PrintWriter out) {
        printWords(out, "public", "class", className, "extends", ABSTRACT_BACKEND_ID_CLASS);
        newBlock(out);
        generateBackEndIdClassBody(out);
        closeBlock(out);
    }

    protected void generateBackEndIdClassBody(PrintWriter out) {
        // getBackEndName()
        newline(out);
        printMethod(out, "public", "String", "getBackEndName");
        startMethodBody(out);
        printWords(out, "return", "\"" + getBackEndName() + "\";");
        endMethodBody(out);

        // getDefaultConfigClass
        newline(out);
        printMethod(out, "public", "Class<? extends pt.ist.fenixframework.Config>", "getDefaultConfigClass");
        startMethodBody(out);
        print(out, "try");
        newBlock(out);
        printWords(out, "return", "(Class<? extends pt.ist.fenixframework.Config>)Class.forName(\"" + getDefaultConfigClassName()
                + "\");");
        closeBlock(out);
        printWords(out, "catch", "(Exception e)");
        newBlock(out);
        print(out, "throw new RuntimeException(e);");
        closeBlock(out);
        endMethodBody(out);

        // getAtomicContextFactoryClass
        newline(out);
        printMethod(out, "public", "Class<? extends pt.ist.fenixframework.atomic.ContextFactory>", "getAtomicContextFactoryClass");
        startMethodBody(out);
        Class factory = getAtomicContextFactoryClass();
        if (factory != null) {
            printWords(out, "return", factory.getName() + ".class;");
        } else {
            printWords(out, "return", "null;");
        }
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
        generateBaseClassConstructors(domClass, out);
        
        // slots getters/setters
        generateSlotsAccessors(domClass, out);

        // roles methods
        generateRoleSlotsMethods(domClass.getRoleSlots(), out);

        // // generate slot consistency predicates
        // generateSlotConsistencyPredicates(domClass, out);
    }

    protected void generateBaseClassConstructors(DomainClass domClass, PrintWriter out) {
        printMethod(out, "protected", "", domClass.getBaseName());
        startMethodBody(out);
        printWords(out, "this(null);");
        endMethodBody(out);

        newline(out);
        printMethod(out, "protected", "", domClass.getBaseName(), makeArg(LOCALITY_HINTS_TYPE, "hints"));
        startMethodBody(out);
        generateBaseClassConstructorsBody(domClass, out);
        endMethodBody(out);
    }

    protected void generateBaseClassConstructorsBody(DomainClass domClass, PrintWriter out) {
        print(out, "super(hints);");
    }

    protected void generateSlots(Iterator slotsIter, PrintWriter out) {
        while (slotsIter.hasNext()) {
            generateSlot((Slot) slotsIter.next(), out);
        }
    }

    protected void generateSlot(Slot slot, PrintWriter out) {
        onNewline(out);
        printWords(out, "private", slot.getTypeName(), slot.getName());
        print(out, ";");
    }

    // this method is similar to the previous, but there are cases when we need to use another type
    // (different from the slot's)
    protected void generateSlotDeclaration(PrintWriter out, String type, String name) {
        printWords(out, "private", type, name);
        println(out, ";");
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
                generateStaticKeyFunctionForRole(role, out);
            }
        }

    }

    protected void generateStaticKeyFunctionForRole(Role role, PrintWriter out) {
        if (role.getMultiplicityUpper() == Role.MULTIPLICITY_MANY) {
            println(out, generateMapKeyFunction(role.getName(), role.getType().getFullName(), "Comparable<?>", "Oid", false));
        }
    }

    protected String generateMapKeyFunction(String roleName, String valueType, String keyType, String keyField,
            boolean allowMultipleKeys) {
        String format =
                "private static pt.ist.fenixframework.dml.runtime.KeyFunction<%keyType%,%valueType%> keyFunction$$%roleName% = new pt.ist.fenixframework.dml.runtime.KeyFunction<%keyType%,%valueType%>() { public %keyType% getKey(%valueType% value) { return value.get%keyField%(); } public boolean allowMultipleKeys() {return %multKeys%; }};";
        return format.replaceAll("%roleName%", roleName).replaceAll("%valueType%", valueType)
                .replaceAll("%keyType%", getReferenceType(keyType)).replaceAll("%keyField%", capitalize(keyField))
                .replaceAll("%multKeys%", allowMultipleKeys ? "true" : "false");
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

        boolean multOne = (role.getMultiplicityUpper() == 1);
        if (multOne) {
            generateStaticRoleSlotsMultOne(role, otherRole, out);
        } else {
            generateStaticRoleSlotsMultStar(role, otherRole, out);
        }

        generateRoleMethodGetInverseRole(role, otherRole, out);
        closeBlock(out, false);
        println(out, ";");
    }

    protected void generateStaticRoleSlotsMultOne(Role role, Role otherRole, PrintWriter out) {
        printMethod(out, "public", getTypeFullName(role.getType()), "getValue",
                makeArg(getTypeFullName(otherRole.getType()), "o1"));
        startMethodBody(out);
        printWords(out, "return", "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + ";");
        endMethodBody(out);

        printMethod(out, "public", "void", "setValue", makeArg(getTypeFullName(otherRole.getType()), "o1"),
                makeArg(getTypeFullName(role.getType()), "o2"));
        startMethodBody(out);
        printWords(out, "((" + otherRole.getType().getBaseName() + ")o1)." + role.getName() + " = o2;");
        endMethodBody(out);
    }

    protected void generateStaticRoleSlotsMultStar(Role role, Role otherRole, PrintWriter out) {
        print(out, "public ");
        print(out, makeGenericType("pt.ist.fenixframework.dml.runtime.RelationBaseSet", getTypeFullName(role.getType())));
        print(out, " ");
        print(out, "getSet(");
        print(out, getTypeFullName(otherRole.getType()));
        print(out, " o1)");
        startMethodBody(out);
        print(out, "return (" + getConcreteSetTypeDeclarationFor(role) + ")");
        print(out, "o1.get");
        print(out, capitalize(role.getName()));
        print(out, "();");
        endMethodBody(out);
    }

    protected void generateRoleMethodGetInverseRole(Role role, Role otherRole, PrintWriter out) {
        // the getInverseRole method
        String inverseRoleType =
                makeGenericType("pt.ist.fenixframework.dml.runtime.Role", getTypeFullName(role.getType()),
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
        return pt.ist.fenixframework.dml.runtime.DirectRelation.class.getName();
    }

    protected void generateStaticRelationSlots(Role role, PrintWriter out) {
        onNewline(out);

        Role otherRole = role.getOtherRole();

        String genericType = "<" + getTypeFullName(otherRole.getType()) + "," + getTypeFullName(role.getType()) + ">";

        boolean multOne = (role.getMultiplicityUpper() == 1);

        // The relation slot
        String relationSlotName = getRelationSlotNameFor(role);
        String directRelationType = getDirectRelationType();

        boolean isDirectRelation = (role.isFirstRole() || (otherRole.getName() == null));

        print(out, isDirectRelation ? "public final static " : "public static ");
        print(out, isDirectRelation ? directRelationType : "pt.ist.fenixframework.dml.runtime.Relation");
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
            print(out, ", \"");
            print(out, role.getRelation().getName());
            print(out, "\"");
            generateDefaultRelationListeners(role, out);
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

    protected void generateDefaultRelationListeners(Role role, PrintWriter out) {
        // intentionally empty
    }

    protected String getRoleType(Role role) {
        return makeGenericType(getRoleBaseType(role), getTypeFullName(role.getOtherRole().getType()),
                getTypeFullName(role.getType()));
    }

    protected String getRoleBaseType(Role role) {
        return ((role.getName() == null) ? "pt.ist.fenixframework.dml.runtime.RoleEmpty" : ((role.getMultiplicityUpper() == 1) ? getRoleOneBaseType() : getRoleManyBaseType()));
    }

    protected String getRoleArgs(Role role) {
        return "";
    }

    protected String getRoleOneBaseType() {
        return "pt.ist.fenixframework.dml.runtime.RoleOne";
    }

    protected String getRoleManyBaseType() {
        return "pt.ist.fenixframework.dml.runtime.RoleMany";
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
        boolean ordered = role.isOrdered();
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
        return RelationAwareSet.class.getName();
    }

    protected void generateRoleSlot(Role role, PrintWriter out) {
        onNewline(out);
        if (role.getMultiplicityUpper() == 1) {
            printWords(out, "private", getTypeFullName(role.getType()), role.getName());
        } else {
            printWords(out, "private", getDefaultCollectionFor(role), role.getName());
        }
        println(out, ";");
    }

    protected String getDefaultCollectionFor(Role role) {
	String collectionName = role.getCollection();
	if (collectionName == null) {
	    collectionName = collectionToUse;
	}
        return makeGenericType(collectionName, role.getType().getFullName());
    }

    protected String getNewRoleStarSlotExpression(Role role) {
        StringBuilder buf = new StringBuilder();

        // generate the default collection
        buf.append("new ");
        buf.append(getDefaultCollectionFor(role));
        buf.append("()");

        return buf.toString();
    }

    protected String getRelationSlotNameFor(Role role) {
        // when the two roles of a relation are played by the same class, 
        // we need to give different names to the relation slots because both
        // will be in the same class

        if ((role.getType() == role.getOtherRole().getType()) && (!role.isFirstRole())) {
            return (role.getRelation().getName() + "$Inverse");
        } else {
            return role.getRelation().getName();
        }
    }

    /**
     * The purpose of the initInstance method is to have the code needed to correctly initialize a
     * domain object instance. There are two cases:
     * 
     * <ol>
     * <li>When the instance is *really* new.
     * <li>When the instance is being re-constructed from persistence (by the DomainObjectAllocator)
     * </ol>
     * 
     * <p>
     * In the first case the parameter 'allocateOnly' is false. Tipically, we need to fully initialize the slots, e.g. create new
     * lists, etc. In the second case, the instance's attributes will be populated, so we should not create them anew.
     * 
     * <p>
     * This method is responsible for: generating the <code>initInstance(boolean)</code> method; generate the call to this method
     * as an instance initializer with the parameter <code>allocateInstance = false</code>.
     */
    protected void generateInitInstance(DomainClass domClass, PrintWriter out) {
        // generate initInstance method to be used by OJB.  This is used in the
        // PostProcessDomainClasses. Maybe we could disable it..., or at least move it to a specific
        // backend.
        generateInitInstanceNoArg(domClass, out);

        // generate initInstance method
        generateInitInstanceMethod(domClass, out);

        // add instance initializer block that calls the initInstance method
        generateInitInstanceInitializer(domClass, out);
    }

    protected void generateInitInstanceNoArg(DomainClass domClass, PrintWriter out) {
        onNewline(out);
        newline(out);
        printMethod(out, "private", "void", "initInstance");
        startMethodBody(out);
        print(out, "initInstance(true);");
        endMethodBody(out);
    }

    protected void generateInitInstanceMethod(DomainClass domClass, PrintWriter out) {
        onNewline(out);
        newline(out);
        printMethod(out, "private", "void", "initInstance", makeArg("boolean", "allocateOnly"));
        startMethodBody(out);
        generateInitInstanceMethodBody(domClass, out);
        endMethodBody(out);
    }

    protected void generateInitInstanceMethodBody(DomainClass domClass, PrintWriter out) {
        // for (Slot slot : domClass.getSlotsList()) {
        //     generateInitSlot(slot, out);
        // }
        onNewline(out);

        for (Role role : domClass.getRoleSlotsList()) {
            if (role.getName() != null) {
                generateInitRoleSlot(role, out);
            }
        }
    }

    protected void generateInitInstanceInitializer(DomainClass domClass, PrintWriter out) {
        newline(out);
        newBlock(out);
        print(out, "initInstance(false);");
        closeBlock(out);
    }

    //     protected void generateInitSlot(Slot slot, PrintWriter out) {
    //         onNewline(out);
    //         printWords(out, slot.getName());
    //         print(out, " = ");
    //         print(out, getNewSlotExpression(slot));
    //         print(out, ";");

    //         // initialize primitive slots with their default value
    //         generateInitializePrimitiveIfNeeded(slot, out);
    //     }

    //     protected void generateInitializePrimitiveIfNeeded(Slot slot, PrintWriter out) {
    // 	PrimitiveToWrapperEntry wrapperEntry = findWrapperEntry(slot.getTypeName());
    // 	if (wrapperEntry != null) { // then it is a primitive type
    // 	    onNewline(out);
    // 	    print(out, "if (!allocateOnly) this.");
    // 	    print(out, slot.getName() + ".put(this, \"" + slot.getName() + "\", ");
    // 	    println(out, wrapperEntry.defaultPrimitiveValue + ");");
    // 	}
    //     }

    protected void generateInitRoleSlot(Role role, PrintWriter out) {
        if (role.getMultiplicityUpper() != 1) {
            onNewline(out);
            print(out, role.getName());
            print(out, " = ");
            print(out, getNewRoleStarSlotExpression(role));
            print(out, ";");
        }
    }

    protected void generateSlotsAccessors(DomainClass domainClass, PrintWriter out) {
        Iterator slotsIter = domainClass.getSlots();
        while (slotsIter.hasNext()) {
            generateSlotAccessors((Slot) slotsIter.next(), out);
        }
    }

    protected void generateSlotAccessors(Slot slot, PrintWriter out) {
        generateSlotGetter(slot.getName(), slot.getTypeName(), out);
	
        newline(out);
	printFinalMethod(out, "public", slot.getTypeName(), "get" + capitalize(slot.getName() + "Cached"), "boolean forceMiss");
	startMethodBody(out);
	print(out, "return get" + capitalize(slot.getName()) + "();");
	endMethodBody(out);
	newline(out);
	
        generateSlotGhostGetter(slot.getName(), slot.getTypeName(), out);
        generateEmptyRegisterGet(slot.getName(), out);
        generateSlotSetter(slot, out);
    }

    protected String getSlotExpression(String slotName) {
        return "this." + slotName;
    }

    protected void generateSlotGetter(String slotName, String typeName, PrintWriter out) {
        generateGetter("public", "get" + capitalize(slotName), slotName, typeName, out);
    }
    
    protected void generateSlotGhostGetter(String slotName, String typeName, PrintWriter out) {
	generateGetter("public", "get" + capitalize(slotName) + "Ghost", slotName, typeName, out);
    }
    
    protected void generateGetter(String visibility, String getterName, String slotName, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, visibility, typeName, getterName);
        startMethodBody(out);
        generateGetterBody(slotName, typeName, out);
        endMethodBody(out);
    }

    protected void generateGetterBody(String slotName, String typeName, PrintWriter out) {
        printWords(out, "return", getSlotExpression(slotName) + ";");
    }

    protected void generateSlotSetter(Slot slot, PrintWriter out) {
        generateSetter("public", "set" + capitalize(slot.getName()), slot, out);
    }

    //     protected void generateInternalSetter(String visibility, String setterName, String slotName, String typeName, PrintWriter out) {
    //         generateSetter(visibility, setterName, slotName, typeName, out);
    //     }

    protected void generateSetter(String visibility, String setterName, Slot slot, PrintWriter out) {
        newline(out);

        printFinalMethod(out, visibility, "void", setterName, makeArg(slot.getTypeName(), slot.getName()));

        startMethodBody(out);
        generateSetterBody(setterName, slot, out);
        endMethodBody(out);
    }

    protected void generateSetterBody(String setterName, Slot slot, PrintWriter out) {
        printWords(out, getSlotExpression(slot.getName()), "=", slot.getName() + ";");
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

        // if (role.needsMultiplicityChecks()) {
        //     generateMultiplicityConsistencyPredicate(role, out);
        // }
    }

    protected String getSetTypeDeclarationFor(Role role) {
        String elemType = getTypeFullName(role.getType());
        return makeGenericType("java.util.Set", elemType);
    }

    protected String getConcreteSetTypeDeclarationFor(Role role) {
        String elemType = getTypeFullName(role.getType());
        String thisType = getTypeFullName(role.getOtherRole().getType());
        return makeGenericType(getRelationAwareBaseTypeFor(role), thisType, elemType);
    }

    protected void generateRoleSlotMethodsMultOne(Role role, PrintWriter out) {
        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();

        // public getter
        generateRoleSlotMethodsMultOneGetter(slotName, typeName, out);

        // public setter
        generateRoleSlotMethodsMultOneSetter(role, out);

        // hasXpto
        generateRoleSlotMethodsMultOneHas(role, out);

        // removeXpto
        generateRoleSlotMethodsMultOneRemove(role, out);
    }

    protected void generateRoleSlotMethodsMultOneGetter(String slotName, String typeName, PrintWriter out) {
        generateGetter("public", "get" + capitalize(slotName), slotName, typeName, out);
        generateGetter("public", "get" + capitalize(slotName) + "Ghost", slotName, typeName, out);
        
        newline(out);
	printFinalMethod(out, "public", typeName, "get" + capitalize(slotName + "Cached"), "boolean forceMiss");
	startMethodBody(out);
	print(out, "return get" + capitalize(slotName) + "();");
	endMethodBody(out);
	newline(out);
	
        generateEmptyRegisterGet(slotName, out);
        
    }

    protected void generateRoleSlotMethodsMultOneSetter(Role role, PrintWriter out) {
        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String setterName = "set" + capitalizedSlotName;
        String methodModifiers = getMethodModifiers();

        newline(out);
        printMethod(out, methodModifiers, "void", setterName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationAddMethodCall(role, slotName, null, out);
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultOneHas(Role role, PrintWriter out) {
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String getterName = "get" + capitalizedSlotName;
        String methodModifiers = getMethodModifiers();

        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName);
        startMethodBody(out);
        print(out, "return (");
        print(out, getterName);
        print(out, "() != null);");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultOneRemove(Role role, PrintWriter out) {
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String setterName = "set" + capitalizedSlotName;
        String methodModifiers = getMethodModifiers();

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
        boolean isOrdered = role.isOrdered();

        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String slotAccessExpression = "get" + capitalizedSlotName + "()";
        String methodModifiers = getMethodModifiers();

        // getXptoCount
        generateRoleSlotMethodsMultStarCount(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression);

        // hasAnyXpto
        generateRoleSlotMethodsMultStarHasAnyChild(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression);

        // hasXpto
        generateRoleSlotMethodsMultStarHasChild(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression, typeName,
                slotName, "");

        if (isOrdered) {
            generateRoleSlotMethodsMultStarOrdered(role, out, typeName, methodModifiers, capitalizedSlotName,
                    slotAccessExpression);
        }

        // getXptoSet
        generateRoleSlotMethodsMultStarSet(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression, slotName,
                typeName);

        // addXpto
        String adderMethodName = getAdderMethodName(role);

        newline(out);
        printMethod(out, methodModifiers, "void", adderMethodName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationAddMethodCall(role, slotName, (isOrdered ? "-1" : null), out);
        endMethodBody(out);

        if (isOrdered) {
            // addXpto
            String indexParam = (slotName.equals("index") ? "pos" : "index");
            newline(out);
            printMethod(out, methodModifiers, "void", adderMethodName, makeArg(typeName, slotName), makeArg("int", indexParam));
            startMethodBody(out);
            generateRelationAddMethodCall(role, slotName, indexParam, out);
            endMethodBody(out);
        }

        // removeXpto
        String removerMethodName = getRemoverMethodName(role);

        newline(out);
        printMethod(out, methodModifiers, "void", removerMethodName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRelationRemoveMethodCall(role, slotName, out);
        endMethodBody(out);

        generateRoleSlotMethodsMultStarGettersAndIterators(role, out);

    }

    private void generateRoleSlotMethodsMultStarOrdered(Role role, PrintWriter out, String typeName, String methodModifiers,
            String capitalizedSlotName, String slotAccessExpression) {
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
        printMethod(out, methodModifiers, "void", "switch" + capitalizedSlotName, makeArg("int", "index1"),
                makeArg("int", "index2"));
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

    protected void generateRoleSlotMethodsMultStarGettersAndIterators(Role role, PrintWriter out) {
        generateRelationGetter("get" + capitalize(role.getName()), role, out);
        generateRelationGetter("get" + capitalize(role.getName()) + "Ghost", role, out);
        generateEmptyRegisterGet(role.getName(), out);
        generateIteratorMethod(role, out);
    }

    protected void generateIteratorMethod(Role role, PrintWriter out) {
        generateIteratorMethod(role, out, "get" + capitalize(role.getName()) + "()");
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

    protected void generateRelationGetter(String getterName, Role role, PrintWriter out) {
        String paramListType = makeGenericType("java.util.Set", getTypeFullName(role.getType()));
        generateRelationGetter(getterName, role, paramListType, out);
    }
    
    protected void generateRelationGetterCached(String getterName, Role role, PrintWriter out) {
        String paramListType = makeGenericType("java.util.Set", getTypeFullName(role.getType()));
        newline(out);
        printFinalMethod(out, "public", paramListType, getterName, "boolean forceMiss");
        startMethodBody(out);
        generateRelationGetterBody(role, out);
        endMethodBody(out);
    }

    protected void generateRelationGetter(String getterName, Role role, String paramListType, PrintWriter out) {
        generateRelationGetter(getterName, getSlotExpression(role.getName()), role, paramListType, out);
    }

    protected void generateRelationGetter(String getterName, String valueToReturn, Role role, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", typeName, getterName);

        startMethodBody(out);
        generateRelationGetterBody(role, out);
        endMethodBody(out);
    }

    protected void generateRelationGetterBody(Role role, PrintWriter out) {
        print(out, "return ");
        print(out, "new ");
        print(out, getRelationAwareTypeFor(role));
        print(out, "((");
        print(out, getTypeFullName(role.getOtherRole().getType()));
        print(out, ")this, ");
        print(out, getRelationSlotNameFor(role));
        print(out, ", ");
        print(out, role.getName());
        print(out, ", ");
        print(out, "keyFunction$$");
        print(out, role.getName());
        print(out, ");");
    }

    protected void generateRoleSlotMethodsMultStarCount(Role role, PrintWriter out, String methodModifiers,
            String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "int", "get" + capitalizedSlotName + "Count");
        startMethodBody(out);
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ".size();");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasAnyChild(Role role, PrintWriter out, String methodModifiers,
            String capitalizedSlotName, String slotAccessExpression) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "hasAny" + capitalizedSlotName);
        startMethodBody(out);
        print(out, "return (! ");
        print(out, slotAccessExpression);
        print(out, ".isEmpty());");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasChild(Role role, PrintWriter out, String methodModifiers,
            String capitalizedSlotName, String slotAccessExpression, String typeName, String slotName, String indexGetterCall) {
        newline(out);
        printMethod(out, methodModifiers, "boolean", "has" + capitalizedSlotName, makeArg(typeName, slotName));
        startMethodBody(out);
        generateRoleSlotMethodsMultStarHasChildBody(role, out, slotAccessExpression, slotName);
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultStarHasChildBody(Role role, PrintWriter out, String slotAccessExpression,
            String slotName) {
        print(out, "return ");
        print(out, slotAccessExpression);
        print(out, ".");
        print(out, "contains(");
        print(out, slotName);
        print(out, ");");
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

    protected void generateEmptyRegisterGet(String suffixName, PrintWriter out) {
	newline(out);
	printFinalMethod(out, "public", "void", "registerGet" + capitalize(suffixName));
	startMethodBody(out);
	// empty method on purpose
	endMethodBody(out);
    }
    
    //     protected void generateMultiplicityConsistencyPredicate(Role role, PrintWriter out) {
    //         String slotName = role.getName();
    //         String slotAccessExpression = getSlotExpression(slotName);
    //         String capitalizedSlotName = capitalize(slotName);

    //         newline(out);
    //         println(out, "@jvstm.cps.ConsistencyPredicate");
    //         printMethod(out, "public final", "boolean", "checkMultiplicityOf" + capitalizedSlotName);
    //         startMethodBody(out);

    //         int lower = role.getMultiplicityLower();
    //         int upper = role.getMultiplicityUpper();

    //         if (lower > 0) {
    //             print(out, "if (");
    //             if (upper == 1) {
    //                 print(out, "! has");
    //                 print(out, capitalizedSlotName);
    //                 print(out, "()");
    //             } else {
    //                 print(out, slotAccessExpression);
    //                 print(out, ".size() < " + lower);
    //             }
    //             println(out, ") return false;");
    //         }

    //         if ((upper > 1) && (upper != Role.MULTIPLICITY_MANY)) {
    //             print(out, "if (");
    //             print(out, slotAccessExpression);
    //             print(out, ".size() > " + upper);
    //             println(out, ") return false;");
    //         }

    //         print(out, "return true;");
    //         endMethodBody(out);
    //     }

    //     protected void generateSlotConsistencyPredicates(DomainClass domClass, PrintWriter out) {
    //         if (domClass.hasSlotWithOption(Slot.Option.REQUIRED)) {
    //             generateRequiredConsistencyPredicate(domClass, out);
    //         }
    //     }

    //     protected void generateRequiredConsistencyPredicate(DomainClass domClass, PrintWriter out) {
    //         newline(out);
    //         println(out, "@jvstm.cps.ConsistencyPredicate");
    //         printMethod(out, "private", "boolean", "checkRequiredSlots");
    //         startMethodBody(out);

    //         for (Slot slot : domClass.getSlotsList()) {
    //             if (slot.hasOption(Slot.Option.REQUIRED)) {
    //                 String slotName = slot.getName();

    //                 print(out, "pt.ist.fenixframework.dml.runtime.ConsistencyChecks.checkRequired(this, \"");
    //                 print(out, slotName);
    //                 print(out, "\", get");
    //                 print(out, capitalize(slotName));
    //                 println(out, "());");
    //             }
    //         }

    //         print(out, "return true;");
    //         endMethodBody(out);
    //     }

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
        if (!afterSpace) {
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
        if (!onNewline) {
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

    // Generic API that all code generators must implement.  These methods are invoked by the
    // DmlCompiler during its execution.

    /**
     * Get the name of the backend for which this class generates code. This method is used during
     * the execution of {@link #generateBackEndId()}.
     */
    protected abstract String getBackEndName();

    /**
     * Get the name of the default configuration class of the backend for which this class
     * generates code. This method is used during the execution of {@link #generateBackEndId()}.
     */
    protected abstract String getDefaultConfigClassName();

    /**
     * Get the class that implements the ContextFactory for AtomicContexts
     */
    protected abstract Class<? extends ContextFactory> getAtomicContextFactoryClass();
}
