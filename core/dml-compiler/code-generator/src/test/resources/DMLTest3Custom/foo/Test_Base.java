package foo;


@SuppressWarnings("all")
 abstract class Test_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    // Static Slots
    
    // Slots
    private java.lang.String name;
    private java.lang.Integer age;
    private java.lang.Float timestamp;
    // Role Slots
    
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
    
    
}
