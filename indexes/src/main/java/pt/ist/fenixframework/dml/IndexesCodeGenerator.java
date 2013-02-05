package pt.ist.fenixframework.dml;

import java.io.PrintWriter;

import pt.ist.fenixframework.FenixFramework;

/**
 * This code generator enhances the default generation by adding indexation to fields 
 * annotated to have that behavior. To do so, it:
 * <ul>
 *
 *  <li>Changes setters to update the index (and initializes the index tree if needed)</li>
 *
 *  <li>Adds a static method to allow an index search by the field</li>
 *
 * </ul>
 * @author nmld
 */
public class IndexesCodeGenerator extends TxIntrospectorCodeGenerator {

    public static final String FENIX_FRAMEWORK_FULL_CLASS = FenixFramework.class.getName();
    // Unfortunately, depending on a DML entity cannot be done explicitly because LinkedList extends a _Base class which 
    // will not be compiled when this code generator is invoked (ultimately, to compile the LinkedList itself)
    public static final String LINKED_LIST_FULL_CLASS = "pt.ist.fenixframework.adt.linkedlist.LinkedList";
    public static final String BPLUS_TREE_FULL_CLASS = "pt.ist.fenixframework.adt.bplustree.BPlusTree";

    public IndexesCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
	setCollectionToUse(BPLUS_TREE_FULL_CLASS);
    }

    @Override
    protected void generateStaticKeyFunctionForRole(Role role, PrintWriter out) {
    	if (role.isIndexed()) {
    	    onNewline(out);
    	    Slot indexedSlot = getIndexedSlot(role);
    	    String keyField = role.getIndexProperty();
    	    println(out, generateIndexKeyFunction(role.getName(), role.getType().getFullName(), indexedSlot.getSlotType()
    		    .getFullname(), keyField, role.getIndexCardinality() == Role.MULTIPLICITY_MANY));
    	    onNewline(out);
    	} else {
    	    super.generateStaticKeyFunctionForRole(role, out);
    	}
    }
    
    
    @Override
    protected String getDefaultCollectionFor(Role role) {
	if (role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY) {
	    return makeGenericType(super.getCollectionToUse(), makeGenericType(LINKED_LIST_FULL_CLASS, role.getType().getFullName()));
	} else {
	    return makeGenericType(super.getCollectionToUse(), role.getType().getFullName());
	}
    }
    
    protected String getRelationAwareBaseTypeFor(Role role) {
	if (role.isIndexed() && role.getIndexCardinality() == Role.MULTIPLICITY_MANY) {
	    return RelationMulValuesIndexedAwareSet.class.getName();
	} else {
	    return super.getRelationAwareBaseTypeFor(role);
	}
    }

}
