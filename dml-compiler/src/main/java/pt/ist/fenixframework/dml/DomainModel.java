package pt.ist.fenixframework.dml;

import java.util.*;
import java.io.Serializable;
import java.net.URL;

public class DomainModel implements Serializable {

    protected Map<String, ValueType> valueTypes = new HashMap<String, ValueType>();
    protected Map<String, DomainEntity> external = new HashMap<String, DomainEntity>();
    protected Map<String, DomainClass> classes = new HashMap<String, DomainClass>();
    protected Map<String, DomainRelation> relations = new HashMap<String, DomainRelation>();
    private boolean finalized = false;

    public DomainModel() {
	initializeBuiltinValueTypes();
	initializeBuiltinEntities();
    }

    private static String[] NON_NULLABLE_TYPES = { "boolean", "byte", "char", "short", "int", "float", "long", "double" };

    public static boolean isNullableType(ValueType vt) {
	String vtFullName = vt.getFullname();
	for (String nonNullableType : NON_NULLABLE_TYPES) {
	    if (nonNullableType.equals(vtFullName)) {
		return false;
	    }
	}
	return true;
    }

    public static boolean isNullableTypeFullName(String vtFullName) {
	for (String nonNullableType : NON_NULLABLE_TYPES) {
	    if (nonNullableType.equals(vtFullName)) {
		return false;
	    }
	}
	return true;
    }

    private static String[][] builtinValueTypes = { /* { fullname, alias } */
        // primitive types
	{ "boolean", "boolean" },
	{ "byte", "byte" },
	{ "char", "char" },
	{ "short", "short" },
	{ "int", "int" },
	{ "float", "float" },
	{ "long", "long" },
	{ "double", "double" },

        // their wrappers
	{ "java.lang.Boolean", "Boolean" },
	{ "java.lang.Byte", "Byte" },
	{ "java.lang.Character", "Character" },
	{ "java.lang.Short", "Short" },
	{ "java.lang.Integer", "Integer" },
	{ "java.lang.Float", "Float" },
	{ "java.lang.Long", "Long" },
	{ "java.lang.Double", "Double" },

        // String is, of course, essential
	{ "java.lang.String", "String" },

	// we need something binary, also
	{ "byte[]", "bytearray" },

	// JodaTime types
	{ "org.joda.time.DateTime", "DateTime" },
	{ "org.joda.time.LocalDate", "LocalDate" },
	{ "org.joda.time.LocalTime", "LocalTime" },
	{ "org.joda.time.Partial", "Partial" },

        // also anything Serializable is acceptable
	{ "java.io.Serializable", "Serializable" }
    };

    protected void initializeBuiltinValueTypes() {
        for (String[] valueType : builtinValueTypes) {
            newValueType(valueType[1], valueType[0]);
        }
        // // primitive types
	// newValueType("boolean", "boolean");
	// newValueType("byte", "byte");
	// newValueType("char", "char");
	// newValueType("short", "short");
	// newValueType("int", "int");
	// newValueType("float", "float");
	// newValueType("long", "long");
	// newValueType("double", "double");

        // // their wrappers
	// newValueType("Boolean", "java.lang.Boolean");
	// newValueType("Byte", "java.lang.Byte");
	// newValueType("Character", "java.lang.Character");
	// newValueType("Short", "java.lang.Short");
	// newValueType("Integer", "java.lang.Integer");
	// newValueType("Float", "java.lang.Float");
	// newValueType("Long", "java.lang.Long");
	// newValueType("Double", "java.lang.Double");

        // // String is, of course, essential
	// newValueType("String", "java.lang.String");

	// // we need something binary, also
	// newValueType("bytearray", "byte[]");

	// // JodaTime types
	// newValueType("DateTime", "org.joda.time.DateTime");
	// newValueType("LocalDate", "org.joda.time.LocalDate");
	// newValueType("LocalTime", "org.joda.time.LocalTime");
	// newValueType("Partial", "org.joda.time.Partial");

        // // also anything Serializable is acceptable
	// newValueType("Serializable", "java.io.Serializable");
    }

    public static boolean isBuiltinValueTypeFullName(String name) {
        for (String[] valueType : builtinValueTypes) {
            if (valueType[0].equals(name)) {
                return true;
            }
        }
        return false;
    }


    private static String[][] builtinEntities = { /* { fullname, alias } */
        { "pt.ist.fenixframework.DomainObject", "DomainObject" },
        { "pt.ist.fenixframework.core.AbstractDomainObject", "AbstractDomainObject" }
    };

    protected void initializeBuiltinEntities() {
        for (String[] entity : builtinEntities) {
            addExternalEntity(null, entity[0], entity[1]);
        }
    }

    protected boolean isBuiltinEntity(String name) {
        for (String[] entity : builtinEntities) {
            if (entity[0].equals(name) || entity[1].equals(name)) {
                return true;
            }
        }
        return false;
    }

    public DomainEntity findClassOrExternal(String name) {
	DomainEntity domClass = findClass(name);
	if (domClass == null) {
	    domClass = external.get(name);
	}
	return domClass;
    }

