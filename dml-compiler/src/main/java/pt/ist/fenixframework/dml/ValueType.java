package pt.ist.fenixframework.dml;

import java.io.Serializable;
import java.util.List;

public interface ValueType extends Serializable {
    public PlainValueType getBaseType();
    public String getDomainName();
    public String getFullname();
    public boolean isBuiltin();
    public boolean isEnum();
    public List<ExternalizationElement> getExternalizationElements();
    public String getInternalizationMethodName();
}
