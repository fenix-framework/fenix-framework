package bar;


@SuppressWarnings("all")
public abstract class TestExtends_Base extends foo.Test {
    // Static Slots
    public final static pt.ist.fenixframework.dml.runtime.RoleOne<bar.TestExtends,bar.Test> role$$footest = new pt.ist.fenixframework.dml.runtime.RoleOne<bar.TestExtends,bar.Test>() {
        public bar.Test getValue(bar.TestExtends o1) {
            return ((TestExtends_Base)o1).footest;
        }
        public void setValue(bar.TestExtends o1, bar.Test o2) {
            ((TestExtends_Base)o1).footest = o2;
        }
        public pt.ist.fenixframework.dml.runtime.Role<bar.Test,bar.TestExtends> getInverseRole() {
            return bar.Test.role$$bartest;
        }
        
    };
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<bar.Test,bar.TestExtends> getRelationbarTestExtendsbarTest() {
        return bar.Test.getRelationbarTestExtendsbarTest();
    }
    
    // Slots
    private java.lang.Short idx;
    // Role Slots
    private bar.Test footest;
    
    // Init Instance
    
    private void initInstance() {
        init$Instance(true);
    }
    
    @Override
    protected void init$Instance(boolean allocateOnly) {
        super.init$Instance(allocateOnly);
        
    }
    
    // Constructors
    protected  TestExtends_Base() {
        super();
    }
    
    // Getters and Setters
    
    public java.lang.Short getIdx() {
        return this.idx;
    }
    
    public void setIdx(java.lang.Short idx) {
        this.idx = idx;
    }
    
    // Role Methods
    
    public bar.Test getFootest() {
        return this.footest;
    }
    
    public void setFootest(bar.Test footest) {
        getRelationbarTestExtendsbarTest().add(footest, (bar.TestExtends)this);
    }
    
    
}
