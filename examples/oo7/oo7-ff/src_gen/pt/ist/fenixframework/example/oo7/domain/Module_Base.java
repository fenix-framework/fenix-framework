package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class Module_Base extends pt.ist.fenixframework.example.oo7.domain.DesignObj {
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly> role$$assemblies = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Assembly> getSet(pt.ist.fenixframework.example.oo7.domain.Module o1) {
            return ((Module_Base)o1).assemblies;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.Module> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Assembly.role$$module;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.OO7Application> role$$appModules = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.OO7Application>("appModules") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> getBox(pt.ist.fenixframework.example.oo7.domain.Module o1) {
            return ((Module_Base)o1).appModules;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Module o1, java.lang.Integer newFk) {
            o1.setKeyAppModules(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.OO7Application.role$$modules;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> role$$designRoot = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly>("designRoot") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> getBox(pt.ist.fenixframework.example.oo7.domain.Module o1) {
            return ((Module_Base)o1).designRoot;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Module o1, java.lang.Integer newFk) {
            o1.setKeyDesignRoot(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.ComplexAssembly.role$$md2;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Manual> role$$manual = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Manual>("manual") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.Manual> getBox(pt.ist.fenixframework.example.oo7.domain.Module o1) {
            return ((Module_Base)o1).manual;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Module o1, java.lang.Integer newFk) {
            o1.setKeyManual(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Manual.role$$md1;
        }
        
    };
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly> AssemblyHasModule = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly>(role$$assemblies);
    static {
        pt.ist.fenixframework.example.oo7.domain.Assembly.AssemblyHasModule = AssemblyHasModule.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.OO7Application> ApplicationHasModules = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.OO7Application>(role$$appModules);
    static {
        pt.ist.fenixframework.example.oo7.domain.OO7Application.ApplicationHasModules = ApplicationHasModules.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> ModuleHasComplexAssembly = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly>(role$$designRoot);
    static {
        pt.ist.fenixframework.example.oo7.domain.ComplexAssembly.ModuleHasComplexAssembly = ModuleHasComplexAssembly.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Manual> ModuleHasManual = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Manual>(role$$manual);
    static {
        pt.ist.fenixframework.example.oo7.domain.Manual.ModuleHasManual = ModuleHasManual.getInverseRelation();
    }
    
    private VBox<java.lang.Integer> keyAppModules;
    private VBox<java.lang.Integer> keyDesignRoot;
    private VBox<java.lang.Integer> keyManual;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly> assemblies;
    private VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> appModules;
    private VBox<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> designRoot;
    private VBox<pt.ist.fenixframework.example.oo7.domain.Manual> manual;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        keyAppModules = VBox.makeNew(allocateOnly, false);
        keyDesignRoot = VBox.makeNew(allocateOnly, false);
        keyManual = VBox.makeNew(allocateOnly, false);
        assemblies = new RelationList<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Assembly>((pt.ist.fenixframework.example.oo7.domain.Module)this, AssemblyHasModule, "assemblies", allocateOnly);
        appModules = VBox.makeNew(allocateOnly, true);
        designRoot = VBox.makeNew(allocateOnly, true);
        manual = VBox.makeNew(allocateOnly, true);
    }
    
    {
        initInstance(false);
    }
    
    protected  Module_Base() {
        super();
    }
    
    public java.lang.Integer getKeyAppModules() {
        return this.keyAppModules.get(this, "keyAppModules");
    }
    
    public void setKeyAppModules(java.lang.Integer keyAppModules) {
        this.keyAppModules.put(this, "keyAppModules", keyAppModules);
    }
    
    private Object get$keyAppModules() {
        java.lang.Integer value = this.keyAppModules.get(this, "keyAppModules");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyAppModules(java.lang.Integer arg0, int txNumber) {
        this.keyAppModules.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyDesignRoot() {
        return this.keyDesignRoot.get(this, "keyDesignRoot");
    }
    
    public void setKeyDesignRoot(java.lang.Integer keyDesignRoot) {
        this.keyDesignRoot.put(this, "keyDesignRoot", keyDesignRoot);
    }
    
    private Object get$keyDesignRoot() {
        java.lang.Integer value = this.keyDesignRoot.get(this, "keyDesignRoot");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyDesignRoot(java.lang.Integer arg0, int txNumber) {
        this.keyDesignRoot.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyManual() {
        return this.keyManual.get(this, "keyManual");
    }
    
    public void setKeyManual(java.lang.Integer keyManual) {
        this.keyManual.put(this, "keyManual", keyManual);
    }
    
    private Object get$keyManual() {
        java.lang.Integer value = this.keyManual.get(this, "keyManual");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyManual(java.lang.Integer arg0, int txNumber) {
        this.keyManual.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public int getAssembliesCount() {
        return this.assemblies.size();
    }
    
    public boolean hasAnyAssemblies() {
        return (! this.assemblies.isEmpty());
    }
    
    public boolean hasAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly assemblies) {
        return this.assemblies.contains(assemblies);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Assembly> getAssembliesSet() {
        return this.assemblies;
    }
    
    public void addAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly assemblies) {
        AssemblyHasModule.add((pt.ist.fenixframework.example.oo7.domain.Module)this, assemblies);
    }
    
    public void removeAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly assemblies) {
        AssemblyHasModule.remove((pt.ist.fenixframework.example.oo7.domain.Module)this, assemblies);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Assembly> getAssemblies() {
        return assemblies;
    }
    
    public void set$assemblies(OJBFunctionalSetWrapper assemblies) {
        this.assemblies.setFromOJB(this, "assemblies", assemblies);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Assembly> getAssembliesIterator() {
        return assemblies.iterator();
    }
    
    public pt.ist.fenixframework.example.oo7.domain.OO7Application getAppModules() {
        return this.appModules.get(this, "appModules");
    }
    
    public void setAppModules(pt.ist.fenixframework.example.oo7.domain.OO7Application appModules) {
        ApplicationHasModules.add((pt.ist.fenixframework.example.oo7.domain.Module)this, appModules);
    }
    
    public boolean hasAppModules() {
        return (getAppModules() != null);
    }
    
    public void removeAppModules() {
        setAppModules(null);
    }
    
    public void set$appModules(pt.ist.fenixframework.example.oo7.domain.OO7Application appModules) {
        this.appModules.setFromOJB(this, "appModules", appModules);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.ComplexAssembly getDesignRoot() {
        return this.designRoot.get(this, "designRoot");
    }
    
    public void setDesignRoot(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly designRoot) {
        ModuleHasComplexAssembly.add((pt.ist.fenixframework.example.oo7.domain.Module)this, designRoot);
    }
    
    public boolean hasDesignRoot() {
        return (getDesignRoot() != null);
    }
    
    public void removeDesignRoot() {
        setDesignRoot(null);
    }
    
    public void set$designRoot(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly designRoot) {
        this.designRoot.setFromOJB(this, "designRoot", designRoot);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.Manual getManual() {
        return this.manual.get(this, "manual");
    }
    
    public void setManual(pt.ist.fenixframework.example.oo7.domain.Manual manual) {
        ModuleHasManual.add((pt.ist.fenixframework.example.oo7.domain.Module)this, manual);
    }
    
    public boolean hasManual() {
        return (getManual() != null);
    }
    
    public void removeManual() {
        setManual(null);
    }
    
    public void set$manual(pt.ist.fenixframework.example.oo7.domain.Manual manual) {
        this.manual.setFromOJB(this, "manual", manual);
    }
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasAnyAssemblies()) return false;
        if (hasAppModules()) return false;
        if (hasDesignRoot()) return false;
        if (hasManual()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        set$keyAppModules(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_APP_MODULES"), txNumber);
        set$keyDesignRoot(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_DESIGN_ROOT"), txNumber);
        set$keyManual(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_MANUAL"), txNumber);
    }
    
}
