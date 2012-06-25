package pt.ist.fenixframework.dml;

import java.util.List;

public class ParamValueType implements ValueType {

    private PlainValueType baseType;
    private String typeArguments;

    public ParamValueType(PlainValueType baseType, String typeArguments) {
        this.baseType = baseType;
        this.typeArguments = typeArguments;
    }

    public PlainValueType getBaseType() {
        return baseType;
    }

    public String getDomainName() {
        return baseType.getDomainName();
    }

    public String getFullname() {
        return baseType.getFullname() + typeArguments;
    }

    public boolean isBuiltin() {
        return baseType.isBuiltin();
    }

    public boolean isEnum() {
        return baseType.isEnum();
    }

    public List<ExternalizationElement> getExternalizationElements() {
        return baseType.getExternalizationElements();
    }

    public String getInternalizationMethodName() {
        return baseType.getInternalizationMethodName();
    }
}
