package foo;


@SuppressWarnings("all")
public abstract class Test2_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleOne<foo.Test2,bar.Test> role$$bartest = new pt.ist.fenixframework.dml.runtime.RoleOne<foo.Test2,bar.Test>() {
        public bar.Test getValue(foo.Test2 o1) {
            return ((Test2_Base)o1).bartest;
        }
        public void setValue(foo.Test2 o1, bar.Test o2) {
            ((Test2_Base)o1).bartest = o2;
        }
        public pt.ist.fenixframework.dml.runtime.Role<bar.Test,foo.Test2> getInverseRole() {
            return bar.Test.role$$test2;
        }
        
    };
    
    private final static class barTestTest2 {
        private static final pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test2,bar.Test> relation = new pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test2,bar.Test>(role$$bartest, "barTestTest2");
    }
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<foo.Test2,bar.Test> getRelationbarTestTest2() {
        return barTestTest2.relation;
    }
    
    // Slots
    private bar.Test tb;
    private bar.TestExtends teb;
    // Role Slots
    private bar.Test bartest;
    
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
    
    public bar.Test getTb() {
        return this.tb;
    }
    
    public void setTb(bar.Test tb) {
        this.tb = tb;
    }
    
    public bar.TestExtends getTeb() {
        return this.teb;
    }
    
    public void setTeb(bar.TestExtends teb) {
        this.teb = teb;
    }
    
    // Role Methods
    
    public bar.Test getBartest() {
        return this.bartest;
    }
    
    public void setBartest(bar.Test bartest) {
        getRelationbarTestTest2().add((foo.Test2)this, bartest);
    }
    
    
}
