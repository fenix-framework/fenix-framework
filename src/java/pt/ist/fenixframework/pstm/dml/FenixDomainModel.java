package pt.ist.fenixframework.pstm.dml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dml.*;

public class FenixDomainModel extends DomainModel {

    private static Map<String,String> BUILT_IN_JDBC_MAP = new HashMap<String,String>();

    private static String[] NON_NULLABLE_TYPES = {
        "boolean", "byte", "char", "short", "int", "float", "long", "double"
    };

    public static boolean isNullableType(ValueType vt) {
        String vtFullName = vt.getFullname();
        for (String nonNullableType : NON_NULLABLE_TYPES) {
            if (nonNullableType.equals(vtFullName)) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected void initializeBuiltinValueTypes() {
        // replace the inherited built-in value-types with the following

        // primitive types
        registerFenixValueType("boolean", "boolean", "BIT");
        registerFenixValueType("byte", "byte", "INTEGER");
        registerFenixValueType("char", "char", "CHAR");
        registerFenixValueType("short", "short", "INTEGER");
        registerFenixValueType("int", "int", "INTEGER");
        registerFenixValueType("float", "float", "FLOAT");
        registerFenixValueType("long", "long", "BIGINT");
        registerFenixValueType("double", "double", "DOUBLE");

        // their wrappers
        registerFenixValueType("java.lang.Boolean", "Boolean", "BIT");
        registerFenixValueType("java.lang.Byte", "Byte", "INTEGER");
        registerFenixValueType("java.lang.Character", "Character", "CHAR");
        registerFenixValueType("java.lang.Short", "Short", "INTEGER");
        registerFenixValueType("java.lang.Integer", "Integer", "INTEGER");
        registerFenixValueType("java.lang.Float", "Float", "FLOAT");
        registerFenixValueType("java.lang.Long", "Long", "BIGINT");
        registerFenixValueType("java.lang.Double", "Double", "DOUBLE");

        // String is, of course, essential
        registerFenixValueType("java.lang.String", "String", "LONGVARCHAR");

        // we need something binary, also
        registerFenixValueType("byte[]", "bytearray", "BLOB");

        // JodaTime types
        registerFenixValueType("org.joda.time.DateTime", "DateTime", "TIMESTAMP");
        registerFenixValueType("org.joda.time.LocalDate", "LocalDate", "VARCHAR");
        registerFenixValueType("org.joda.time.LocalTime", "LocalTime", "TIME");
        registerFenixValueType("org.joda.time.Partial", "Partial", "LONGVARCHAR");

        // The JodaTime's Period class is dealt with in the Fenix app code base for the time being
        //registerFenixValueType("org.joda.time.Period", "Period", "");
    }

    protected void registerFenixValueType(String valueTypeName, String aliasName, String jdbcType) {
        newValueType(aliasName, valueTypeName);
        BUILT_IN_JDBC_MAP.put(aliasName, jdbcType);
    }

    /*
     * This method will need to be changed once we get rid of OJB.
     */
    public String getJdbcTypeFor(String valueType) {
        ValueType vt = findValueType(valueType);

        String jdbcType = null;

        if (vt.isEnum()) {
            jdbcType = "VARCHAR";
        } else if (vt.isBuiltin()) {
            jdbcType = BUILT_IN_JDBC_MAP.get(valueType);
        } else {
            List<ExternalizationElement> extElems = vt.getExternalizationElements();
            if (extElems.size() != 1) {
                throw new Error("Can't handle ValueTypes with more than one externalization element, yet!");
            }
            jdbcType = getJdbcTypeFor(extElems.get(0).getType().getDomainName());
        }

        if (jdbcType == null) {
            throw new Error("Couldn't find a JDBC type for the value type " + valueType);
        }

        return jdbcType;
    }


    /*
     * This must be reevaluated.  In Fenix there are several cases of
     * relations that are differentiations of another relation.
     * 
     * For instance, there are many relations between the
     * RootDomainObject and other classes, but there is only one
     * keyRootDomainObject in the DomainObject class, rather than one
     * for each subclass.
     *
     * I guess that the proper way to deal with this was to declare an
     * abstract relation between RootDomainObject and DomainObject,
     * and then add concrete relations between the different
     * subclasses of DomainObject and the RootDomainObject.
     *
     * This, however, needs further brainstorming...
     * So, I will leave this disabled for now.
     */
     public void finalizeDomain() {
         super.finalizeDomain();

         // go through each of the relations and add the foreign keys
         // for the roles with multiplicity one to the corresponding
         // classes...
         for (DomainRelation rel : relations.values()) {
             List<Role> roles = rel.getRoles();
             int numRoles = roles.size();
             if (numRoles != 2) {
                 if (numRoles > 2) {
                     throw new RuntimeException("Can't handle with more than two roles yet!");
                 }
             }

             Role r0 = roles.get(0);
             Role r1 = roles.get(1);

             // FIXME: the following cast to DomainClass is horrible.
             // The hierarchy DomainEntity -- DomainClass -- DomainRelation should be revised...
             addForeignKeySlotIfNeeded((DomainClass)r0.getType(), r1);
             addForeignKeySlotIfNeeded((DomainClass)r1.getType(), r0);
         }

         for (final DomainClass domainClass : classes.values()) {
             final DomainEntity domainEntity = domainClass.getSuperclass();
             final int domainClassHierarchyLevel = calculateHierarchyLevel(domainClass);
             if (domainClassHierarchyLevel > 1) {
                 final DomainClass domainObjectDescendent = findDirectDomainObjectDecendent(domainClass);
                 final Slot ojbConcreteClassSlot = domainObjectDescendent.findSlot("ojbConcreteClass");
                 if (ojbConcreteClassSlot == null) {
                     domainObjectDescendent.addSlot(new Slot("ojbConcreteClass", findValueType("String")));
                 }
             }
         }

         checkForRepeatedSlots();
     }

     private DomainClass findDirectDomainObjectDecendent(final DomainClass domainClass) {
         final int domainClassHierarchyLevel = calculateHierarchyLevel(domainClass);
         return domainClassHierarchyLevel == 1 ? domainClass : findDirectDomainObjectDecendent((DomainClass) domainClass.getSuperclass());
     }

     private int calculateHierarchyLevel(final DomainClass domainClass) {
         final DomainEntity domainEntity = domainClass.getSuperclass();
         return domainEntity == null || !isDomainClass(domainEntity) ? 0 : calculateHierarchyLevel((DomainClass) domainEntity) + 1;
     }

     private boolean isDomainClass(final DomainEntity domainEntity) {
         return domainEntity instanceof DomainClass;
     }

    private void addForeignKeySlotIfNeeded(DomainClass domClass, Role role) {
        if (role.getMultiplicityUpper() == 1) {
            String fkName = "key" + CodeGenerator.capitalize(role.getName());
            
            Slot existingSlot = domClass.findSlot(fkName);
            if (existingSlot != null) {
                if (existingSlot.getTypeName().equals("java.lang.Integer")) {
                    System.err.printf("The slot %s in class %s corresponds to a foreign key and should not be specified in the DML...\n", 
                                      fkName, 
                                      domClass.getName());
                } else {
                    System.err.printf("A slot with the name %s already exists in class %s!\n", 
                                      fkName, 
                                      domClass.getName());
                    System.exit(1);
                }
            }

            domClass.addSlot(new Slot(fkName, findValueType("Integer")));
        }
    }
}
