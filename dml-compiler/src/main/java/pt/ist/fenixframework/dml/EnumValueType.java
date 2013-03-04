package pt.ist.fenixframework.dml;

import java.util.List;

public class EnumValueType implements ValueType {

    private String domainName;
    private String fullTypeName;

    public EnumValueType(String domainName, String fullTypeName) {
        this.domainName = domainName;
        this.fullTypeName = fullTypeName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public PlainValueType getBaseType() {
        throw new Error("Enum value types do not have a PlainValueType as base");
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    @Override
    public String getFullname() {
        return fullTypeName;
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public boolean isEnum() {
        return true;
    }

    @Override
    public List<ExternalizationElement> getExternalizationElements() {
        return null;
    }

    @Override
    public String getInternalizationMethodName() {
        return null;
    }
}
