package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class Assembly_Base extends pt.ist.fenixframework.example.oo7.domain.DesignObj {
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> role$$superAssembly = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly>("superAssembly") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> getBox(pt.ist.fenixframework.example.oo7.domain.Assembly o1) {
            return ((Assembly_Base)o1).superAssembly;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Assembly o1, java.lang.Integer newFk) {
            o1.setKeySuperAssembly(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.ComplexAssembly.role$$subAssemblies;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.Module> role$$module = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.Module>("module") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.Module> getBox(pt.ist.fenixframework.example.oo7.domain.Assembly o1) {
            return ((Assembly_Base)o1).module;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Assembly o1, java.lang.Integer newFk) {
            o1.setKeyModule(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Module.role$$assemblies;
        }
        
    };
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> AssemblyHasComplexAssembly;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.Module> AssemblyHasModule;
    
    private VBox<java.lang.Integer> keySuperAssembly;
    private VBox<java.lang.Integer> keyModule;
    private VBox<java.lang.String> ojbConcreteClass;
    private VBox<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> superAssembly;
    private VBox<pt.ist.fenixframework.example.oo7.domain.Module> module;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        keySuperAssembly = VBox.makeNew(allocateOnly, false);
        keyModule = VBox.makeNew(allocateOnly, false);
        ojbConcreteClass = VBox.makeNew(allocateOnly, false);
        superAssembly = VBox.makeNew(allocateOnly, true);
        module = VBox.makeNew(allocateOnly, true);
    }
    
    {
        initInstance(false);
    }
    
    protected  Assembly_Base() {
        super();
        setOjbConcreteClass(getClass().getName());
    }
    
    public java.lang.Integer getKeySuperAssembly() {
        return this.keySuperAssembly.get(this, "keySuperAssembly");
    }
    
    public void setKeySuperAssembly(java.lang.Integer keySuperAssembly) {
        this.keySuperAssembly.put(this, "keySuperAssembly", keySuperAssembly);
    }
    
    private Object get$keySuperAssembly() {
        java.lang.Integer value = this.keySuperAssembly.get(this, "keySuperAssembly");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keySuperAssembly(java.lang.Integer arg0, int txNumber) {
        this.keySuperAssembly.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyModule() {
        return this.keyModule.get(this, "keyModule");
    }
    
    public void setKeyModule(java.lang.Integer keyModule) {
        this.keyModule.put(this, "keyModule", keyModule);
    }
    
    private Object get$keyModule() {
        java.lang.Integer value = this.keyModule.get(this, "keyModule");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyModule(java.lang.Integer arg0, int txNumber) {
        this.keyModule.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.String getOjbConcreteClass() {
        return this.ojbConcreteClass.get(this, "ojbConcreteClass");
    }
    
    public void setOjbConcreteClass(java.lang.String ojbConcreteClass) {
        this.ojbConcreteClass.put(this, "ojbConcreteClass", ojbConcreteClass);
    }
    
    private Object get$ojbConcreteClass() {
        java.lang.String value = this.ojbConcreteClass.get(this, "ojbConcreteClass");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForString(value);
    }
    
    private final void set$ojbConcreteClass(java.lang.String arg0, int txNumber) {
        this.ojbConcreteClass.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.ComplexAssembly getSuperAssembly() {
        return this.superAssembly.get(this, "superAssembly");
    }
    
    public void setSuperAssembly(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly superAssembly) {
        AssemblyHasComplexAssembly.add((pt.ist.fenixframework.example.oo7.domain.Assembly)this, superAssembly);
    }
    
    public boolean hasSuperAssembly() {
        return (getSuperAssembly() != null);
    }
    
    public void removeSuperAssembly() {
        setSuperAssembly(null);
    }
    
    public void set$superAssembly(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly superAssembly) {
        this.superAssembly.setFromOJB(this, "superAssembly", superAssembly);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.Module getModule() {
        return this.module.get(this, "module");
    }
    
    public void setModule(pt.ist.fenixframework.example.oo7.domain.Module module) {
        AssemblyHasModule.add((pt.ist.fenixframework.example.oo7.domain.Assembly)this, module);
    }
    
    public boolean hasModule() {
        return (getModule() != null);
    }
    
    public void removeModule() {
        setModule(null);
    }
    
    public void set$module(pt.ist.fenixframework.example.oo7.domain.Module module) {
        this.module.setFromOJB(this, "module", module);
    }
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasSuperAssembly()) return false;
        if (hasModule()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        set$keySuperAssembly(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_SUPER_ASSEMBLY"), txNumber);
        set$keyModule(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_MODULE"), txNumber);
        set$ojbConcreteClass(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "OJB_CONCRETE_CLASS"), txNumber);
    }
    
}
