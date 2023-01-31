package foo;


@SuppressWarnings("all")
 abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,foo.Test2> getRelationbarTestExtendsbarTest() {
        return new pt.ist.fenixframework.dml.runtime.DirectRelation(null, null);
    }
    
    // Constructors
    protected  Test_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.util.Locale getLocale() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void setLocale(java.util.Locale locale) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    // Role Methods
    
    public void addBartest(foo.Test2 bartest) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public void removeBartest(foo.Test2 bartest) {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    public java.util.Set<foo.Test2> getBartestSet() {
        throw new UnsupportedOperationException("Not implemented in default code generator");
    }
    
    
}
