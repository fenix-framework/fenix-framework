package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class ComplexAssembly_Base extends pt.ist.fenixframework.example.oo7.domain.Assembly {
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly> role$$subAssemblies = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Assembly> getSet(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly o1) {
            return ((ComplexAssembly_Base)o1).subAssemblies;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Assembly,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Assembly.role$$superAssembly;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module> role$$md2 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Module> getSet(pt.ist.fenixframework.example.oo7.domain.ComplexAssembly o1) {
            return ((ComplexAssembly_Base)o1).md2;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.ComplexAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Module.role$$designRoot;
        }
        
    };
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly> AssemblyHasComplexAssembly = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly>(role$$subAssemblies);
    static {
        pt.ist.fenixframework.example.oo7.domain.Assembly.AssemblyHasComplexAssembly = AssemblyHasComplexAssembly.getInverseRelation();
    }
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module> ModuleHasComplexAssembly;
    
    
    private RelationList<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly> subAssemblies;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module> md2;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        subAssemblies = new RelationList<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Assembly>((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, AssemblyHasComplexAssembly, "subAssemblies", allocateOnly);
        md2 = new RelationList<pt.ist.fenixframework.example.oo7.domain.ComplexAssembly,pt.ist.fenixframework.example.oo7.domain.Module>((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, ModuleHasComplexAssembly, "md2", allocateOnly);
    }
    
    {
        initInstance(false);
    }
    
    protected  ComplexAssembly_Base() {
        super();
    }
    
    public int getSubAssembliesCount() {
        return this.subAssemblies.size();
    }
    
    public boolean hasAnySubAssemblies() {
        return (! this.subAssemblies.isEmpty());
    }
    
    public boolean hasSubAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly subAssemblies) {
        return this.subAssemblies.contains(subAssemblies);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Assembly> getSubAssembliesSet() {
        return this.subAssemblies;
    }
    
    public void addSubAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly subAssemblies) {
        AssemblyHasComplexAssembly.add((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, subAssemblies);
    }
    
    public void removeSubAssemblies(pt.ist.fenixframework.example.oo7.domain.Assembly subAssemblies) {
        AssemblyHasComplexAssembly.remove((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, subAssemblies);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Assembly> getSubAssemblies() {
        return subAssemblies;
    }
    
    public void set$subAssemblies(OJBFunctionalSetWrapper subAssemblies) {
        this.subAssemblies.setFromOJB(this, "subAssemblies", subAssemblies);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Assembly> getSubAssembliesIterator() {
        return subAssemblies.iterator();
    }
    
    public int getMd2Count() {
        return this.md2.size();
    }
    
    public boolean hasAnyMd2() {
        return (! this.md2.isEmpty());
    }
    
    public boolean hasMd2(pt.ist.fenixframework.example.oo7.domain.Module md2) {
        return this.md2.contains(md2);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Module> getMd2Set() {
        return this.md2;
    }
    
    public void addMd2(pt.ist.fenixframework.example.oo7.domain.Module md2) {
        ModuleHasComplexAssembly.add((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, md2);
    }
    
    public void removeMd2(pt.ist.fenixframework.example.oo7.domain.Module md2) {
        ModuleHasComplexAssembly.remove((pt.ist.fenixframework.example.oo7.domain.ComplexAssembly)this, md2);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Module> getMd2() {
        return md2;
    }
    
    public void set$md2(OJBFunctionalSetWrapper md2) {
        this.md2.setFromOJB(this, "md2", md2);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Module> getMd2Iterator() {
        return md2.iterator();
    }
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasAnySubAssemblies()) return false;
        if (hasAnyMd2()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        
    }
    
}
