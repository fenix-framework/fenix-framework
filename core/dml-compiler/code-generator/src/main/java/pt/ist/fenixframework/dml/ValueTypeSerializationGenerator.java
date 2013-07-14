package pt.ist.fenixframework.dml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

    public static String getSerializedFormTypeName(ValueType vt, boolean simple) {
        List<ExternalizationElement> extElems = vt.getExternalizationElements();
        if (extElems.size() == 0) { // built-in type do not have externalizers
            return simple ? vt.getDomainName() : vt.getFullname();
        } else if (extElems.size() == 1) {
            // It's just a wrapper.  So, our serialized form is the same as our component's
            return getSerializedFormTypeName(extElems.get(0).getType(), simple);
        } else {
            // Alas, we need a full-blown SerializedForm for this one
            return simple ? JsonElement.class.getSimpleName() : JsonElement.class.getName();
        }
    }

    public static String getSerializedFormTypeName(ValueType vt) {
        return getSerializedFormTypeName(vt, false);
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

    @Override
    protected void generateFilePreamble(String subPackageName, PrintWriter out) {
        super.generateFilePreamble(subPackageName, out);

        println(out, "import " + JsonObject.class.getName() + ";");
        println(out, "import pt.ist.fenixframework.util.JsonConverter;");
    }

    protected void generateValueTypeSerializations() {
        for (ValueType vt : getDomainModel().getAllValueTypes()) {
            if (!(vt.isBuiltin() || vt.isEnum())) {
                println(out, "");
                print(out, "// VT: " + vt.getDomainName() + " serializes as " + getSerializedFormTypeName(vt));

                generateValueTypeSerialization(vt);
                generateValueTypeDeSerialization(vt);
            }
        }
    }

    protected String applyExternalizerTo(ExternalizationElement extElem, String expr) {
        String extMethodName = extElem.getMethodName();
        // parametric types require cast, so we always cast
        String cast = "(" + extElem.getType().getFullname() + ")";

        return (extMethodName.contains(".")) ? cast + extMethodName + "(" + expr + ")" : cast + expr + "." + extMethodName + "()";
    }

    protected String applyExternalizationIfRequired(String expr, ValueType vt) {
        if ((vt.isBuiltin() || vt.isEnum())) {
            return expr;
        } else {
            return SERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt) + "(" + expr + ")";
        }
    }

    public static String makeSafeValueTypeName(ValueType vt) {
        return vt.getDomainName().replace('.', '$');
    }

    protected String makePrettySlotName(ExternalizationElement extElem) {
        String name = extElem.getMethodName().replace('.', '_');
        if (name.startsWith("get")) {
            name = name.substring(3);
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    protected void generateValueTypeSerialization(ValueType vt) {
        onNewline(out);
        printMethod(out, "public static final", getSerializedFormTypeName(vt), SERIALIZATION_METHOD_PREFIX
                + makeSafeValueTypeName(vt), makeArg(vt.getFullname(), "obj"));
        startMethodBody(out);
        if (vt.getExternalizationElements().size() == 1) {
            print(out, "return (obj == null) ? null : ");
            ExternalizationElement extElem = vt.getExternalizationElements().get(0);
            ValueType extElemVt = extElem.getType();
            print(out, applyExternalizationIfRequired(applyExternalizerTo(extElem, "obj"), extElemVt));
        } else {
            println(out, "if (obj == null) return null;");
            println(out, "JsonObject json = new JsonObject();");
            for (ExternalizationElement extElem : vt.getExternalizationElements()) {
                print(out, "json.add(\"");
                print(out, makePrettySlotName(extElem));
                print(out, "\", JsonConverter.getJsonFor(");
                print(out, applyExternalizationIfRequired(applyExternalizerTo(extElem, "obj"), extElem.getType()));
                print(out, "));");
                newline(out);
            }
            print(out, "return json");
        }
        print(out, ";");
        endMethodBody(out);
    }

    protected void generateValueTypeDeSerialization(ValueType vt) {
        onNewline(out);
        printMethod(out, "public static final", vt.getFullname(), DESERIALIZATION_METHOD_PREFIX + makeSafeValueTypeName(vt),
                makeArg(getSerializedFormTypeName(vt), "obj"));
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
                    newline(out);
                    firstArg = false;
                } else {
                    println(out, ",");
                }

                ValueType extElemVt = extElem.getType();
                if (extElemVt.isBuiltin() || extElemVt.isEnum()) {
                    print(out, "JsonConverter.get");
                    print(out, capitalize(extElem.getType().getDomainName()));
                    print(out, "FromJson(obj.getAsJsonObject().get(\"");;
                    print(out, makePrettySlotName(extElem));
                    print(out, "\"))");
                } else {
                    // note that extElems.size() > 1 because of the outer if statment
                    print(out, DESERIALIZATION_METHOD_PREFIX);
                    print(out, makeSafeValueTypeName(extElemVt));
                    print(out, "(JsonConverter.get");
                    print(out, getSerializedFormTypeName(extElemVt, true));
                    print(out, "FromJson(obj.getAsJsonObject().get(\"");
                    print(out, makePrettySlotName(extElem));
                    print(out, "\")))");
                }
            }
        }
        print(out, ");");
        endMethodBody(out);
    }
}
