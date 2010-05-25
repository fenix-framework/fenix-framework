package pt.ist.fenixframework.pstm.dml;

import java.io.PrintWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;

import dml.*;
import dml.runtime.Relation;

public class FenixValueTypeSerializationGenerator extends FenixCodeGenerator {

    protected static final String SERIALIZATION_METHOD_PREFIX = "serialize$";
    protected static final String DESERIALIZATION_METHOD_PREFIX = "deSerialize$";

    protected PrintWriter out;

    public FenixValueTypeSerializationGenerator(CompilerArgs compArgs, DomainModel domainModel) {
	super(compArgs, domainModel);
    }

    @Override
    public void generateCode() {
	// create the FenixValueTypeSerializationGenerator before starting the "standard" generation.
 	String packageName = "pt.ist.fenixframework";
 	String vtGenClassname = "ValueTypeSerializationGenerator";
	File file = new File(getBaseDirectoryFor(packageName), vtGenClassname + ".java");
	try {
	    file.getParentFile().mkdirs();
	    this.out = new PrintWriter(new FileWriter(file), true);
	} catch (java.io.IOException ioe) {
	    throw new Error("Can't open file" + file);
	}

	generateFilePreamble(packageName, this.out);
	newline(out);
	printWords(out, "public", "class", "ValueTypeSerializationGenerator");
	newBlock(out);
	generateValueTypeSerializations();
	closeBlock(out);
    }
    
    protected void generateValueTypeSerializations() {
	for (ValueType vt : getDomainModel().getAllValueTypes()) {
	    if (!(vt.isBuiltin() || vt.isEnum())) {
		println(out, "");
		print(out, "// VT: " + vt.getDomainName());
		generateValueTypeSerializableForm(vt);
		generateValueTypeSerialization(vt);
		generateValueTypeDeSerialization(vt);
	    }
	}
    }

    protected void generateValueTypeSerializableForm(ValueType vt) {
	newline(out);
	print(out, "public static class " + makeSerializationValueTypeName(vt) + " implements java.io.Serializable");
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
		generateSlotDeclaration(out, makeSerializationValueTypeName(extElemVt),
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
	    ValueType extElemVt = extElem.getType();
	    printWords(out, "this." + makeSlotName(extElem), "=",
		       applyExternalizationIfRequired(applyExternalizerTo(extElem, "obj"), extElemVt));
	    println(out, ";");
	}
	endMethodBody(out);
    }

    protected String applyExternalizerTo(ExternalizationElement extElem, String expr) {
	String extMethodName = extElem.getMethodName();
	// parametric types require cast, so we alwys cast
	String cast = "(" + extElem.getType().getFullname() + ")";

	return (extMethodName.contains("."))
		? cast + extMethodName + "(" + expr + ")"
		: cast + expr + "." + extMethodName + "()";
    }

    protected String applyExternalizationIfRequired(String expr, ValueType vt) {
	if (!(vt.isBuiltin() || vt.isEnum())) {
	    return SERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt) + "(" + expr + ")";
	} else {
	    return expr;
	}
    }

    protected String makeSerializationValueTypeName(ValueType vt) {
	return "Serialized$" + makeSafeValueTypeName(vt);
    }

    protected String makeSafeValueTypeName(ValueType vt) {
	return vt.getDomainName().replace('.', '$');
    }

    protected String makeSlotName(ExternalizationElement extElem) {
	return extElem.getMethodName().replace('.', '_');
    }

    protected void generateCheckNullable(ValueType vt) {
	if (FenixDomainModel.isNullableType(vt)) {
	    print(out, "(obj == null) ? null : ");
	}
    }

    protected void generateValueTypeSerialization(ValueType vt) {
	onNewline(out);
	printMethod(out, "public static", makeSerializationValueTypeName(vt),
		    SERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt),
		    makeArg(vt.getFullname(), "obj"));
	startMethodBody(out);
	print(out, "return ");
	generateCheckNullable(vt);
	print(out, "new " + makeSerializationValueTypeName(vt) + "(obj);");
	endMethodBody(out);
    }

    protected void generateValueTypeDeSerialization(ValueType vt) {
	onNewline(out);
	printMethod(out, "public static", vt.getFullname(),
		    DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt),
		    makeArg(makeSerializationValueTypeName(vt) , "obj"));
	startMethodBody(out);
	String internalizationMethodName = vt.getInternalizationMethodName();
	if (internalizationMethodName == null) { // class constructor
	    internalizationMethodName = "new " + vt.getFullname();
	} else if (!internalizationMethodName.contains(".")) { // static method in the same class
	    internalizationMethodName = vt.getFullname() + "." + internalizationMethodName;
	}
	print(out, "return ");
	generateCheckNullable(vt);
        print(out, "(" + vt.getFullname() + ")");
	print(out, internalizationMethodName + "(");

	boolean firstArg = true;
	List<ExternalizationElement> extElems = vt.getExternalizationElements();
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
		print(out, DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(extElemVt) + "(obj." +
		      extElem.getMethodName().replace('.', '_') + ")");
	    }
	}
	print(out, ");");
	endMethodBody(out);
    }
}
