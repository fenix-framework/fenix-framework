package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class Conn_Base extends pt.ist.fenixframework.pstm.AbstractDomainObject {
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> role$$from = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart>("from") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getBox(pt.ist.fenixframework.example.oo7.domain.Conn o1) {
            return ((Conn_Base)o1).from;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Conn o1, java.lang.Integer newFk) {
            o1.setKeyFrom(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.AtomicPart.role$$connectionsFrom;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> role$$to = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart>("to") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> getBox(pt.ist.fenixframework.example.oo7.domain.Conn o1) {
            return ((Conn_Base)o1).to;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Conn o1, java.lang.Integer newFk) {
            o1.setKeyTo(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.AtomicPart,pt.ist.fenixframework.example.oo7.domain.Conn> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.AtomicPart.role$$connectionsTo;
        }
        
    };
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> ConnectionHasAtomicPartFrom;
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Conn,pt.ist.fenixframework.example.oo7.domain.AtomicPart> ConnectionHasAtomicPartTo;
    
    private VBox<java.lang.Long> id;
    private VBox<java.lang.Integer> length;
    private VBox<java.lang.String> type;
    private VBox<java.lang.Integer> keyFrom;
    private VBox<java.lang.Integer> keyTo;
    private VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> from;
    private VBox<pt.ist.fenixframework.example.oo7.domain.AtomicPart> to;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        id = VBox.makeNew(allocateOnly, false);
        length = VBox.makeNew(allocateOnly, false);
        type = VBox.makeNew(allocateOnly, false);
        keyFrom = VBox.makeNew(allocateOnly, false);
        keyTo = VBox.makeNew(allocateOnly, false);
        from = VBox.makeNew(allocateOnly, true);
        to = VBox.makeNew(allocateOnly, true);
    }
    
    {
        initInstance(false);
    }
    
    protected  Conn_Base() {
        super();
    }
    
    public java.lang.Long getId() {
        return this.id.get(this, "id");
    }
    
    public void setId(java.lang.Long id) {
        this.id.put(this, "id", id);
    }
    
    private Object get$id() {
        java.lang.Long value = this.id.get(this, "id");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForLong(value);
    }
    
    private final void set$id(java.lang.Long arg0, int txNumber) {
        this.id.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getLength() {
        return this.length.get(this, "length");
    }
    
    public void setLength(java.lang.Integer length) {
        this.length.put(this, "length", length);
    }
    
    private Object get$length() {
        java.lang.Integer value = this.length.get(this, "length");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$length(java.lang.Integer arg0, int txNumber) {
        this.length.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.String getType() {
        return this.type.get(this, "type");
    }
    
    public void setType(java.lang.String type) {
        this.type.put(this, "type", type);
    }
    
    private Object get$type() {
        java.lang.String value = this.type.get(this, "type");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForString(value);
    }
    
    private final void set$type(java.lang.String arg0, int txNumber) {
        this.type.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyFrom() {
        return this.keyFrom.get(this, "keyFrom");
    }
    
    public void setKeyFrom(java.lang.Integer keyFrom) {
        this.keyFrom.put(this, "keyFrom", keyFrom);
    }
    
    private Object get$keyFrom() {
        java.lang.Integer value = this.keyFrom.get(this, "keyFrom");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyFrom(java.lang.Integer arg0, int txNumber) {
        this.keyFrom.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyTo() {
        return this.keyTo.get(this, "keyTo");
    }
    
    public void setKeyTo(java.lang.Integer keyTo) {
        this.keyTo.put(this, "keyTo", keyTo);
    }
    
    private Object get$keyTo() {
        java.lang.Integer value = this.keyTo.get(this, "keyTo");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyTo(java.lang.Integer arg0, int txNumber) {
        this.keyTo.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.AtomicPart getFrom() {
        return this.from.get(this, "from");
    }
    
    public void setFrom(pt.ist.fenixframework.example.oo7.domain.AtomicPart from) {
        ConnectionHasAtomicPartFrom.add((pt.ist.fenixframework.example.oo7.domain.Conn)this, from);
    }
    
    public boolean hasFrom() {
        return (getFrom() != null);
    }
    
    public void removeFrom() {
        setFrom(null);
    }
    
    public void set$from(pt.ist.fenixframework.example.oo7.domain.AtomicPart from) {
        this.from.setFromOJB(this, "from", from);
    }
    
    public pt.ist.fenixframework.example.oo7.domain.AtomicPart getTo() {
        return this.to.get(this, "to");
    }
    
    public void setTo(pt.ist.fenixframework.example.oo7.domain.AtomicPart to) {
        ConnectionHasAtomicPartTo.add((pt.ist.fenixframework.example.oo7.domain.Conn)this, to);
    }
    
    public boolean hasTo() {
        return (getTo() != null);
    }
    
    public void removeTo() {
        setTo(null);
    }
    
    public void set$to(pt.ist.fenixframework.example.oo7.domain.AtomicPart to) {
        this.to.setFromOJB(this, "to", to);
    }
    
    protected boolean checkDisconnected() {
        if (hasFrom()) return false;
        if (hasTo()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        set$id(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "ID"), txNumber);
        set$length(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "LENGTH"), txNumber);
        set$type(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TYPE"), txNumber);
        set$keyFrom(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_FROM"), txNumber);
        set$keyTo(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_TO"), txNumber);
    }
    
}
