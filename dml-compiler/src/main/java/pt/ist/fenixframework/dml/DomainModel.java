package pt.ist.fenixframework.dml;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

import pt.ist.fenixframework.DomainObject;

public class DomainModel implements Serializable {

    protected Map<String, ValueType> valueTypes = new HashMap<String, ValueType>();
    protected Map<String, DomainEntity> external = new HashMap<String, DomainEntity>();
    protected Map<String, DomainClass> classes = new HashMap<String, DomainClass>();
    protected Map<String, DomainRelation> relations = new HashMap<String, DomainRelation>();

    private final Collection<ListenerRegistration<DeletionListener<?>>> deletionListeners = new ConcurrentLinkedQueue<>();
    private final Collection<ListenerRegistration<DeletionBlockerListener<?>>> blockerListeners = new ConcurrentLinkedQueue<>();
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
            { "boolean", "boolean" }, { "byte", "byte" }, { "char", "char" }, { "short", "short" }, { "int", "int" },
            { "float", "float" }, { "long", "long" }, { "double", "double" },

            // their wrappers
            { "java.lang.Boolean", "Boolean" }, { "java.lang.Byte", "Byte" }, { "java.lang.Character", "Character" },
            { "java.lang.Short", "Short" }, { "java.lang.Integer", "Integer" }, { "java.lang.Float", "Float" },
            { "java.lang.Long", "Long" }, { "java.lang.Double", "Double" },

            // String is, of course, essential
            { "java.lang.String", "String" },

            // we need something binary, also
            { "byte[]", "bytearray" },

            // JodaTime types
            { "org.joda.time.DateTime", "DateTime" }, { "org.joda.time.LocalDate", "LocalDate" },
            { "org.joda.time.LocalTime", "LocalTime" }, { "org.joda.time.Partial", "Partial" },

            // also anything Serializable is acceptable
            { "java.io.Serializable", "Serializable" },

            // we need JsonElement for we live in a JSON world
            { "com.google.gson.JsonElement", "JsonElement" } };

    protected void initializeBuiltinValueTypes() {
        for (String[] valueType : builtinValueTypes) {
            newValueType(valueType[1], valueType[0]);
        }
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
            { "pt.ist.fenixframework.core.AbstractDomainObject", "AbstractDomainObject" } };

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

    /**
     * Registers a new {@link DeletionListener} for the given type.
     * 
     * The listener will be invoked whenever an object compatible with the given type (i.e. the concrete type or
     * a sub-class) is being deleted (by calling its {@link DeletionListener#deleting(DomainObject)} method).
     * 
     * @param type
     *            The type for which this listener will be invoked.
     * @param listener
     *            The listener to register.
     * @throws IllegalStateException
     *             If the DomainModel is still being assembled.
     */
    public <T extends DomainObject> void registerDeletionListener(Class<T> type, DeletionListener<T> listener) {
        if (!finalized) {
            throw new IllegalStateException("Cannot register deletion listeners before the DomainModel is finalized!");
        }
        deletionListeners.add(new ListenerRegistration<DeletionListener<?>>(type, listener));
    }

    /**
     * Registers a new {@link DeletionBlockerListener} for the given type.
     * 
     * The listener will be invoked whenever an object compatible with the given type (i.e. the concrete type or
     * a sub-class) requests all its {@link DeletionBlocker}s.
     * 
     * @param type
     *            The type for which this listener will be invoked.
     * @param listener
     *            The listener to register.
     * @throws IllegalStateException
     *             If the DomainModel is still being assembled.
     */
    public <T extends DomainObject> void registerDeletionBlockerListener(Class<T> type, DeletionBlockerListener<T> listener) {
        if (!finalized) {
            throw new IllegalStateException("Cannot register deletion listeners before the DomainModel is finalized!");
        }
        blockerListeners.add(new ListenerRegistration<DeletionBlockerListener<?>>(type, listener));
    }

    /**
     * Returns all the registered {@link DeletionListener}s that are compatible with the given type.
     * 
     * @param type
     *            The type for which to retrieve the deletion listeners.
     * @return
     *         All the listeners for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> Iterable<DeletionListener<T>> getDeletionListenersForType(Class<?> type) {
        List<DeletionListener<T>> result = new ArrayList<>();
        for (ListenerRegistration<?> listener : deletionListeners) {
            if (listener.matches(type)) {
                result.add((DeletionListener<T>) listener.getListener());
            }
        }
        return result;
    }

    /**
     * Returns all the registered {@link DeletionBlockerListener}s that are compatible with the given type.
     * 
     * @param type
     *            The type for which to retrieve the deletion listeners.
     * @return
     *         All the listeners for the given type.
     */
    @SuppressWarnings("unchecked")
    public <T extends DomainObject> Iterable<DeletionBlockerListener<T>> getDeletionBlockerListenersForType(Class<?> type) {
        List<DeletionBlockerListener<T>> result = new ArrayList<>();
        for (ListenerRegistration<?> listener : blockerListeners) {
            if (listener.matches(type)) {
                result.add((DeletionBlockerListener<T>) listener.getListener());
            }
        }
        return result;
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
                    throw new RuntimeException(
                            externalName + " was defined as an external entity but there is no concrete definition of it!");
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
                        System.err.printf("WARNING: Slot named '%s' in class '%s' already exists in a superclass\n",
                                slot.getName(), domClass.getName());
                    }
                    if (superDomClass.findRoleSlot(slot.getName()) != null) {
                        System.err.printf("WARNING: Slot named '%s' in class '%s' already exists in a superclass as role slot\n",
                                slot.getName(), domClass.getName());
                    }
                }

                for (Role role : domClass.getRoleSlotsList()) {
                    if (superDomClass.findSlot(role.getName()) != null) {
                        System.err.printf(
                                "WARNING: Role slot named '%s' in class '%s' already exists in a superclass as a slot\n",
                                role.getName(), domClass.getName());
                    }

                    if (superDomClass.findRoleSlot(role.getName()) != null) {
                        System.err.printf(
                                "WARNING: Role slot named '%s' in class '%s' already exists in a superclass as role slot\n",
                                role.getName(), domClass.getName());
                    }
                }
            }
        }
    }

    @Override
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

    private static final class ListenerRegistration<T> {

        private final Class<?> type;
        private final T listener;

        public ListenerRegistration(Class<?> type, T listener) {
            this.type = Objects.requireNonNull(type);
            this.listener = Objects.requireNonNull(listener);
        }

        public boolean matches(Class<?> target) {
            return type.isAssignableFrom(target);
        }

        public T getListener() {
            return listener;
        }

    }

}
