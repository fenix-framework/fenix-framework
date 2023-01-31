package foo;


@SuppressWarnings("all")
public abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleMany<foo.Test,bar.Test> role$$bartest = new pt.ist.fenixframework.dml.runtime.RoleMany<foo.Test,bar.Test>() {
        public pt.ist.fenixframework.dml.runtime.RelationBaseSet<bar.Test> getSet(foo.Test o1) {
            return (pt.ist.fenixframework.dml.runtime.RelationAwareSet<foo.Test,bar.Test>) ((Test_Base) o1).bartest;
        }
        public pt.ist.fenixframework.dml.runtime.Role<bar.Test,foo.Test> getInverseRole() {
            return bar.Test.role$$footest;
        }
        
    };
    
    private final static class fooTestbarTest {
        private static final pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,bar.Test> relation = new pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,bar.Test>(role$$bartest, "fooTestbarTest");
    }
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,bar.Test> getRelationfooTestbarTest() {
        return fooTestbarTest.relation;
    }
    private static pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,bar.Test> keyFunction$$bartest = new pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,bar.Test>() { public Comparable<?> getKey(bar.Test value) { return value.getOid(); } public boolean allowMultipleKeys() {return false; }};
    
    // Slots
    private java.lang.String name;
    private java.lang.Integer age;
    private java.lang.Float timestamp;
    // Role Slots
    private pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<bar.Test> bartest;
    
    // Init Instance
    
    private void initInstance() {
        init$Instance(true);
    }
    
    @Override
    protected void init$Instance(boolean allocateOnly) {
        super.init$Instance(allocateOnly);
        bartest = new pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<bar.Test>();
    }
    
    // Constructors
    protected  Test_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.lang.String getName() {
        return this.name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.Integer getAge() {
        return this.age;
    }
    
    public void setAge(java.lang.Integer age) {
        this.age = age;
    }
    
    public java.lang.Float getTimestamp() {
        return this.timestamp;
    }
    
    public void setTimestamp(java.lang.Float timestamp) {
        this.timestamp = timestamp;
    }
    
    // Role Methods
    
    public void addBartest(bar.Test bartest) {
        getRelationfooTestbarTest().add((foo.Test)this, bartest);
    }
    
    public void removeBartest(bar.Test bartest) {
        getRelationfooTestbarTest().remove((foo.Test)this, bartest);
    }
    
    public java.util.Set<bar.Test> getBartestSet() {
        return new pt.ist.fenixframework.dml.runtime.RelationAwareSet<foo.Test,bar.Test>((foo.Test)this, getRelationfooTestbarTest(), bartest, keyFunction$$bartest);
    }
    
    
}
