package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class DesignObj_Base extends pt.ist.fenixframework.pstm.AbstractDomainObject {
    
    private VBox<java.lang.Long> id;
    private VBox<java.lang.String> type;
    private VBox<java.lang.Long> buildDate;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        id = VBox.makeNew(allocateOnly, false);
        type = VBox.makeNew(allocateOnly, false);
        buildDate = VBox.makeNew(allocateOnly, false);
    }
    
    {
        initInstance(false);
    }
    
    protected  DesignObj_Base() {
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
    
    public java.lang.Long getBuildDate() {
        return this.buildDate.get(this, "buildDate");
    }
    
    public void setBuildDate(java.lang.Long buildDate) {
        this.buildDate.put(this, "buildDate", buildDate);
    }
    
    private Object get$buildDate() {
        java.lang.Long value = this.buildDate.get(this, "buildDate");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForLong(value);
    }
    
    private final void set$buildDate(java.lang.Long arg0, int txNumber) {
        this.buildDate.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    protected boolean checkDisconnected() {
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        set$id(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "ID"), txNumber);
        set$type(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TYPE"), txNumber);
        set$buildDate(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "BUILD_DATE"), txNumber);
    }
    
}
