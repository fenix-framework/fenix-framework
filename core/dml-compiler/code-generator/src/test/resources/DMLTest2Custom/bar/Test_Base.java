package bar;


@SuppressWarnings("all")
public abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleOne<bar.Test,foo.Test> role$$footest = new pt.ist.fenixframework.dml.runtime.RoleOne<bar.Test,foo.Test>() {
        public foo.Test getValue(bar.Test o1) {
            return ((Test_Base)o1).footest;
        }
        public void setValue(bar.Test o1, foo.Test o2) {
            ((Test_Base)o1).footest = o2;
        }
        public pt.ist.fenixframework.dml.runtime.Role<foo.Test,bar.Test> getInverseRole() {
            return foo.Test.role$$bartest;
        }
        
    };
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,bar.Test> getRelationfooTestbarTest() {
        return foo.Test.getRelationfooTestbarTest();
    }
    
    // Slots
    private java.lang.String name;
    private java.lang.Integer age;
    private java.lang.Float timestamp;
    // Role Slots
    private foo.Test footest;
    
    // Init Instance
    
    private void initInstance() {
        init$Instance(true);
    }
    
    @Override
    protected void init$Instance(boolean allocateOnly) {
        super.init$Instance(allocateOnly);
        
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
    
    public foo.Test getFootest() {
        return this.footest;
    }
    
    public void setFootest(foo.Test footest) {
        getRelationfooTestbarTest().add(footest, (bar.Test)this);
    }
    
    
}
