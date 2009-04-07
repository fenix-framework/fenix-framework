package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class CompositePart_Base extends pt.ist.fenixframework.example.oo7.domain.DesignObj {
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.Document> role$$document = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.Document>("document") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.Document> getBox(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).document;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.CompositePart o1, java.lang.Integer newFk) {
            o1.setKeyDocument(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Document.role$$cp4;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> role$$rootPart = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>("rootPart") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getBox(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).rootPart;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.CompositePart o1, java.lang.Integer newFk) {
            o1.setKeyRootPart(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.AtomicPart.role$$cp2;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> role$$atomicPart = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getSet(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).atomicPart;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.AtomicPart.role$$cp1;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> role$$ba2 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getSet(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).ba2;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.BaseAssembly.role$$unsharedPart;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> role$$ba1 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getSet(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).ba1;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.BaseAssembly.role$$sharedPart;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> role$$baseAssemblies = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getSet(pt.ist.fenixframework.example.oo7.domain.CompositePart o1) {
            return ((CompositePart_Base)o1).baseAssemblies;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.BaseAssembly,pt.ist.fenixframework.example.oo7.domain.CompositePart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.BaseAssembly.role$$cp3;
        }
        
    };
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.Document> CompositePartHasDocument = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.Document>(role$$document);
    static {
        pt.ist.fenixframework.example.oo7.domain.Document.CompositePartHasDocument = CompositePartHasDocument.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> CompositePartHasRootPart = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>(role$$rootPart);
    static {
        pt.ist.fenixframework.example.oo7.domain.AtomicPart.CompositePartHasRootPart = CompositePartHasRootPart.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> CompositePartHasAtomicParts = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>(role$$atomicPart);
    static {
        pt.ist.fenixframework.example.oo7.domain.AtomicPart.CompositePartHasAtomicParts = CompositePartHasAtomicParts.getInverseRelation();
    }
    
    static {
        CompositePartHasAtomicParts.addListener(new dml.runtime.RelationAdapter<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>() {
            @Override
            public void beforeAdd(pt.ist.fenixframework.example.oo7.domain.CompositePart arg0, pt.ist.fenixframework.example.oo7.domain.AtomicPart arg1) {
                pt.ist.fenixframework.pstm.Transaction.addRelationTuple("CompositePartHasAtomicParts", arg1, "cp1", arg0, "atomicPart");
            }
            @Override
            public void beforeRemove(pt.ist.fenixframework.example.oo7.domain.CompositePart arg0, pt.ist.fenixframework.example.oo7.domain.AtomicPart arg1) {
                pt.ist.fenixframework.pstm.Transaction.removeRelationTuple("CompositePartHasAtomicParts", arg1, "cp1", arg0, "atomicPart");
            }
            
        }
        );
    }
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> BaseAssemblyHasUnsharedParts;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> BaseAssemblyHasSharedParts;
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> CompositePartHasBaseAssemblies = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>(role$$baseAssemblies);
    static {
        pt.ist.fenixframework.example.oo7.domain.BaseAssembly.CompositePartHasBaseAssemblies = CompositePartHasBaseAssemblies.getInverseRelation();
    }
    
    static {
        CompositePartHasBaseAssemblies.addListener(new dml.runtime.RelationAdapter<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>() {
            @Override
            public void beforeAdd(pt.ist.fenixframework.example.oo7.domain.CompositePart arg0, pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg1) {
                pt.ist.fenixframework.pstm.Transaction.addRelationTuple("CompositePartHasBaseAssemblies", arg1, "cp3", arg0, "baseAssemblies");
            }
            @Override
            public void beforeRemove(pt.ist.fenixframework.example.oo7.domain.CompositePart arg0, pt.ist.fenixframework.example.oo7.domain.BaseAssembly arg1) {
                pt.ist.fenixframework.pstm.Transaction.removeRelationTuple("CompositePartHasBaseAssemblies", arg1, "cp3", arg0, "baseAssemblies");
            }
            
        }
        );
    }
    
    private VBox<java.lang.Integer> keyDocument;
    private VBox<java.lang.Integer> keyRootPart;
    private VBox<pt.ist.fenixframework.example.oo7.domain.Document> document;
    private VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> rootPart;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> atomicPart;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> ba2;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> ba1;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly> baseAssemblies;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        keyDocument = VBox.makeNew(allocateOnly, false);
        keyRootPart = VBox.makeNew(allocateOnly, false);
        document = VBox.makeNew(allocateOnly, true);
        rootPart = VBox.makeNew(allocateOnly, true);
        atomicPart = new RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart>((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, CompositePartHasAtomicParts, "atomicPart", allocateOnly);
        ba2 = new RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, BaseAssemblyHasUnsharedParts, "ba2", allocateOnly);
        ba1 = new RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, BaseAssemblyHasSharedParts, "ba1", allocateOnly);
        baseAssemblies = new RelationList<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.BaseAssembly>((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, CompositePartHasBaseAssemblies, "baseAssemblies", allocateOnly);
    }
    
    {
        initInstance(false);
    }
    
    protected  CompositePart_Base() {
        super();
    }
    
    public java.lang.Integer getKeyDocument() {
        return this.keyDocument.get(this, "keyDocument");
    }
    
    public void setKeyDocument(java.lang.Integer keyDocument) {
        this.keyDocument.put(this, "keyDocument", keyDocument);
    }
    
    private Object get$keyDocument() {
        java.lang.Integer value = this.keyDocument.get(this, "keyDocument");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyDocument(java.lang.Integer arg0, int txNumber) {
        this.keyDocument.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyRootPart() {
        return this.keyRootPart.get(this, "keyRootPart");
    }
    
    public void setKeyRootPart(java.lang.Integer keyRootPart) {
        this.keyRootPart.put(this, "keyRootPart", keyRootPart);
    }
    
    private Object get$keyRootPart() {
        java.lang.Integer value = this.keyRootPart.get(this, "keyRootPart");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyRootPart(java.lang.Integer arg0, int txNumber) {
        this.keyRootPart.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.Document getDocument() {
        return this.document.get(this, "document");
    }
    
    public void setDocument(pt.ist.fenixframework.example.oo7.domain.Document document) {
        CompositePartHasDocument.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, document);
    }
    
    public boolean hasDocument() {
        return (getDocument() != null);
    }
    
    public void removeDocument() {
        setDocument(null);
    }
    
    public void set$document(pt.ist.fenixframework.example.oo7.domain.Document document) {
        this.document.setFromOJB(this, "document", document);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.AtomicPart getRootPart() {
        return this.rootPart.get(this, "rootPart");
    }
    
    public void setRootPart(pt.ist.fenixframework.example.oo7.domain.AtomicPart rootPart) {
        CompositePartHasRootPart.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, rootPart);
    }
    
    public boolean hasRootPart() {
        return (getRootPart() != null);
    }
    
    public void removeRootPart() {
        setRootPart(null);
    }
    
    public void set$rootPart(pt.ist.fenixframework.example.oo7.domain.AtomicPart rootPart) {
        this.rootPart.setFromOJB(this, "rootPart", rootPart);
    }
    
    public int getAtomicPartCount() {
        return this.atomicPart.size();
    }
    
    public boolean hasAnyAtomicPart() {
        return (! this.atomicPart.isEmpty());
    }
    
    public boolean hasAtomicPart(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicPart) {
        return this.atomicPart.contains(atomicPart);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicPartSet() {
        return this.atomicPart;
    }
    
    public void addAtomicPart(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicPart) {
        CompositePartHasAtomicParts.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, atomicPart);
    }
    
    public void removeAtomicPart(pt.ist.fenixframework.example.oo7.domain.AtomicPart atomicPart) {
        CompositePartHasAtomicParts.remove((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, atomicPart);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicPart() {
        return atomicPart;
    }
    
    public void set$atomicPart(OJBFunctionalSetWrapper atomicPart) {
        this.atomicPart.setFromOJB(this, "atomicPart", atomicPart);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getAtomicPartIterator() {
        return atomicPart.iterator();
    }
    
    public int getBa2Count() {
        return this.ba2.size();
    }
    
    public boolean hasAnyBa2() {
        return (! this.ba2.isEmpty());
    }
    
    public boolean hasBa2(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba2) {
        return this.ba2.contains(ba2);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa2Set() {
        return this.ba2;
    }
    
    public void addBa2(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba2) {
        BaseAssemblyHasUnsharedParts.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, ba2);
    }
    
    public void removeBa2(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba2) {
        BaseAssemblyHasUnsharedParts.remove((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, ba2);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa2() {
        return ba2;
    }
    
    public void set$ba2(OJBFunctionalSetWrapper ba2) {
        this.ba2.setFromOJB(this, "ba2", ba2);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa2Iterator() {
        return ba2.iterator();
    }
    
    public int getBa1Count() {
        return this.ba1.size();
    }
    
    public boolean hasAnyBa1() {
        return (! this.ba1.isEmpty());
    }
    
    public boolean hasBa1(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba1) {
        return this.ba1.contains(ba1);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa1Set() {
        return this.ba1;
    }
    
    public void addBa1(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba1) {
        BaseAssemblyHasSharedParts.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, ba1);
    }
    
    public void removeBa1(pt.ist.fenixframework.example.oo7.domain.BaseAssembly ba1) {
        BaseAssemblyHasSharedParts.remove((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, ba1);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa1() {
        return ba1;
    }
    
    public void set$ba1(OJBFunctionalSetWrapper ba1) {
        this.ba1.setFromOJB(this, "ba1", ba1);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.BaseAssembly> getBa1Iterator() {
        return ba1.iterator();
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
        CompositePartHasBaseAssemblies.add((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, baseAssemblies);
    }
    
    public void removeBaseAssemblies(pt.ist.fenixframework.example.oo7.domain.BaseAssembly baseAssemblies) {
        CompositePartHasBaseAssemblies.remove((pt.ist.fenixframework.example.oo7.domain.CompositePart)this, baseAssemblies);
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
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasDocument()) return false;
        if (hasRootPart()) return false;
        if (hasAnyAtomicPart()) return false;
        if (hasAnyBa2()) return false;
        if (hasAnyBa1()) return false;
        if (hasAnyBaseAssemblies()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        set$keyDocument(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_DOCUMENT"), txNumber);
        set$keyRootPart(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_ROOT_PART"), txNumber);
    }
    
}
