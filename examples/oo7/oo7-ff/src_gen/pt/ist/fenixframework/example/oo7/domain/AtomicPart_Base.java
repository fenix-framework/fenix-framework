package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class AtomicPart_Base extends pt.ist.fenixframework.example.oo7.domain.DesignObj {
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.OO7Application> role$$appAtomicParts = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.OO7Application>("appAtomicParts") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> getBox(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1) {
            return ((AtomicPart_Base)o1).appAtomicParts;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1, java.lang.Integer newFk) {
            o1.setKeyAppAtomicParts(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.AtomicPart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.OO7Application.role$$atomicParts;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$cp2 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1) {
            return ((AtomicPart_Base)o1).cp2;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$rootPart;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$cp1 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1) {
            return ((AtomicPart_Base)o1).cp1;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.AtomicPart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$atomicPart;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> role$$connectionsFrom = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Conn> getSet(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1) {
            return ((AtomicPart_Base)o1).connectionsFrom;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Conn.role$$from;
        }
        
    };
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> role$$connectionsTo = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Conn> getSet(pt.ist.fenixframework.example.oo7.domain.AtomicPart o1) {
            return ((AtomicPart_Base)o1).connectionsTo;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Conn.role$$to;
        }
        
    };
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.OO7Application> ApplicationHasAtomicParts = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.OO7Application>(role$$appAtomicParts);
    static {
        pt.ist.fenixframework.example.oo7.domain.OO7Application.ApplicationHasAtomicParts = ApplicationHasAtomicParts.getInverseRelation();
    }
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> CompositePartHasRootPart;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> CompositePartHasAtomicParts;
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> ConnectionHasAtomicPartFrom = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>(role$$connectionsFrom);
    static {
        pt.ist.fenixframework.example.oo7.domain.Conn.ConnectionHasAtomicPartFrom = ConnectionHasAtomicPartFrom.getInverseRelation();
    }
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> ConnectionHasAtomicPartTo = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>(role$$connectionsTo);
    static {
        pt.ist.fenixframework.example.oo7.domain.Conn.ConnectionHasAtomicPartTo = ConnectionHasAtomicPartTo.getInverseRelation();
    }
    
    private VBox<java.lang.Integer> x;
    private VBox<java.lang.Integer> y;
    private VBox<java.lang.Long> docId;
    private VBox<java.lang.Integer> keyAppAtomicParts;
    private VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> appAtomicParts;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> cp2;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart> cp1;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> connectionsFrom;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> connectionsTo;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        x = VBox.makeNew(allocateOnly, false);
        y = VBox.makeNew(allocateOnly, false);
        docId = VBox.makeNew(allocateOnly, false);
        keyAppAtomicParts = VBox.makeNew(allocateOnly, false);
        appAtomicParts = VBox.makeNew(allocateOnly, true);
        cp2 = new RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, CompositePartHasRootPart, "cp2", allocateOnly);
        cp1 = new RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, CompositePartHasAtomicParts, "cp1", allocateOnly);
        connectionsFrom = new RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, ConnectionHasAtomicPartFrom, "connectionsFrom", allocateOnly);
        connectionsTo = new RelationList<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn>((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, ConnectionHasAtomicPartTo, "connectionsTo", allocateOnly);
    }
    
    {
        initInstance(false);
    }
    
    protected  AtomicPart_Base() {
        super();
    }
    
    public java.lang.Integer getX() {
        return this.x.get(this, "x");
    }
    
    public void setX(java.lang.Integer x) {
        this.x.put(this, "x", x);
    }
    
    private Object get$x() {
        java.lang.Integer value = this.x.get(this, "x");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$x(java.lang.Integer arg0, int txNumber) {
        this.x.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getY() {
        return this.y.get(this, "y");
    }
    
    public void setY(java.lang.Integer y) {
        this.y.put(this, "y", y);
    }
    
    private Object get$y() {
        java.lang.Integer value = this.y.get(this, "y");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$y(java.lang.Integer arg0, int txNumber) {
        this.y.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Long getDocId() {
        return this.docId.get(this, "docId");
    }
    
    public void setDocId(java.lang.Long docId) {
        this.docId.put(this, "docId", docId);
    }
    
    private Object get$docId() {
        java.lang.Long value = this.docId.get(this, "docId");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForLong(value);
    }
    
    private final void set$docId(java.lang.Long arg0, int txNumber) {
        this.docId.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyAppAtomicParts() {
        return this.keyAppAtomicParts.get(this, "keyAppAtomicParts");
    }
    
    public void setKeyAppAtomicParts(java.lang.Integer keyAppAtomicParts) {
        this.keyAppAtomicParts.put(this, "keyAppAtomicParts", keyAppAtomicParts);
    }
    
    private Object get$keyAppAtomicParts() {
        java.lang.Integer value = this.keyAppAtomicParts.get(this, "keyAppAtomicParts");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyAppAtomicParts(java.lang.Integer arg0, int txNumber) {
        this.keyAppAtomicParts.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.OO7Application getAppAtomicParts() {
        return this.appAtomicParts.get(this, "appAtomicParts");
    }
    
    public void setAppAtomicParts(pt.ist.fenixframework.example.oo7.domain.OO7Application appAtomicParts) {
        ApplicationHasAtomicParts.add((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, appAtomicParts);
    }
    
    public boolean hasAppAtomicParts() {
        return (getAppAtomicParts() != null);
    }
    
    public void removeAppAtomicParts() {
        setAppAtomicParts(null);
    }
    
    public void set$appAtomicParts(pt.ist.fenixframework.example.oo7.domain.OO7Application appAtomicParts) {
        this.appAtomicParts.setFromOJB(this, "appAtomicParts", appAtomicParts);
    }
    
    public int getCp2Count() {
        return this.cp2.size();
    }
    
    public boolean hasAnyCp2() {
        return (! this.cp2.isEmpty());
    }
    
    public boolean hasCp2(pt.ist.fenixframework.example.oo7.domain.CompositePart cp2) {
        return this.cp2.contains(cp2);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp2Set() {
        return this.cp2;
    }
    
    public void addCp2(pt.ist.fenixframework.example.oo7.domain.CompositePart cp2) {
        CompositePartHasRootPart.add((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, cp2);
    }
    
    public void removeCp2(pt.ist.fenixframework.example.oo7.domain.CompositePart cp2) {
        CompositePartHasRootPart.remove((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, cp2);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp2() {
        return cp2;
    }
    
    public void set$cp2(OJBFunctionalSetWrapper cp2) {
        this.cp2.setFromOJB(this, "cp2", cp2);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp2Iterator() {
        return cp2.iterator();
    }
    
    public int getCp1Count() {
        return this.cp1.size();
    }
    
    public boolean hasAnyCp1() {
        return (! this.cp1.isEmpty());
    }
    
    public boolean hasCp1(pt.ist.fenixframework.example.oo7.domain.CompositePart cp1) {
        return this.cp1.contains(cp1);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp1Set() {
        return this.cp1;
    }
    
    public void addCp1(pt.ist.fenixframework.example.oo7.domain.CompositePart cp1) {
        CompositePartHasAtomicParts.add((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, cp1);
    }
    
    public void removeCp1(pt.ist.fenixframework.example.oo7.domain.CompositePart cp1) {
        CompositePartHasAtomicParts.remove((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, cp1);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp1() {
        return cp1;
    }
    
    public void set$cp1(OJBFunctionalSetWrapper cp1) {
        this.cp1.setFromOJB(this, "cp1", cp1);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp1Iterator() {
        return cp1.iterator();
    }
    
    public int getConnectionsFromCount() {
        return this.connectionsFrom.size();
    }
    
    public boolean hasAnyConnectionsFrom() {
        return (! this.connectionsFrom.isEmpty());
    }
    
    public boolean hasConnectionsFrom(pt.ist.fenixframework.example.oo7.domain.Conn connectionsFrom) {
        return this.connectionsFrom.contains(connectionsFrom);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsFromSet() {
        return this.connectionsFrom;
    }
    
    public void addConnectionsFrom(pt.ist.fenixframework.example.oo7.domain.Conn connectionsFrom) {
        ConnectionHasAtomicPartFrom.add((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, connectionsFrom);
    }
    
    public void removeConnectionsFrom(pt.ist.fenixframework.example.oo7.domain.Conn connectionsFrom) {
        ConnectionHasAtomicPartFrom.remove((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, connectionsFrom);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsFrom() {
        return connectionsFrom;
    }
    
    public void set$connectionsFrom(OJBFunctionalSetWrapper connectionsFrom) {
        this.connectionsFrom.setFromOJB(this, "connectionsFrom", connectionsFrom);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsFromIterator() {
        return connectionsFrom.iterator();
    }
    
    public int getConnectionsToCount() {
        return this.connectionsTo.size();
    }
    
    public boolean hasAnyConnectionsTo() {
        return (! this.connectionsTo.isEmpty());
    }
    
    public boolean hasConnectionsTo(pt.ist.fenixframework.example.oo7.domain.Conn connectionsTo) {
        return this.connectionsTo.contains(connectionsTo);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsToSet() {
        return this.connectionsTo;
    }
    
    public void addConnectionsTo(pt.ist.fenixframework.example.oo7.domain.Conn connectionsTo) {
        ConnectionHasAtomicPartTo.add((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, connectionsTo);
    }
    
    public void removeConnectionsTo(pt.ist.fenixframework.example.oo7.domain.Conn connectionsTo) {
        ConnectionHasAtomicPartTo.remove((pt.ist.fenixframework.example.oo7.domain.AtomicPart)this, connectionsTo);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsTo() {
        return connectionsTo;
    }
    
    public void set$connectionsTo(OJBFunctionalSetWrapper connectionsTo) {
        this.connectionsTo.setFromOJB(this, "connectionsTo", connectionsTo);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Conn> getConnectionsToIterator() {
        return connectionsTo.iterator();
    }
    
    protected boolean checkDisconnected() {
        if (! super.checkDisconnected()) return false;
        if (hasAppAtomicParts()) return false;
        if (hasAnyCp2()) return false;
        if (hasAnyCp1()) return false;
        if (hasAnyConnectionsFrom()) return false;
        if (hasAnyConnectionsTo()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        super.readSlotsFromResultSet(rs, txNumber);
        set$x(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "X"), txNumber);
        set$y(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "Y"), txNumber);
        set$docId(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "DOC_ID"), txNumber);
        set$keyAppAtomicParts(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_APP_ATOMIC_PARTS"), txNumber);
    }
    
}
