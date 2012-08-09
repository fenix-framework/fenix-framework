package pt.ist.fenixframework.dml;

import java.io.Serializable;

public class ExternalizationElement implements Serializable {

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
