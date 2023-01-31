package foo;


@SuppressWarnings("all")
 abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleMany<foo.Test,foo.Test2> role$$bartest = new pt.ist.fenixframework.dml.runtime.RoleMany<foo.Test,foo.Test2>() {
        public pt.ist.fenixframework.dml.runtime.RelationBaseSet<foo.Test2> getSet(foo.Test o1) {
            return (pt.ist.fenixframework.dml.runtime.RelationAwareSet<foo.Test,foo.Test2>) ((Test_Base) o1).bartest;
        }
        public pt.ist.fenixframework.dml.runtime.Role<foo.Test2,foo.Test> getInverseRole() {
            return foo.Test2.role$$footest;
        }
        
    };
    
    private final static class barTestExtendsbarTest {
        private static final pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,foo.Test2> relation = new pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,foo.Test2>(role$$bartest, "barTestExtendsbarTest");
    }
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,foo.Test2> getRelationbarTestExtendsbarTest() {
        return barTestExtendsbarTest.relation;
    }
    private static pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,foo.Test2> keyFunction$$bartest = new pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,foo.Test2>() { public Comparable<?> getKey(foo.Test2 value) { return value.getOid(); } public boolean allowMultipleKeys() {return false; }};
    
    // Slots
    private java.util.Locale locale;
    // Role Slots
    private pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<foo.Test2> bartest;
    
    // Init Instance
    
    private void initInstance() {
        init$Instance(true);
    }
    
    @Override
    protected void init$Instance(boolean allocateOnly) {
        super.init$Instance(allocateOnly);
        bartest = new pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<foo.Test2>();
    }
    
    // Constructors
    protected  Test_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.util.Locale getLocale() {
        return this.locale;
    }
    
    public void setLocale(java.util.Locale locale) {
        this.locale = locale;
    }
    
    // Role Methods
    
    public void addBartest(foo.Test2 bartest) {
        getRelationbarTestExtendsbarTest().add((foo.Test)this, bartest);
    }
    
    public void removeBartest(foo.Test2 bartest) {
        getRelationbarTestExtendsbarTest().remove((foo.Test)this, bartest);
    }
    
    public java.util.Set<foo.Test2> getBartestSet() {
        return new pt.ist.fenixframework.dml.runtime.RelationAwareSet<foo.Test,foo.Test2>((foo.Test)this, getRelationbarTestExtendsbarTest(), bartest, keyFunction$$bartest);
    }
    
    
}