    public DomainClass findClass(String name) {
	return classes.get(name);
    }

    public DomainRelation findRelation(String name) {
	return relations.get(name);
    }

    public void addClass(DomainClass domClass) {
	checkNotFinalized();
	checkNameUnique(domClass.getFullName());
	classes.put(domClass.getFullName(), domClass);
    }

    public void addRelation(DomainRelation domRelation) {
	checkNotFinalized();
	checkNameUnique(domRelation.getFullName());
	relations.put(domRelation.getFullName(), domRelation);
    }

    public void addExternalEntity(URL sourceFile, String name) {
	addExternalEntity(sourceFile, name, name);
    }

    public void addExternalEntity(URL sourceFile, String name, String aliasName) {
	if (aliasName == null) {
	    aliasName = name;
	}
	checkNotFinalized();
	DomainExternalEntity ent = new DomainExternalEntity(sourceFile, name);
	external.put(aliasName, ent);
	if (!aliasName.equals(name)) {
	    external.put(name, ent);
	}
    }

    public Iterator<DomainClass> getClasses() {
	return classes.values().iterator();
    }

    public Collection<DomainClass> getDomainClasses() {
	return classes.values();
    }

    public Iterator<DomainRelation> getRelations() {
	return relations.values().iterator();
    }

    public Collection<DomainRelation> getDomainRelations() {
	return relations.values();
    }
    
    public void newValueType(String domainName, String fullName) {
	ValueType valueType = new PlainValueType(fullName);
	newValueType(domainName, valueType);
    }

    public void newValueType(String domainName, ValueType valueType) {
	if (domainName == null) {
	    domainName = valueType.getFullname();
	}
	valueType.getBaseType().setDomainName(domainName);
        checkValueTypeName(domainName);
	valueTypes.put(domainName, valueType);
    }

    public void newEnumType(String domainName, String fullName) {
	if (domainName == null) {
	    domainName = fullName;
	}
        checkValueTypeName(domainName);
	valueTypes.put(domainName, new EnumValueType(domainName, fullName));
    }

    public Collection<ValueType> getAllValueTypes() {
        return valueTypes.values();
    }

    public ValueType findValueType(String name) {
	return valueTypes.get(name);
    }

    public boolean isEnumType(String valueTypeName) {
	ValueType vt = findValueType(valueTypeName);
	return ((vt != null) && vt.isEnum());
    }

    public void finalizeDomain() {
	finalizeDomain(false);
    }

    public void finalizeDomain(boolean checkForMissingExternals) {
	// go through each of the relations and add their slots to the
	// corresponding classes...
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
	    r0.getType().addRoleSlot(r1);
	    r1.getType().addRoleSlot(r0);
	}

	checkForRepeatedSlots();
	
	if (checkForMissingExternals) {
	    for (String externalName : external.keySet()) {
		if (!isBuiltinEntity(externalName) && !classes.containsKey(externalName)) {
		    throw new RuntimeException(externalName
			    + " was defined as an external entity but there is no concrete definition of it!");
		}
	    }
	}
	finalized = true;
    }

    protected void checkForRepeatedSlots() {
	for (DomainClass domClass : classes.values()) {
	    DomainEntity superDomClass = domClass.getSuperclass();
	    if (superDomClass != null) {
		for (Slot slot : domClass.getSlotsList()) {
		    if (superDomClass.findSlot(slot.getName()) != null) {
			System.err.printf("WARNING: Slot named '%s' in class '%s' already exists in a superclass\n", slot
				.getName(), domClass.getName());
		    }
		    if (superDomClass.findRoleSlot(slot.getName()) != null) {
			System.err.printf("WARNING: Slot named '%s' in class '%s' already exists in a superclass as role slot\n",
				slot.getName(), domClass.getName());
		    }
		}

		for (Role role : domClass.getRoleSlotsList()) {
		    if (superDomClass.findSlot(role.getName()) != null) {
			System.err.printf(
				"WARNING: Role slot named '%s' in class '%s' already exists in a superclass as a slot\n", role
					.getName(), domClass.getName());
		    }

		    if (superDomClass.findRoleSlot(role.getName()) != null) {
			System.err.printf(
				"WARNING: Role slot named '%s' in class '%s' already exists in a superclass as role slot\n", role
					.getName(), domClass.getName());
		    }
		}
	    }
	}
    }

    public String toString() {
	return "{ classes = " + classes + ", relations = " + relations + " }";
    }

    private void checkNotFinalized() {
	if (finalized) {
	    throw new RuntimeException("Cannot change Domain after finalization");
	}
    }

    private void checkValueTypeName(String name) {
        if (valueTypes.containsKey(name)) {
	    throw new RuntimeException("Duplicate name for value type: " + name);
        }
    }

    private void checkNameUnique(String name) {
	if (classes.containsKey(name) || relations.containsKey(name)) {
	    throw new RuntimeException("Duplicate name: " + name);
	}
    }
}
