package pt.ist.fenixframework.dml;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import pt.ist.fenixframework.dml.runtime.Relation;

public class ValueTypeSerializationGenerator extends DefaultCodeGenerator {

    public static final String SERIALIZER_CLASS_PACKAGE = "pt.ist.fenixframework";
    public static final String SERIALIZER_CLASS_SIMPLE_NAME = "ValueTypeSerializer";
    public static final String SERIALIZER_CLASS_FULL_NAME = SERIALIZER_CLASS_PACKAGE + "." + SERIALIZER_CLASS_SIMPLE_NAME;
    public static final String SERIALIZATION_METHOD_PREFIX = "serialize$";
    public static final String DESERIALIZATION_METHOD_PREFIX = "deSerialize$";

    protected PrintWriter out;

    public ValueTypeSerializationGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
    }

    public static String getSerializedFormTypeName(ValueType vt) {
        List<ExternalizationElement> extElems = vt.getExternalizationElements();
        if (extElems.size() == 0) { // built-in type do not have externalizers
            return vt.getFullname();
        } else if (extElems.size() == 1) {
            // It's just a wrapper.  So, our serialized form is the same as our component's
            return getSerializedFormTypeName(extElems.get(0).getType());
        } else {
            // Alas, we need a full-blown SerializedForm for this one
            return makeSerializationValueTypeName(vt);
        }
    }


    @Override
    public void generateCode() {
	// create the ValueTypeSerializationGenerator before starting the "standard" generation.
	File file = new File(getBaseDirectoryFor(SERIALIZER_CLASS_PACKAGE), SERIALIZER_CLASS_SIMPLE_NAME + ".java");
	try {
	    file.getParentFile().mkdirs();
	    this.out = new PrintWriter(new FileWriter(file), true);
	} catch (java.io.IOException ioe) {
	    throw new Error("Can't open file" + file);
	}

	generateFilePreamble(SERIALIZER_CLASS_PACKAGE, this.out);
	newline(out);
	printWords(out, "public", "final", "class", SERIALIZER_CLASS_SIMPLE_NAME);
	newBlock(out);
	generateValueTypeSerializations();
	closeBlock(out);
    }
    
    protected void generateValueTypeSerializations() {
	for (ValueType vt : getDomainModel().getAllValueTypes()) {
	    if (!(vt.isBuiltin() || vt.isEnum())) {
		println(out, "");
		print(out, "// VT: " + vt.getDomainName() + " serializes as " + getSerializedFormTypeName(vt));

                List<ExternalizationElement> extElems = vt.getExternalizationElements();
                if (extElems.size() > 1) { // because 1-externalizer VTs unwrap and use their component's type
                    generateValueTypeSerializableForm(vt);
                }
		generateValueTypeSerialization(vt);
		generateValueTypeDeSerialization(vt);
	    }
	}
    }

    protected void generateValueTypeSerializableForm(ValueType vt) {
        onNewline(out);
	print(out, "public static final class " + makeSerializationValueTypeName(vt) + " implements java.io.Serializable");
	newBlock(out);
	onNewline(out);
	println(out, "private static final long serialVersionUID = 1L;");
	generateSlots(vt);
	generateConstructor(vt);
	closeBlock(out);
    }

    protected void generateSlots(ValueType vt) {
	List<ExternalizationElement> extElems = vt.getExternalizationElements();
	for (ExternalizationElement extElem : extElems) {
	    ValueType extElemVt = extElem.getType();
	    if (extElemVt.isBuiltin() || extElemVt.isEnum()) {
		generateSlotDeclaration(out, extElemVt.getFullname(), makeSlotName(extElem));
	    } else {
		generateSlotDeclaration(out, getSerializedFormTypeName(extElemVt),
					extElem.getMethodName().replace('.', '_'));
	    }
	}
    }

    protected void generateConstructor(ValueType vt) {
	onNewline(out);
	printMethod(out, "private", "", makeSerializationValueTypeName(vt), makeArg(vt.getFullname(), "obj"));
	startMethodBody(out);
	List<ExternalizationElement> extElems = vt.getExternalizationElements();
	for (ExternalizationElement extElem : extElems) {
            onNewline(out);
	    ValueType extElemVt = extElem.getType();
	    printWords(out, "this." + makeSlotName(extElem), "=",
		       applyExternalizationIfRequired(applyExternalizerTo(extElem, "obj"), extElemVt));
	    print(out, ";");
	}
	endMethodBody(out);
    }

    protected String applyExternalizerTo(ExternalizationElement extElem, String expr) {
	String extMethodName = extElem.getMethodName();
	// parametric types require cast, so we always cast
	String cast = "(" + extElem.getType().getFullname() + ")";

	return (extMethodName.contains("."))
		? cast + extMethodName + "(" + expr + ")"
		: cast + expr + "." + extMethodName + "()";
    }

    protected String applyExternalizationIfRequired(String expr, ValueType vt) {
	if ((vt.isBuiltin() || vt.isEnum())) {
	    return expr;
        } else {
	    return SERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt) + "(" + expr + ")";
	}
    }

    public static String makeSerializationValueTypeName(ValueType vt) {
	return "Serialized$" + makeSafeValueTypeName(vt);
    }

    public static String makeSafeValueTypeName(ValueType vt) {
	return vt.getDomainName().replace('.', '$');
    }

    protected String makeSlotName(ExternalizationElement extElem) {
	return extElem.getMethodName().replace('.', '_');
    }

    protected void generateValueTypeSerialization(ValueType vt) {
	onNewline(out);
	printMethod(out, "public static final", getSerializedFormTypeName(vt),
		    SERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt),
		    makeArg(vt.getFullname(), "obj"));
	startMethodBody(out);
	print(out, "return ");
	if (DomainModel.isNullableType(vt)) {
	    print(out, "(obj == null) ? null : ");
        }
        if (vt.getExternalizationElements().size() == 1) {
            ExternalizationElement extElem = vt.getExternalizationElements().get(0);
	    ValueType extElemVt = extElem.getType();
	    print(out, applyExternalizationIfRequired(applyExternalizerTo(extElem, "obj"), extElemVt));
        } else {
            print(out, "new " + makeSerializationValueTypeName(vt) + "(obj)");
        }
        print(out, ";");
	endMethodBody(out);
    }

    protected void generateValueTypeDeSerialization(ValueType vt) {
	onNewline(out);
	printMethod(out, "public static final", vt.getFullname(),
		    DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt),
		    makeArg(getSerializedFormTypeName(vt) , "obj"));
	startMethodBody(out);
	String internalizationMethodName = vt.getInternalizationMethodName();
	if (internalizationMethodName == null) { // class constructor
	    internalizationMethodName = "new " + vt.getFullname();
	} else if (!internalizationMethodName.contains(".")) { // static method in the same class
	    internalizationMethodName = vt.getFullname() + "." + internalizationMethodName;
	}
	print(out, "return ");
	if (DomainModel.isNullableTypeFullName(getSerializedFormTypeName(vt))) {
	    print(out, "(obj == null) ? null : ");
        }
	print(out, "(" + vt.getFullname() + ")");
	print(out, internalizationMethodName + "(");

	boolean firstArg = true;
	List<ExternalizationElement> extElems = vt.getExternalizationElements();
        if (extElems.size() == 1) {
            ExternalizationElement extElem = extElems.get(0);
            ValueType extElemVt = extElem.getType();
            if (extElemVt.isBuiltin() || extElemVt.isEnum()) {
                print(out, "obj");
            } else {
                print(out, DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(extElemVt) + "(obj)");
            }
        } else {
            for (ExternalizationElement extElem : extElems) {

                if (firstArg) {
                    firstArg = false;
                } else {
                    print(out, ", ");
                }

                ValueType extElemVt = extElem.getType();
                if (extElemVt.isBuiltin() || extElemVt.isEnum()) {
                    print(out, "obj." + makeSlotName(extElem));
                } else {
                    // note that extElems.size() > 1 because of the outer if statment
                    print(out, DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(extElemVt) + "(obj." +
                          extElem.getMethodName().replace('.', '_') + ")");
                }
            }
        }
	print(out, ");");
	endMethodBody(out);
    }
}
