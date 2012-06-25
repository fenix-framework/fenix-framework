package pt.ist.fenixframework.dml;

import java.util.ArrayList;
import java.util.List;


public class PlainValueType implements ValueType {

    private String domainName;
    private String fullTypeName;
    
    private List<ExternalizationElement> extElements = new ArrayList<ExternalizationElement>();
    private String internalizationMethodName = null;


    public PlainValueType(String fullTypeName) {
        this.fullTypeName = fullTypeName;
    }

    public PlainValueType(String domainName, String fullTypeName) {
        this.domainName = domainName;
        this.fullTypeName = fullTypeName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public PlainValueType getBaseType() {
        return this;
    }

    public String getDomainName() {
        return domainName;
    }

    public String getFullname() {
        return fullTypeName;
    }

    public boolean isBuiltin() {
        // builtin value-types have no externalization elements
        return extElements.isEmpty();
    }

    public boolean isEnum() {
        return false;
    }

    public List<ExternalizationElement> getExternalizationElements() {
        return extElements;
    }

    public void addExternalizationElement(ExternalizationElement extElement) {
        this.extElements.add(extElement);
    }

    public String getInternalizationMethodName() {
        return internalizationMethodName;
    }

    public void setInternalizationMethodName(String name) {
        this.internalizationMethodName = name;
    }
}
