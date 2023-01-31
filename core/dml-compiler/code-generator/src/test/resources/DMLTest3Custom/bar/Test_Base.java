package bar;


@SuppressWarnings("all")
public abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleMany<bar.Test,bar.TestExtends> role$$bartest = new pt.ist.fenixframework.dml.runtime.RoleMany<bar.Test,bar.TestExtends>() {
        public pt.ist.fenixframework.dml.runtime.RelationBaseSet<bar.TestExtends> getSet(bar.Test o1) {
            return (pt.ist.fenixframework.dml.runtime.RelationAwareSet<bar.Test,bar.TestExtends>) ((Test_Base) o1).bartest;
        }
        public pt.ist.fenixframework.dml.runtime.Role<bar.TestExtends,bar.Test> getInverseRole() {
            return bar.TestExtends.role$$footest;
        }
        
    };
    public final static pt.ist.fenixframework.dml.runtime.RoleOne<bar.Test,foo.Test2> role$$test2 = new pt.ist.fenixframework.dml.runtime.RoleOne<bar.Test,foo.Test2>() {
        public foo.Test2 getValue(bar.Test o1) {
            return ((Test_Base)o1).test2;
        }
        public void setValue(bar.Test o1, foo.Test2 o2) {
            ((Test_Base)o1).test2 = o2;
        }
        public pt.ist.fenixframework.dml.runtime.Role<foo.Test2,bar.Test> getInverseRole() {
            return foo.Test2.role$$bartest;
        }
        
    };
    
    private final static class barTestExtendsbarTest {
        private static final pt.ist.fenixframework.dml.runtime.DirectRelation<bar.Test,bar.TestExtends> relation = new pt.ist.fenixframework.dml.runtime.DirectRelation<bar.Test,bar.TestExtends>(role$$bartest, "barTestExtendsbarTest");
    }
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<bar.Test,bar.TestExtends> getRelationbarTestExtendsbarTest() {
        return barTestExtendsbarTest.relation;
    }
    private static pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,bar.TestExtends> keyFunction$$bartest = new pt.ist.fenixframework.dml.runtime.KeyFunction<Comparable<?>,bar.TestExtends>() { public Comparable<?> getKey(bar.TestExtends value) { return value.getOid(); } public boolean allowMultipleKeys() {return false; }};
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test2,bar.Test> getRelationbarTestTest2() {
        return foo.Test2.getRelationbarTestTest2();
    }
    
    // Slots
    private java.lang.String name;
    private java.lang.Integer age;
    private java.lang.Float timestamp;
    // Role Slots
    private pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<bar.TestExtends> bartest;
    private foo.Test2 test2;
    
    // Init Instance
    
    private void initInstance() {
        init$Instance(true);
    }
    
    @Override
    protected void init$Instance(boolean allocateOnly) {
        super.init$Instance(allocateOnly);
        bartest = new pt.ist.fenixframework.dml.runtime.StubDomainBasedMap<bar.TestExtends>();
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
    
    public void addBartest(bar.TestExtends bartest) {
        getRelationbarTestExtendsbarTest().add((bar.Test)this, bartest);
    }
    
    public void removeBartest(bar.TestExtends bartest) {
        getRelationbarTestExtendsbarTest().remove((bar.Test)this, bartest);
    }
    
    public java.util.Set<bar.TestExtends> getBartestSet() {
        return new pt.ist.fenixframework.dml.runtime.RelationAwareSet<bar.Test,bar.TestExtends>((bar.Test)this, getRelationbarTestExtendsbarTest(), bartest, keyFunction$$bartest);
    }
    
    public foo.Test2 getTest2() {
        return this.test2;
    }
    
    public void setTest2(foo.Test2 test2) {
        getRelationbarTestTest2().add(test2, (bar.Test)this);
    }
    
    
}
