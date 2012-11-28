package pt.ist.fenixframework.backend.ogm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
// import java.util.List;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.atomic.DefaultContextFactory;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DAPCodeGenerator;
import pt.ist.fenixframework.dml.DomainClass;
// import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;
import pt.ist.fenixframework.dml.Role;
import pt.ist.fenixframework.dml.Slot;
// import pt.ist.fenixframework.dml.ValueType;
import pt.ist.fenixframework.dml.ValueTypeSerializationGenerator;

public class OgmCodeGenerator extends IndexesCodeGenerator {

    protected static final String VT_SERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.SERIALIZATION_METHOD_PREFIX;

    protected static final String VT_DESERIALIZER =
        ValueTypeSerializationGenerator.SERIALIZER_CLASS_SIMPLE_NAME + "." +
        ValueTypeSerializationGenerator.DESERIALIZATION_METHOD_PREFIX;

    protected static final String PRIMARY_KEY_TYPE = "String";

    protected PrintWriter ormWriter;
    protected ArrayList<String> ormSlots;
    protected ArrayList<String> ormSlotsForRelationToOne;
    protected ArrayList<Role> ormRoleManyToOne;
    protected ArrayList<Role> ormRoleOneToMany;
    // protected ArrayList<Role> ormRoleOneToOne;
    protected ArrayList<Role> ormRoleManyToMany;
    protected ArrayList<String> ormTransientSlots;

