package foo;


@SuppressWarnings("all")
public abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,bar.Test> getRelationfooTestbarTest() {
        return new pt.ist.fenixframework.dml.runtime.DirectRelation(null, null);
    }
    
    // Constructors
    protected  Test_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.lang.String getName() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void setName(java.lang.String name) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public java.lang.Integer getAge() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void setAge(java.lang.Integer age) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public java.lang.Float getTimestamp() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void setTimestamp(java.lang.Float timestamp) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    // Role Methods
    
    public void addBartest(bar.Test bartest) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void removeBartest(bar.Test bartest) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public java.util.Set<bar.Test> getBartestSet() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    
}
