package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class BaseAssembly_Base extends pt.ist.fenixframework.example.oo7.domain.Assembly {
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.OO7Application> role$$appBaseAssemblies = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.OO7Application>("appBaseAssemblies") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> getBox(pt.ist.fenixframework.example.oo7.domain.BaseAssembly o1) {
            return ((BaseAssembly_Base)o1).appBaseAssemblies;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.BaseAssembly o1, java.lang.Integer newFk) {
            o1.setKeyAppBaseAssemblies(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.OO7Application.role$$baseAssemblies;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$unsharedPart = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.BaseAssembly o1) {
            return ((BaseAssembly_Base)o1).unsharedPart;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$ba2;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$sharedPart = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.BaseAssembly o1) {
            return ((BaseAssembly_Base)o1).sharedPart;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$ba1;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$cp3 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.BaseAssembly o1) {
            return ((BaseAssembly_Base)o1).cp3;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$baseAssemblies;
        }
        
    };
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.OO7Application> ApplicationHasBaseAssemblies = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.OO7Application>(role$$appBaseAssemblies);
    static {
        pt.ist.fenixframework.example.oo7.domain.OO7Application.ApplicationHasBaseAssemblies = ApplicationHasBaseAssemblies.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> BaseAssemblyHasUnsharedParts = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>(role$$unsharedPart);
    static {
        pt.ist.fenixframework.example.oo7.domain.CompositePart.BaseAssemblyHasUnsharedParts = BaseAssemblyHasUnsharedParts.getInverseRelation();
    }
    
    static {
        BaseAssemblyHasUnsharedParts.addListener(new dml.runtime.RelationAdapter<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
            @Override
            public void beforeAdd(pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg0, pt.ist.fenixframework.example.oo7.domain.CompositePart arg1) {
                pt.ist.fenixframework.pstm.Transaction.addRelationTuple("BaseAssemblyHasUnsharedParts", arg1, "ba2", arg0, "unsharedPart");
            }
            @Override
            public void beforeRemove(pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg0, pt.ist.fenixframework.example.oo7.domain.CompositePart arg1) {
                pt.ist.fenixframework.pstm.Transaction.removeRelationTuple("BaseAssemblyHasUnsharedParts", arg1, "ba2", arg0, "unsharedPart");
            }
            
        }
        );
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> BaseAssemblyHasSharedParts = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>(role$$sharedPart);
    static {
        pt.ist.fenixframework.example.oo7.domain.CompositePart.BaseAssemblyHasSharedParts = BaseAssemblyHasSharedParts.getInverseRelation();
    }
    
    static {
        BaseAssemblyHasSharedParts.addListener(new dml.runtime.RelationAdapter<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
            @Override
            public void beforeAdd(pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg0, pt.ist.fenixframework.example.oo7.domain.CompositePart arg1) {
                pt.ist.fenixframework.pstm.Transaction.addRelationTuple("BaseAssemblyHasSharedParts", arg1, "ba1", arg0, "sharedPart");
            }
            @Override
            public void beforeRemove(pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg0, pt.ist.fenixframework.example.oo7.domain.CompositePart arg1) {
                pt.ist.fenixframework.pstm.Transaction.removeRelationTuple("BaseAssemblyHasSharedParts", arg1, "ba1", arg0, "sharedPart");
            }
            
        }
        );
    }
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> CompositePartHasBaseAssemblies;
    
    private VBox<java.lang.Integer> keyAppBaseAssemblies;
    private VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> appBaseAssemblies;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> unsharedPart;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> sharedPart;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> cp3;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        keyAppBaseAssemblies = VBox.makeNew(allocateOnly, false);
        appBaseAssemblies = VBox.makeNew(allocateOnly, true);
        unsharedPart = new RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, BaseAssemblyHasUnsharedParts, "unsharedPart", allocateOnly);
        sharedPart = new RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, BaseAssemblyHasSharedParts, "sharedPart", allocateOnly);
        cp3 = new RelationList<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, CompositePartHasBaseAssemblies, "cp3", allocateOnly);
    }
    
    {
        initInstance(false);
    }
    
    protected  BaseAssembly_Base() {
        super();
    }
    
    public java.lang.Integer getKeyAppBaseAssemblies() {
        return this.keyAppBaseAssemblies.get(this, "keyAppBaseAssemblies");
    }
    
    public void setKeyAppBaseAssemblies(java.lang.Integer keyAppBaseAssemblies) {
        this.keyAppBaseAssemblies.put(this, "keyAppBaseAssemblies", keyAppBaseAssemblies);
    }
    
    private Object get$keyAppBaseAssemblies() {
        java.lang.Integer value = this.keyAppBaseAssemblies.get(this, "keyAppBaseAssemblies");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyAppBaseAssemblies(java.lang.Integer arg0, int txNumber) {
        this.keyAppBaseAssemblies.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.OO7Application getAppBaseAssemblies() {
        return this.appBaseAssemblies.get(this, "appBaseAssemblies");
    }
    
    public void setAppBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.OO7Application appBaseAssemblies) {
        ApplicationHasBaseAssemblies.add((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, appBaseAssemblies);
    }
    
    public boolean hasAppBaseAssemblies() {
        return (getAppBaseAssemblies() != null);
    }
    
    public void removeAppBaseAssemblies() {
        setAppBaseAssemblies(null);
    }
    
    public void set$appBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.OO7Application appBaseAssemblies) {
        this.appBaseAssemblies.setFromOJB(this, "appBaseAssemblies", appBaseAssemblies);
    }
    
    public int getUnsharedPartCount() {
        return this.unsharedPart.size();
    }
    
    public boolean hasAnyUnsharedPart() {
        return (! this.unsharedPart.isEmpty());
    }
    
    public boolean hasUnsharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart unsharedPart) {
        return this.unsharedPart.contains(unsharedPart);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getUnsharedPartSet() {
        return this.unsharedPart;
    }
    
    public void addUnsharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart unsharedPart) {
        BaseAssemblyHasUnsharedParts.add((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, unsharedPart);
    }
    
    public void removeUnsharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart unsharedPart) {
        BaseAssemblyHasUnsharedParts.remove((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, unsharedPart);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getUnsharedPart() {
        return unsharedPart;
    }
    
    public void set$unsharedPart(OJBFunctionalSetWrapper unsharedPart) {
        this.unsharedPart.setFromOJB(this, "unsharedPart", unsharedPart);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getUnsharedPartIterator() {
        return unsharedPart.iterator();
    }
    
    public int getSharedPartCount() {
        return this.sharedPart.size();
    }
    
    public boolean hasAnySharedPart() {
        return (! this.sharedPart.isEmpty());
    }
    
    public boolean hasSharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart sharedPart) {
        return this.sharedPart.contains(sharedPart);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSharedPartSet() {
        return this.sharedPart;
    }
    
    public void addSharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart sharedPart) {
        BaseAssemblyHasSharedParts.add((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, sharedPart);
    }
    
    public void removeSharedPart(pt.ist.fenixframework.example.oo7.domain.CompositePart sharedPart) {
        BaseAssemblyHasSharedParts.remove((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, sharedPart);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSharedPart() {
        return sharedPart;
    }
    
    public void set$sharedPart(OJBFunctionalSetWrapper sharedPart) {
        this.sharedPart.setFromOJB(this, "sharedPart", sharedPart);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSharedPartIterator() {
        return sharedPart.iterator();
    }
    
    public int getCp3Count() {
        return this.cp3.size();
    }
    
    public boolean hasAnyCp3() {
        return (! this.cp3.isEmpty());
    }
    
    public boolean hasCp3(pt.ist.fenixframework.example.oo7.domain.CompositePart cp3) {
        return this.cp3.contains(cp3);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp3Set() {
        return this.cp3;
    }
    
    public void addCp3(pt.ist.fenixframework.example.oo7.domain.CompositePart cp3) {
        CompositePartHasBaseAssemblies.add((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, cp3);
    }
    
    public void removeCp3(pt.ist.fenixframework.example.oo7.domain.CompositePart cp3) {
        CompositePartHasBaseAssemblies.remove((pt.ist.fenixframework.example.oo7.domain.BaseAssembly)this, cp3);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp3() {
        return cp3;
    }
    
    public void set$cp3(OJBFunctionalSetWrapper cp3) {
        this.cp3.setFromOJB(this, "cp3", cp3);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp3Iterator() {
        return cp3.iterator();
    }
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasAppBaseAssemblies()) return false;
        if (hasAnyUnsharedPart()) return false;
        if (hasAnySharedPart()) return false;
        if (hasAnyCp3()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        set$keyAppBaseAssemblies(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_APP_BASE_ASSEMBLIES"), txNumber);
    }
    
}
