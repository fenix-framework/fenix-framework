package pt.ist.fenixframework.dml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class Slot implements Serializable {
    
    public enum Option { REQUIRED }


    private String name;
    private ValueType type;
    private Set<Option> slotOptions = EnumSet.noneOf(Option.class);
    private List<Annotation> annotations = new ArrayList<Annotation>();
    
    public Slot(String name, ValueType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public String getType() {
        return getTypeName();
    }

    public ValueType getSlotType() {
        return type;
    }

    public String getTypeName() {
        return type.getFullname();
    }

    public void addOption(Option option) {
        slotOptions.add(option);
    }

    public Set<Option> getOptions() {
        return slotOptions;
    }

    public boolean hasOption(Option option) {
        return slotOptions.contains(option);
    }
    
    public void addAnnotation(Annotation annotation) {
	annotations.add(annotation);
    }
    
    public List<Annotation> getAnnotations() {
	return annotations;
    }
    
    public boolean hasAnnotation(Annotation annotation) {
	return annotations.contains(annotation);
    }
    
    public boolean hasIndexedAnnotation() {
	for (Annotation annotation : annotations) {
	    if (annotation.getName().equals(Annotation.INDEX_ANNOTATION)) {
		return true;
	    }
	}
	return false;
    }

//     public void generateSlotDeclaration(CodeWriter out) {
//         out.write(type);
//         out.write(" ");
//         out.write(name);
//         out.writeln(";");
//     }

//     public void generateSlotAccessors(CodeWriter out) {
//         // getter
//         out.write("public ");
//         out.write(type);
//         out.write("get");
//         out.write(capitalizeFirstChar(name));
//         out.write("() ");
//         out.newBlock();
//         out.write("return getBodyForGet().");
//         out.write(name);
//         out.writeln(";");
//         out.closeBlock();

//         // setter
//         out.write("public void ");
//         out.write("set");
//         out.write(capitalizeFirstChar(name));
//         out.write("(");
//         out.write(type);
//         out.write(" ");
//         out.write(name);
//         out.write(") ");
//         out.newBlock();
//         out.write("getBodyForSet().");
//         out.write(name);
//         out.writeln(" = ");
//         out.writeln(name);
//         out.writeln(";");
//         out.closeBlock();
//     }
}
