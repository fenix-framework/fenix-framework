package dml;

public class ExternalizationElement {

    private ValueType type;
    private String methodName;
    
    public ExternalizationElement(ValueType type, String methodName) {
        this.type = type;
        this.methodName = methodName;
    }

    public ValueType getType() {
        return type;
    }

    public String getMethodName() {
        return methodName;
    }
}
