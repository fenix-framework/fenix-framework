package dml;

import java.util.List;

public interface ValueType {
    public PlainValueType getBaseType();
    public String getDomainName();
    public String getFullname();
    public boolean isBuiltin();
    public boolean isEnum();
    public List<ExternalizationElement> getExternalizationElements();
    public String getInternalizationMethodName();
}
