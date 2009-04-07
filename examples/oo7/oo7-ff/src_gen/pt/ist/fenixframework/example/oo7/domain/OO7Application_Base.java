package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class OO7Application_Base extends pt.ist.fenixframework.pstm.AbstractDomainObject {
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart> role$$atomicParts = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getSet(pt.ist.fenixframework.example.oo7.domain.OO7Application o1) {
            return ((OO7Application_Base)o1).atomicParts;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.OO7Application> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.AtomicPart.role$$appAtomicParts;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> role$$baseAssemblies = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getSet(pt.ist.fenixframework.example.oo7.domain.OO7Application o1) {
            return ((OO7Application_Base)o1).baseAssemblies;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.OO7Application> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.BaseAssembly.role$$appBaseAssemblies;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module> role$$modules = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Module> getSet(pt.ist.fenixframework.example.oo7.domain.OO7Application o1) {
            return ((OO7Application_Base)o1).modules;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.OO7Application> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Module.role$$appModules;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document> role$$documents = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Document> getSet(pt.ist.fenixframework.example.oo7.domain.OO7Application o1) {
            return ((OO7Application_Base)o1).documents;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.OO7Application> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Document.role$$appDocuments;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual> role$$manuals = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Manual> getSet(pt.ist.fenixframework.example.oo7.domain.OO7Application o1) {
            return ((OO7Application_Base)o1).manuals;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.OO7Application> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Manual.role$$appManuals;
        }
        
    };
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart> ApplicationHasAtomicParts;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> ApplicationHasBaseAssemblies;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module> ApplicationHasModules;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document> ApplicationHasDocuments;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual> ApplicationHasManuals;
    
    
    private RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart> atomicParts;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> baseAssemblies;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module> modules;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document> documents;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual> manuals;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        atomicParts = new RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart>((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, ApplicationHasAtomicParts, "atomicParts", allocateOnly);
        baseAssemblies = new RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, ApplicationHasBaseAssemblies, "baseAssemblies", allocateOnly);
        modules = new RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Module>((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, ApplicationHasModules, "modules", allocateOnly);
        documents = new RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document>((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, ApplicationHasDocuments, "documents", allocateOnly);
        manuals = new RelationList<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual>((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, ApplicationHasManuals, "manuals", allocateOnly);
    }
    
    {
        initInstance(false);
    }
    
    protected  OO7Application_Base() {
        super();
    }
    
    public int getAtomicPartsCount() {
        return this.atomicParts.size();
    }
    
    public boolean hasAnyAtomicParts() {
        return (! this.atomicParts.isEmpty());
    }
    
    public boolean hasAtomicParts(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicParts) {
        return this.atomicParts.contains(atomicParts);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicPartsSet() {
        return this.atomicParts;
    }
    
    public void addAtomicParts(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicParts) {
        ApplicationHasAtomicParts.add((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, atomicParts);
    }
    
    public void removeAtomicParts(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicParts) {
        ApplicationHasAtomicParts.remove((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, atomicParts);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicParts() {
        return atomicParts;
    }
    
    public void set$atomicParts(OJBFunctionalSetWrapper atomicParts) {
        this.atomicParts.setFromOJB(this, "atomicParts", atomicParts);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicPartsIterator() {
        return atomicParts.iterator();
    }
    
    public int getBaseAssembliesCount() {
        return this.baseAssemblies.size();
    }
    
    public boolean hasAnyBaseAssemblies() {
        return (! this.baseAssemblies.isEmpty());
    }
    
    public boolean hasBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.BaseAssembly baseAssemblies) {
        return this.baseAssemblies.contains(baseAssemblies);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBaseAssembliesSet() {
        return this.baseAssemblies;
    }
    
    public void addBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.BaseAssembly baseAssemblies) {
        ApplicationHasBaseAssemblies.add((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, baseAssemblies);
    }
    
    public void removeBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.BaseAssembly baseAssemblies) {
        ApplicationHasBaseAssemblies.remove((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, baseAssemblies);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBaseAssemblies() {
        return baseAssemblies;
    }
    
    public void set$baseAssemblies(OJBFunctionalSetWrapper baseAssemblies) {
        this.baseAssemblies.setFromOJB(this, "baseAssemblies", baseAssemblies);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBaseAssembliesIterator() {
        return baseAssemblies.iterator();
    }
    
    public int getModulesCount() {
        return this.modules.size();
    }
    
    public boolean hasAnyModules() {
        return (! this.modules.isEmpty());
    }
    
    public boolean hasModules(pt.ist.fenixframework.example.oo7.domain.Module modules) {
        return this.modules.contains(modules);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Module> getModulesSet() {
        return this.modules;
    }
    
    public void addModules(pt.ist.fenixframework.example.oo7.domain.Module modules) {
        ApplicationHasModules.add((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, modules);
    }
    
    public void removeModules(pt.ist.fenixframework.example.oo7.domain.Module modules) {
        ApplicationHasModules.remove((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, modules);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Module> getModules() {
        return modules;
    }
    
    public void set$modules(OJBFunctionalSetWrapper modules) {
        this.modules.setFromOJB(this, "modules", modules);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Module> getModulesIterator() {
        return modules.iterator();
    }
    
    public int getDocumentsCount() {
        return this.documents.size();
    }
    
    public boolean hasAnyDocuments() {
        return (! this.documents.isEmpty());
    }
    
    public boolean hasDocuments(pt.ist.fenixframework.example.oo7.domain.Document documents) {
        return this.documents.contains(documents);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Document> getDocumentsSet() {
        return this.documents;
    }
    
    public void addDocuments(pt.ist.fenixframework.example.oo7.domain.Document documents) {
        ApplicationHasDocuments.add((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, documents);
    }
    
    public void removeDocuments(pt.ist.fenixframework.example.oo7.domain.Document documents) {
        ApplicationHasDocuments.remove((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, documents);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Document> getDocuments() {
        return documents;
    }
    
    public void set$documents(OJBFunctionalSetWrapper documents) {
        this.documents.setFromOJB(this, "documents", documents);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Document> getDocumentsIterator() {
        return documents.iterator();
    }
    
    public int getManualsCount() {
        return this.manuals.size();
    }
    
    public boolean hasAnyManuals() {
        return (! this.manuals.isEmpty());
    }
    
    public boolean hasManuals(pt.ist.fenixframework.example.oo7.domain.Manual manuals) {
        return this.manuals.contains(manuals);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Manual> getManualsSet() {
        return this.manuals;
    }
    
    public void addManuals(pt.ist.fenixframework.example.oo7.domain.Manual manuals) {
        ApplicationHasManuals.add((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, manuals);
    }
    
    public void removeManuals(pt.ist.fenixframework.example.oo7.domain.Manual manuals) {
        ApplicationHasManuals.remove((pt.ist.fenixframework.example.oo7.domain.OO7Application)this, manuals);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Manual> getManuals() {
        return manuals;
    }
    
    public void set$manuals(OJBFunctionalSetWrapper manuals) {
        this.manuals.setFromOJB(this, "manuals", manuals);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Manual> getManualsIterator() {
        return manuals.iterator();
    }
    
    protected boolean checkDisconnected() {
        if (hasAnyAtomicParts()) return false;
        if (hasAnyBaseAssemblies()) return false;
        if (hasAnyModules()) return false;
        if (hasAnyDocuments()) return false;
        if (hasAnyManuals()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        
    }
    
}
