package pt.ist.fenixframework.dml;

import java.io.PrintWriter;
import java.util.Iterator;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.adt.bplustree.IBPlusTree;
import pt.ist.fenixframework.indexes.InitializerBPlusTree;

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
public class IndexesCodeGenerator extends DefaultCodeGenerator {

    public static final String FENIX_FRAMEWORK_FULL_CLASS = FenixFramework.class.getName();
    /** Cannot refer directly to the BPlusTree.class because that would load the class into the VM, and thus load the base class.
     * That is a problem because this class (the CodeGenerator) is loaded when passed to the DmlCompiler. And only after that, will 
     * the base class ever be generated. Thus we have a cyclic dependency and must break it by only using the BPlusTree name. */
    public static final String BPLUS_TREE_FULL_CLASS = "pt.ist.fenixframework.adt.bplustree.BPlusTree";
    public static final String INTERFACE_BPLUS_TREE_FULL_CLASS = IBPlusTree.class.getName();
    public static final String INITIALIZER_BPLUS_TREE_FULL_CLASS = InitializerBPlusTree.class.getName();

    public IndexesCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
    }

    @Override
    protected void generateBaseClassBody(DomainClass domClass, PrintWriter out) {
        super.generateBaseClassBody(domClass, out);
        generateIndexMethods(domClass, out);
    }
    
    @Override
    protected void generateSetterBody(DomainClass domainClass, String setterName, Slot slot, PrintWriter out) {
        generateIndexationInSetter(domainClass, slot, out);
        super.generateSetterBody(domainClass, setterName, slot, out);
    }
    
    protected void generateIndexationInSetter(DomainClass domainClass, Slot slot, PrintWriter out) {
	if (!slot.hasIndexedAnnotation()) {
	    return;
	}
	// Check if the previous field was null. If not, remove it from the index.
	boolean slotMayBeNull = mayBeNull(slot.getSlotType());
	if (slotMayBeNull) {
	    print(out, "if (");
	    print(out, "get" + capitalize(slot.getName() + "()"));
	    print(out, " != null)");
	    newBlock(out);
	}
	
	print(out, getStaticFieldName(slot.getName()));
	print(out, ".remove(");
	print(out, "get" + capitalize(slot.getName() + "()"));
	print(out, ");");
	
	if (slotMayBeNull) {
	    closeBlock(out);
	}

	// Check if the new field value is null. If not, add it to the index.
	if (slotMayBeNull) {
	    print(out, "if (");
	    print(out, slot.getName());
	    print(out, " != null)");
	    newBlock(out);
	}
	onNewline(out);
	print(out, getStaticFieldName(slot.getName()));
	print(out, ".insert(");
	print(out, slot.getName());
	print(out, ", (");
	print(out, domainClass.getFullName());
	print(out, ")this);");
	
	if (slotMayBeNull) {
	    closeBlock(out);
	}
        onNewline(out);
    }

    private static final String[] primitiveTypes = { "byte", "short", "int", "long", "float", "double", "boolean", "char" };

    private boolean mayBeNull(ValueType slotType) {
	String name = slotType.getFullname();
	for (String primitiveType : primitiveTypes) {
	    if (primitiveType.equals(name)) {
		return false;
	    }
	}
	return true;
    }

    protected String getIndexedFieldKey(String fullDomainClassName, String slotName) {
	return "\"" + fullDomainClassName + "." + slotName + "\"";
    }

    protected void generateIndexMethods(DomainClass domainClass, PrintWriter out) {
	Iterator<Slot> slotsIter = domainClass.getSlots();
	while (slotsIter.hasNext()) {
	    generateSlotSearchIndex(domainClass, (Slot) slotsIter.next(), out);
	}
    }

    protected void generateSlotSearchIndex(DomainClass domainClass, Slot slot, PrintWriter out) {
	if (slot.hasIndexedAnnotation()) {
	    generateStaticIndexField(domainClass, slot, out);
	    generateStaticIndexMethod(domainClass, slot, out);
	}
    }

    private void generateStaticIndexField(DomainClass domainClass, Slot slot, PrintWriter out) {
	newline(out);
	
	print(out, "public static transient ");
	print(out, INTERFACE_BPLUS_TREE_FULL_CLASS);
	print(out, "<");
	print(out, domainClass.getFullName());
	print(out, ">");
	print(out, " ");
	print(out, getStaticFieldName(slot.getName()));
	print(out, " = new ");
	print(out, INITIALIZER_BPLUS_TREE_FULL_CLASS);
	print(out, "<");
	print(out, domainClass.getFullName());
	print(out, ">");
	print(out, "(");
	print(out, getIndexedFieldKey(domainClass.getFullName(), slot.getName()));
	print(out, ", ");
	print(out, domainClass.getBaseName());
	print(out, ".class, \"");
	print(out, getStaticFieldName(slot.getName()));
	print(out, "\");");
	
	newline(out);
    }
    
    private String getStaticFieldName(String slotName) {
	return slotName + "$index";
    }

    protected void generateStaticIndexMethod(DomainClass domainClass, Slot slot, PrintWriter out) {
	newline(out);

	printFinalMethod(out, "public static", domainClass.getFullName(), getStaticIndexMethodName(slot), makeArg(slot.getTypeName(), slot.getName()));

	startMethodBody(out);
	generateStaticIndexMethodBody(domainClass.getFullName(), slot, out);
	endMethodBody(out);
    }

    protected void generateStaticIndexMethodBody(String fullDomainClassName, Slot slot, PrintWriter out) {
	// Generate the search
	print(out, "return ");
	print(out, getStaticFieldName(slot.getName()));
	print(out, ".get(");
	print(out, slot.getName());
	print(out, ");");
    }

    protected String getStaticIndexMethodName(Slot slotName) {
	return "findBy" + slotName.getName().substring(0, 1).toUpperCase() + slotName.getName().substring(1);
    }

}