    public OgmCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return OgmDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return OgmBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return OgmConfig.class.getName();
    }

    @Override
    protected Class<? extends ContextFactory> getAtomicContextFactoryClass() {
        return DefaultContextFactory.class;
    }

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        super.generateFilePreamble(subPackageName, out);
        println(out, "import pt.ist.fenixframework.backend.ogm.OgmBackEnd;");
        println(out, "import pt.ist.fenixframework.backend.ogm.OgmOID;");
        println(out, "import pt.ist.fenixframework.core.Externalization;");
        println(out, "import pt.ist.fenixframework.core.adt.bplustree.BPlusTree;");
        println(out, "import " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ";");
        println(out, "import static " + ValueTypeSerializationGenerator.SERIALIZER_CLASS_FULL_NAME + ".*;");
        newline(out);
    }

    ////////////////////////////////////////////// import from TPCW below

    @Override
    public void generateCode() {
        File file = new File(getBaseDirectoryFor("") + "/META-INF/orm.xml");
        file.getParentFile().mkdirs();

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            this.ormWriter = new PrintWriter(fileWriter, true);
            ormBeginFile();
            super.generateCode();
            ormGenerateNonBaseClasses(getDomainModel().getClasses());
            ormEndFile();
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

    @Override
    protected void generateBaseClass(DomainClass domClass, PrintWriter out) {
        ormBeginBaseClass(domClass);
        super.generateBaseClass(domClass, out);
        ormEndBaseClass();
        // ormGenerateNonBaseClass(domClass);
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        generateStaticSlots(domClass, out);
        newline(out);
        generateSlots(domClass.getSlots(), out);
        newline(out);
        generateRoleSlots(domClass.getRoleSlots(), out);
        generateInitInstance(domClass, out);
        generateDefaultConstructor(domClass.getBaseName(), out);
        generateSlotsAccessors(domClass, out);
        generateRoleSlotsMethods(domClass.getRoleSlots(), out);
        generateIndexMethods(domClass, out);
    }

    @Override
    protected void generateInitRoleSlot(Role role, PrintWriter out) {
        if (role.getMultiplicityUpper() != 1) {
            onNewline(out);
            print(out, role.getName());
            print(out, " = ");
            print(out, getNewRoleStarSlotExpressionWithEmptySet(role));
            print(out, ";");
        }
    }

    protected void generateDefaultConstructor(String classname, PrintWriter out) {
        printMethod(out, "public", "", classname);
        startMethodBody(out);
        endMethodBody(out);
    }

    private static String[] builtInTypesFromDmlInOgm = {
        // "char",
        // "java.lang.Character",
        // "short",
        // "java.lang.Short",
        // "float",
        // "java.lang.Float",
        "long",
        "java.lang.Long",
        "int",
        "java.lang.Integer",
        "double",
        "java.lang.Double",
        "java.lang.String",
        "boolean",
        "java.lang.Boolean",
        "byte",
        "java.lang.Byte",
        "bytearray",
        "byte[]"
    };
    
    private boolean ogmSupportsType(String typeName) {
        for (String supportedType : builtInTypesFromDmlInOgm) {
            if (supportedType.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void generateSlotAccessors(DomainClass domainClass, Slot slot, PrintWriter out) {
        super.generateSlotAccessors(domainClass, slot, out);
        generateHibernateSlotGetter(slot, out);
        generateHibernateSlotSetter(slot, out);
        this.ormSlots.add(addHibernateToSlotName(slot.getName()));
    }

    protected void generateHibernateSlotGetter(Slot slot, PrintWriter out) {
        String slotName = slot.getName();
        String typeNameFrom = slot.getTypeName();
        String typeNameTo;
        String slotExpression;

        if (ogmSupportsType(typeNameFrom)) {
            typeNameTo = typeNameFrom;
            slotExpression = getSlotExpression(slotName);
        } else {
            typeNameTo = "byte[]";
            slotExpression = "Externalization.externalizeObject(" + getSlotExpression(slotName) + ")";
        }

        newline(out);
        println(out, "@javax.persistence.Access(javax.persistence.AccessType.PROPERTY)");
        printFinalMethod(out, "private", typeNameTo, "get" + addHibernateToSlotName(capitalize(slotName)));
        startMethodBody(out);
        printWords(out, "return", slotExpression + ";");
        endMethodBody(out);
    }

    protected void generateHibernateSlotSetter(Slot slot, PrintWriter out) {
        String slotName = slot.getName();
        String typeNameFrom;
        String typeNameTo = slot.getTypeName();
        String setterExpression;

        if (ogmSupportsType(typeNameTo)) {
            typeNameFrom = typeNameTo;
            setterExpression = slotName;
        } else {
            typeNameFrom = "byte[]";
            setterExpression = "(" + typeNameTo + ")Externalization.internalizeObject(" + slotName + ")";
        }

        newline(out);
        printFinalMethod(out, "private", "void", "set" + addHibernateToSlotName(capitalize(slotName)),
                         makeArg(typeNameFrom, slotName));
        startMethodBody(out);
        printWords(out, getSlotExpression(slotName), "=", setterExpression + ";");
        endMethodBody(out);
    }

    protected void generateHibernateGetterBody(String slotName, String typeName, PrintWriter out) {
        printWords(out, "return", getSlotExpression(slotName) + ";");
    }

    // smf: This code is the same as in ISPN backend. Should refactor.
    @Override
    protected void generateStaticRoleSlotsMultOne(Role role, Role otherRole, PrintWriter out) {
        generateRoleMethodAdd(role, otherRole, out);
        generateRoleMethodRemove(role, otherRole, out);
    }

    // smf: This code is the same as in ISPN backend. Should refactor.
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

    // smf: This code is the same as in ISPN backend. Should refactor.
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

    protected void generateRoleMethodGetInverseRole(Role role, Role otherRole, PrintWriter out) {
        // the getInverseRole method
        String inverseRoleType = makeGenericType("pt.ist.fenixframework.dml.runtime.Role",
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
    }

    @Override
    protected void generateSlot(Slot slot, PrintWriter out) {
        this.ormTransientSlots.add(slot.getName());
        onNewline(out);
        // printWords(out, "private", getReferenceType(slot.getTypeName()), slot.getName());
        printWords(out, "private", slot.getTypeName(), slot.getName());
        print(out, ";");
    }

    protected String makeForeignKeyName(String name) {
        return "fk$" + name;
    }

    @Override
    protected void generateRoleSlot(Role role, PrintWriter out) {
        ormAddRole(role);
        onNewline(out);

        if (role.getMultiplicityUpper() == 1) {
            printWords(out, "private", PRIMARY_KEY_TYPE, makeForeignKeyName(role.getName()) + ";");
            newline(out);
            printWords(out, "private", "transient", getTypeFullName(role.getType()), role.getName());
            // ormGenerateRoleSlotMultOne(role);
        } else {
            printWords(out, "private", "transient", getSetTypeDeclarationFor(role), role.getName());
            // printWords(out, "=", getNewRoleStarSlotExpressionWithEmptySet(role));

            // // // println(out, ";");
            // // // onNewline(out);
            // // // printWords(out, "private", getSetTypeDeclarationFor(role), addHibernateToSlotName(role.getName()));
            // // // printWords(out, "=", "new", makeGenericType("java.util.HashSet", getTypeFullName(role.getType())) + "()");
            // ormGenerateRoleSlotMultMany(role);
        }
        println(out, ";");
    }

    protected String getSetTypeDeclarationFor(Role role) {
        String elemType = getTypeFullName(role.getType());
        return makeGenericType("java.util.Set", elemType);
        // String thisType = getTypeFullName(role.getOtherRole().getType());
        // return makeGenericType(getRelationAwareBaseTypeFor(role), thisType, elemType);
    }

    protected String getConcreteSetTypeDeclarationFor(Role role) {
        String elemType = getTypeFullName(role.getType());
        // return makeGenericType("java.util.Set", elemType);
        String thisType = getTypeFullName(role.getOtherRole().getType());
        return makeGenericType(getRelationAwareBaseTypeFor(role), thisType, elemType);
    }

    @Override
    protected void generateRoleSlotMethodsMultOne(Role role, PrintWriter out) {
        super.generateRoleSlotMethodsMultOne(role, out);
        generateRoleSlotMethodsMultOneHibernateFK(role, out);
        generateRoleSlotMethodsMultOneInternalSetter(role, out);
    }

    @Override
    protected void generateRoleSlotMethodsMultOneGetter(String slotName, String typeName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "public", typeName, "get" + capitalize(slotName));
        startMethodBody(out);
        
        generateGetterDAPStatement(dC, slotName, typeName, out);//DAP read stats update statement
        
        generateRoleSlotMethodsMultOneGetterUpdateFromFK(slotName, makeForeignKeyName(slotName),
                                                         out);
        printWords(out, "return", getSlotExpression(slotName) + ";");
        endMethodBody(out);
    }

    protected void generateRoleSlotMethodsMultOneGetterUpdateFromFK(String slotName,
                                                                    String fkSlotName,
                                                                    PrintWriter out) {
        print(out, "if (" + slotName + " == null && " + fkSlotName + " != null)");
        newBlock(out);
        print(out, getSlotExpression(slotName) + " = OgmBackEnd.getInstance().getDomainObject(" +
              fkSlotName + ");");
        closeBlock(out);
    }

    protected void generateRoleSlotMethodsMultOneGetterUpdateToFK(String fkSetterName,
                                                                  String slotName,
                                                                  PrintWriter out) {
        print(out, fkSetterName + "(" + slotName + " == null ? null : " + slotName + ".getExternalId());");
    }

    protected void generateRoleSlotMethodsMultOneHibernateFK(Role role, PrintWriter out) {
        String slotName = makeForeignKeyName(role.getName());
        generateRoleSlotMethodsMultOneHibernateFkGetter(slotName, out);
        generateRoleSlotMethodsMultOneHibernateFkSetter(slotName, out);
    }

    protected void generateRoleSlotMethodsMultOneHibernateFkGetter(String slotName, PrintWriter out) {
        // for now we can reuse generateGetter
        //generateGetter("", "get" + capitalize(slotName), slotName, PRIMARY_KEY_TYPE, out);
        
        newline(out);
        printFinalMethod(out, "", PRIMARY_KEY_TYPE, "get" + capitalize(slotName));
        startMethodBody(out);
        //generateGetterBody(slotName, PRIMARY_KEY_TYPE, out);
        printWords(out, "return", getSlotExpression(slotName) + ";");
        endMethodBody(out);
    }

    // ideally, this would use generateSetter, but that method now takes the domain class for the
    // indexes :-/ (which should actually be using generateSlotSetter instead :-/)
    protected void generateRoleSlotMethodsMultOneHibernateFkSetter(String slotName, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "", "void", "set" + capitalize(slotName), makeArg(PRIMARY_KEY_TYPE, slotName));
        startMethodBody(out);
        printWords(out, getSlotExpression(slotName), "=", slotName + ";");
        endMethodBody(out);
    }


    // smf: This code is similar to the ISPN backend. Should refactor (consider this method name in superclass?)
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
        println(out, getSlotExpression(slotName) + " = " + slotName + ";");
        generateRoleSlotMethodsMultOneGetterUpdateToFK("set" + capitalize(makeForeignKeyName(slotName)),
                                                       slotName, out);
        endMethodBody(out);
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {
        super.generateRoleSlotMethodsMultStar(role, out);

        // String paramListType = makeGenericType("java.util.List", getTypeFullName(role.getType()));

        // generateIteratorMethod(role, out);
        generateRoleMultGetterSetter(role, out);
    }

    // @Override
    // protected void generateIteratorMethod(Role role, PrintWriter out) {
    //     newline(out);
    //     printFinalMethod(out, "public", makeGenericType("java.util.Iterator", getTypeFullName(role.getType())), "get"
    //                      + capitalize(role.getName()) + "Iterator");
    //     startMethodBody(out);
    //     printWords(out, "return", getSlotExpression(role.getName()));
    //     print(out, ".iterator();");
    //     endMethodBody(out);
    // }

    protected void generateRoleMultGetterSetter(Role role, PrintWriter out) {
        generateRoleMultGetter(role, out);
        generateRoleMultSetter(role, out);
    }

    protected void generateRoleMultGetter(Role role, PrintWriter out) {
        String roleName = role.getName();
        String slotExpression = getSlotExpression(roleName);

        newline(out);
        println(out, "@javax.persistence.Access(javax.persistence.AccessType.PROPERTY)");
        printFinalMethod(out, "public", getSetTypeDeclarationFor(role), "get" + addHibernateToSlotName(capitalize(roleName)));
        startMethodBody(out);
        // we need to check for null, because save() runs in the top-level class's constructor,
        // which means that at that time, initInstance() hasn't yet executed
        println(out, "if (" + slotExpression + " == null) { return null; }");
        printWords(out, "return", "((" + getRelationAwareTypeFor(role) + ")" + slotExpression + ").getToHibernate();");
        endMethodBody(out);
    }

    protected void generateRoleMultSetter(Role role, PrintWriter out) {
        newline(out);
        printFinalMethod(out, "private", "void", "set" + addHibernateToSlotName(capitalize(role.getName())),
                         makeArg(getSetTypeDeclarationFor(role), role.getName()));
        startMethodBody(out);
        printWords(out, "((" + getRelationAwareTypeFor(role) + ")this." + role.getName() + ").setFromHibernate(" + role.getName() + ");");
        endMethodBody(out);
    }

    @Override
    protected String getNewRoleStarSlotExpression(Role role) {
        return getNewRoleStarSlotExpressionWithBackingSet(role, role.getName());
    }

    protected String getNewRoleStarSlotExpressionWithEmptySet(Role role) {
        StringBuilder buf = new StringBuilder();
        buf.append("new ");
        buf.append(makeGenericType("java.util.HashSet", getTypeFullName(role.getType())));
        buf.append("()");
        return getNewRoleStarSlotExpressionWithBackingSet(role, buf.toString());
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

    protected String addHibernateToSlotName(String slotName) {
        return slotName + "$via$hibernate";
    }


    ////////////////////////////////////////////// import from TPCW above


    @Override
    protected String getRoleOneBaseType() {
        return "pt.ist.fenixframework.dml.runtime.Role";
    }

    @Override
    protected String getRelationAwareBaseTypeFor(Role role) {
        // FIXME: handle other types of collections other than sets
        return "pt.ist.fenixframework.backend.ogm.RelationSet";
    }


    ///////////////////////////////////////////////////////////////////////////
    // Below are methods specific to the generation of the ORM mapping XML file

    protected void ormBeginFile() {
        StringBuilder buf = new StringBuilder();
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n");
        buf.append("<entity-mappings\n");
        buf.append("    xmlns=\"http://java.sun.com/xml/ns/persistence/orm\"\n");
        buf.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        buf.append("    xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence/orm orm_2_0.xsd\"\n");
        buf.append("    version=\"2.0\">\n\n");

        buf.append("    <mapped-superclass class=\"" + getDomainClassRoot() + "\" access=\"FIELD\" metadata-complete=\"false\">\n");
        buf.append("    </mapped-superclass>\n\n");
        this.ormWriter.print(buf.toString());
    }

    protected void ormBeginBaseClass(DomainClass domClass) {
        this.ormSlots = new ArrayList<String>();
        this.ormSlotsForRelationToOne = new ArrayList<String>();
        this.ormRoleManyToOne = new ArrayList<Role>();
        this.ormRoleOneToMany = new ArrayList<Role>();
        // this.ormRoleOneToOne = new ArrayList<Role>();
        this.ormRoleManyToMany = new ArrayList<Role>();
        this.ormTransientSlots = new ArrayList<String>();

        StringBuilder buf = new StringBuilder();
        buf.append("    <mapped-superclass class=\"");
        buf.append(domClass.getPackageName());
        buf.append(".");
        buf.append(domClass.getBaseName());
        buf.append("\" metadata-complete=\"false\">\n");
        buf.append("        <attributes>\n");
        this.ormWriter.print(buf.toString());
    }

    protected void ormEndBaseClass() {
        // slots must be dumped in this order per the schema definition :-/
        for (String name : this.ormSlots) {
            ormGenerateSlot(name);
        }
        for (String name : this.ormSlotsForRelationToOne) {
            ormGenerateSlot(name);
        }
        for (Role role : this.ormRoleManyToOne) {
            ormGenerateRoleManyToOne(role);
        }
        for (Role role : this.ormRoleOneToMany) {
            ormGenerateRoleOneToMany(role);
        }
        // for (Role role : this.ormRoleOneToOne) {
        //     ormGenerateRoleOneToOne(role);
        // }
        for (Role role : this.ormRoleManyToMany) {
            ormGenerateRoleManyToMany(role);
        }
        for (String name : this.ormTransientSlots) {
            ormGenerateTransient(name);
        }

        StringBuilder buf = new StringBuilder();
        buf.append("        </attributes>\n");
        buf.append("    </mapped-superclass>\n");
        this.ormWriter.println(buf.toString());
    }

    // protected void ormGenerateSlot(Slot slot) {
    //     ormGenerateSlot(slot.getName());
    // }

    protected void ormGenerateSlot(String slotName) {
        StringBuilder buf = new StringBuilder();
        buf.append("            <basic name=\"");
        buf.append(slotName);
        buf.append("\"");
        buf.append(" access=\"PROPERTY\"");
        buf.append(" />");
        this.ormWriter.println(buf.toString());
    }

    protected void ormGenerateRoleManyToOne(Role role) {
        StringBuilder buf = new StringBuilder();
        buf.append("            <many-to-one name=\"");
        buf.append(role.getName());
        buf.append("\" target-entity=\"");
        buf.append(getTypeFullName(role.getType()));
        buf.append("\">");
        // buf.append(ormGetCascade());
        buf.append("</many-to-one>");
        this.ormWriter.println(buf.toString());
    }

    protected void ormGenerateRoleOneToMany(Role role) {
        StringBuilder buf = new StringBuilder();
        buf.append("            <one-to-many name=\"");
        buf.append(addHibernateToSlotName(role.getName()));
        buf.append("\" target-entity=\"");
        buf.append(getTypeFullName(role.getType()));
        buf.append("\" access=\"PROPERTY\">");
        buf.append("</one-to-many>");
        this.ormWriter.println(buf.toString());
    }

    // protected void ormGenerateRoleOneToOne(Role role) {
    //     StringBuilder buf = new StringBuilder();
    //     buf.append("            <one-to-one fetch=\"LAZY\" name=\"");
    //     buf.append(role.getName());
    //     buf.append("\">");
    //     // buf.append(ormGetCascade());
    //     buf.append("</one-to-one>");
    //     this.ormWriter.println(buf.toString());
    // }

    protected void ormGenerateRoleManyToMany(Role role) {
        StringBuilder buf = new StringBuilder();
        buf.append("            <many-to-many name=\"");
        buf.append(addHibernateToSlotName(role.getName()));
        buf.append("\" access=\"PROPERTY\">");
        // buf.append(ormGetCascade());
        buf.append("</many-to-many>");
        this.ormWriter.println(buf.toString());
    }

    protected void ormGenerateTransient(String name) {
        StringBuilder buf = new StringBuilder();
        buf.append("            <transient name=\"");
        buf.append(name);
        buf.append("\" />");
        this.ormWriter.println(buf.toString());
    }

    protected String ormGetCascade() {
        return "<cascade><cascade-persist/><cascade-merge/><cascade-refresh/><cascade-detach/></cascade>";
    }

    protected String ormGetRoleMultiplicity(Role role) {
        return (role.getMultiplicityUpper() == 1) ? "one" : "many";
    }

    protected void ormGenerateNonBaseClasses(Iterator classesIter) {
        StringBuilder buf = new StringBuilder();

        buf.append("    <!-- The concrete classes.  Both 'class' and 'name' are required so that\n");
        buf.append("         hibernate does not use each class's simple name, voiding the\n");
        buf.append("         namespace :-/ -->\n");

        // buf.append("    <entity class=\"pt.ist.fenixframework.core.AbstractDomainObject\" name=\"pt.ist.fenixframework.core.AbstractDomainObject\" access=\"FIELD\" metadata-complete=\"false\">\n");
        // buf.append("        <attributes>\n");
        // buf.append("            <id name=\"hibernate$primaryKey\" />\n");
        // buf.append("        </attributes>\n");
        // buf.append("        <!--<inheritance strategy=\"\">-->\n");
        // buf.append("    </entity>\n\n");

        while (classesIter.hasNext()) {
            String className = getEntityFullName((DomainClass) classesIter.next());
            buf.append("    <entity class=\"");
            // buf.append(getEntityFullName((DomainClass) classesIter.next()));
            buf.append(className);
            buf.append("\" name=\"");
            buf.append(className);
            buf.append("\" metadata-complete=\"true\"/>\n");
        }
        this.ormWriter.println(buf.toString());
    }

    protected void ormEndFile() {
        this.ormWriter.println("</entity-mappings>");
    }

    protected void ormAddSlot(Slot slot) {
        this.ormSlots.add(slot.getName());
    }

    protected void ormAddSlotForRelationToOne(String name) {
        this.ormSlotsForRelationToOne.add(name);
    }

    protected void ormAddRole(Role role) {
        Role otherRole = role.getOtherRole();

        if (role.getMultiplicityUpper() == 1) {
            this.ormSlotsForRelationToOne.add(makeForeignKeyName(role.getName()));
            this.ormTransientSlots.add((role.getName()));
            // if (otherRole.getMultiplicityUpper() == 1) {
            //     // this.ormRoleOneToOne.add(role);
            // } else {
            //     this.ormRoleManyToOne.add(role);
            // }
        } else {
            this.ormTransientSlots.add(role.getName());
            if (otherRole.getMultiplicityUpper() == 1) {
                this.ormRoleOneToMany.add(role);
            } else {
                this.ormRoleManyToMany.add(role);
            }
        }


        // ArrayList<Role> theList =
        //     (role.getMultiplicityUpper() == 1
        //      ? (otherRole.getMultiplicityUpper() == 1 ?
        //         this.ormRoleOneToOne : this.ormRoleManyToOne)
        //      : (otherRole.getMultiplicityUpper() == 1 ?
        //         this.ormRoleOneToMany : this.ormRoleManyToMany));
        // theList.add(role);
             
        

        // if (role.getMultiplicityUpper() == 1) {
        //     if (otherRole.getMultiplicityUpper() == 1) {
        //         this.ormRoleOneToOne.add(role);
        //     } else {
        //         this.ormRoleManyToOne.add(role);
        //     }
        // } else {
        //     if (otherRole.getMultiplicityUpper() == 1) {
        //         this.ormRoleOneToMany.add(role);
        //     } else {
        //         this.ormRoleManyToMany.add(role);
        //     }
        // }
    }

}
