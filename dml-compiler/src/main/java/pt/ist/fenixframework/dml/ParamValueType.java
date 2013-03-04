package pt.ist.fenixframework.dml;

import java.util.List;

public class ParamValueType implements ValueType {

    private PlainValueType baseType;
    private String typeArguments;

    public ParamValueType(PlainValueType baseType, String typeArguments) {
        this.baseType = baseType;
        this.typeArguments = typeArguments;
    }

    @Override
    public PlainValueType getBaseType() {
        return baseType;
    }

    @Override
    public String getDomainName() {
        return baseType.getDomainName();
    }

    @Override
    public String getFullname() {
        return baseType.getFullname() + typeArguments;
    }

    @Override
    public boolean isBuiltin() {
        return baseType.isBuiltin();
    }

    @Override
    public boolean isEnum() {
        return baseType.isEnum();
    }

    @Override
    public List<ExternalizationElement> getExternalizationElements() {
        return baseType.getExternalizationElements();
    }

    @Override
    public String getInternalizationMethodName() {
        return baseType.getInternalizationMethodName();
    }
}
