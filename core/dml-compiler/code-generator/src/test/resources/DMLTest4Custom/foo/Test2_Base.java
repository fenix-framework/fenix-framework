package foo;


@SuppressWarnings("all")
public abstract class Test2_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleOne<foo.Test2,foo.Test> role$$footest = new pt.ist.fenixframework.dml.runtime.RoleOne<foo.Test2,foo.Test>() {
        public foo.Test getValue(foo.Test2 o1) {
            return ((Test2_Base)o1).footest;
        }
        public void setValue(foo.Test2 o1, foo.Test o2) {
            ((Test2_Base)o1).footest = o2;
        }
        public pt.ist.fenixframework.dml.runtime.Role<foo.Test,foo.Test2> getInverseRole() {
            return foo.Test.role$$bartest;
        }
        
    };
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test,foo.Test2> getRelationbarTestExtendsbarTest() {
        return foo.Test.getRelationbarTestExtendsbarTest();
    }
    
    // Slots
    private java.lang.Object o;
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
    protected  Test2_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.lang.Object getO() {
        return this.o;
    }
    
    public void setO(java.lang.Object o) {
        this.o = o;
    }
    
    // Role Methods
    
    public foo.Test getFootest() {
        return this.footest;
    }
    
    public void setFootest(foo.Test footest) {
        getRelationbarTestExtendsbarTest().add(footest, (foo.Test2)this);
    }
    
    
}
