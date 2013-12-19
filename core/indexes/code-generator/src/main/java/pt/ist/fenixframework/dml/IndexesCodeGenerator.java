package pt.ist.fenixframework.dml;

import java.io.PrintWriter;

import pt.ist.fenixframework.dml.runtime.DomainBasedMap;

/**
 * This code generator enhances the default generation by adding indexation to fields
 * annotated to have that behavior. To do so, it:
 * <ul>
 * 
 * <li>Changes setters to update the index (and initializes the index tree if needed)</li>
 * 
 * <li>Adds a static method to allow an index search by the field</li>
 * 
 * </ul>
 * 
 * @author nmld
 */
public class IndexesCodeGenerator extends TxIntrospectorCodeGenerator {

    // Unfortunately, depending on a DML entity cannot be done explicitly because LinkedList extends a _Base class which 
    // will not be compiled when this code generator is invoked (ultimately, to compile the LinkedList itself)
    private static final String LINKED_LIST_FULL_CLASS = "pt.ist.fenixframework.adt.linkedlist.LinkedList";

    public IndexesCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected void generateStaticKeyFunctionForRole(Role role, PrintWriter out) {
        if (role.isIndexed()) {
            onNewline(out);
            Slot indexedSlot = getIndexedSlot(role);
            String keyField = role.getIndexProperty();
            println(out,
                    generateMapKeyFunction(role.getName(), role.getType().getFullName(), indexedSlot.getSlotType().getFullname(),
                            keyField, role.getIndexCardinality() == Role.MULTIPLICITY_MANY));
            onNewline(out);
        } else {
            super.generateStaticKeyFunctionForRole(role, out);
        }
    }

    @Override
    protected void generateRoleSlotMethodsMultStar(Role role, PrintWriter out) {
        super.generateRoleSlotMethodsMultStar(role, out);
        boolean isIndexed = role.isIndexed();
        String typeName = getTypeFullName(role.getType());
        String slotName = role.getName();
        String capitalizedSlotName = capitalize(slotName);
        String slotAccessExpression = "get" + capitalizedSlotName + "()";
        String methodModifiers = getMethodModifiers(role);
        if (isIndexed) {
            generateRoleSlotMethodsMultStarIndexed(role, out, methodModifiers, capitalizedSlotName, slotAccessExpression,
                    typeName, slotName);
        }
    }

    protected void generateRoleSlotMethodsMultStarIndexed(Role role, PrintWriter out, String methodModifiers,
            String capitalizedSlotName, String slotAccessExpression, String typeName, String slotName) {
        Slot indexedSlot = getIndexedSlot(role);
        String keyField = role.getIndexProperty();
        String retType = role.getType().getFullName();
        String methodNameSufix = "";
        boolean haveMany = role.getIndexCardinality() == Role.MULTIPLICITY_MANY;
        if (haveMany) {
            retType = makeGenericType("java.util.Set", retType);
        }
        onNewline(out);
        printMethod(out, "public", retType, "get" + capitalize(role.getName()) + "By" + capitalize(keyField) + methodNameSufix,
                indexedSlot.getSlotType().getFullname() + " key");
        startMethodBody(out);
        printWords(out, "return", getSearchForKey(role, haveMany ? getCollectionToUse() : retType));
        print(out, ";");
        endMethodBody(out);
    }

    private Slot getIndexedSlot(Role role) {
        Slot indexedSlot = role.getType().findSlot(role.getIndexProperty());
        if (indexedSlot == null) { // indexed field doesn't exist
            throw new Error("Unknown indexed field: " + role.getIndexProperty());
        }
        return indexedSlot;
    }

    private String getSearchForKey(Role role, String retType) {
        boolean indexMult = role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY;
        String fetchMethod = "get" + (indexMult ? "Values" : "");
        return "((" + getRelationAwareTypeFor(role) + ") get" + capitalize(role.getName()) + "Set())." + fetchMethod + "(key)";
    }

    @Override
    protected String getDefaultCollectionFor(Role role) {
        if (role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY) {
            return makeGenericType(getCollectionToUse(), makeGenericType(LINKED_LIST_FULL_CLASS, role.getType().getFullName()));
        } else {
            return super.getDefaultCollectionFor(role);
        }
    }

    @Override
    protected String getDefaultCollectionGetterFor(Role role) {
        if (role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY) {
            return makeGenericType(DomainBasedMap.Getter.class.getCanonicalName(),
                    makeGenericType(LINKED_LIST_FULL_CLASS, getTypeFullName(role.getType())));
        } else {
            return super.getDefaultCollectionGetterFor(role);
        }
    }

    @Override
    protected String getRelationAwareBaseTypeFor(Role role) {
        if (role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY) {
            return RelationMulValuesIndexedAwareSet.class.getName();
        } else {
            return super.getRelationAwareBaseTypeFor(role);
        }
    }

}
