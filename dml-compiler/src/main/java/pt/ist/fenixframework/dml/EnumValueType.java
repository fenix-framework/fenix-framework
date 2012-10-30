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

    public PlainValueType getBaseType() {
        throw new Error("Enum value types do not have a PlainValueType as base");
    }

    public String getDomainName() {
        return domainName;
    }

    public String getFullname() {
        return fullTypeName;
    }

    public boolean isBuiltin() {
        return true;
    }

    public boolean isEnum() {
        return true;
    }

    public List<ExternalizationElement> getExternalizationElements() {
        return null;
    }

    public String getInternalizationMethodName() {
        return null;
    }
}
