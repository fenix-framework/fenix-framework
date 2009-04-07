package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class Document_Base extends pt.ist.fenixframework.pstm.AbstractDomainObject {
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart> role$$cp4 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.CompositePart> getSet(pt.ist.fenixframework.example.oo7.domain.Document o1) {
            return ((Document_Base)o1).cp4;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.CompositePart,pt.ist.fenixframework.example.oo7.domain.Document> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.CompositePart.role$$document;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.OO7Application> role$$appDocuments = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.OO7Application>("appDocuments") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> getBox(pt.ist.fenixframework.example.oo7.domain.Document o1) {
            return ((Document_Base)o1).appDocuments;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Document o1, java.lang.Integer newFk) {
            o1.setKeyAppDocuments(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Document> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.OO7Application.role$$documents;
        }
        
    };
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart> CompositePartHasDocument;
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.OO7Application> ApplicationHasDocuments = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.OO7Application>(role$$appDocuments);
    static {
        pt.ist.fenixframework.example.oo7.domain.OO7Application.ApplicationHasDocuments = ApplicationHasDocuments.getInverseRelation();
    }
    
    private VBox<java.lang.String> title;
    private VBox<java.lang.Long> id;
    private VBox<java.lang.String> text;
    private VBox<java.lang.Integer> keyAppDocuments;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart> cp4;
    private VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> appDocuments;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        title = VBox.makeNew(allocateOnly, false);
        id = VBox.makeNew(allocateOnly, false);
        text = VBox.makeNew(allocateOnly, false);
        keyAppDocuments = VBox.makeNew(allocateOnly, false);
        cp4 = new RelationList<pt.ist.fenixframework.example.oo7.domain.Document,pt.ist.fenixframework.example.oo7.domain.CompositePart>((pt.ist.fenixframework.example.oo7.domain.Document)this, CompositePartHasDocument, "cp4", allocateOnly);
        appDocuments = VBox.makeNew(allocateOnly, true);
    }
    
    {
        initInstance(false);
    }
    
    protected  Document_Base() {
        super();
    }
    
    public java.lang.String getTitle() {
        return this.title.get(this, "title");
    }
    
    public void setTitle(java.lang.String title) {
        this.title.put(this, "title", title);
    }
    
    private Object get$title() {
        java.lang.String value = this.title.get(this, "title");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForString(value);
    }
    
    private final void set$title(java.lang.String arg0, int txNumber) {
        this.title.persistentLoad((arg0 == null) ? null : arg0, txNumber);
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
    
    public java.lang.String getText() {
        return this.text.get(this, "text");
    }
    
    public void setText(java.lang.String text) {
        this.text.put(this, "text", text);
    }
    
    private Object get$text() {
        java.lang.String value = this.text.get(this, "text");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForString(value);
    }
    
    private final void set$text(java.lang.String arg0, int txNumber) {
        this.text.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyAppDocuments() {
        return this.keyAppDocuments.get(this, "keyAppDocuments");
    }
    
    public void setKeyAppDocuments(java.lang.Integer keyAppDocuments) {
        this.keyAppDocuments.put(this, "keyAppDocuments", keyAppDocuments);
    }
    
    private Object get$keyAppDocuments() {
        java.lang.Integer value = this.keyAppDocuments.get(this, "keyAppDocuments");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyAppDocuments(java.lang.Integer arg0, int txNumber) {
        this.keyAppDocuments.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public int getCp4Count() {
        return this.cp4.size();
    }
    
    public boolean hasAnyCp4() {
        return (! this.cp4.isEmpty());
    }
    
    public boolean hasCp4(pt.ist.fenixframework.example.oo7.domain.CompositePart cp4) {
        return this.cp4.contains(cp4);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp4Set() {
        return this.cp4;
    }
    
    public void addCp4(pt.ist.fenixframework.example.oo7.domain.CompositePart cp4) {
        CompositePartHasDocument.add((pt.ist.fenixframework.example.oo7.domain.Document)this, cp4);
    }
    
    public void removeCp4(pt.ist.fenixframework.example.oo7.domain.CompositePart cp4) {
        CompositePartHasDocument.remove((pt.ist.fenixframework.example.oo7.domain.Document)this, cp4);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp4() {
        return cp4;
    }
    
    public void set$cp4(OJBFunctionalSetWrapper cp4) {
        this.cp4.setFromOJB(this, "cp4", cp4);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.CompositePart> getCp4Iterator() {
        return cp4.iterator();
    }
    
    public pt.ist.fenixframework.example.oo7.domain.OO7Application getAppDocuments() {
        return this.appDocuments.get(this, "appDocuments");
    }
    
    public void setAppDocuments(pt.ist.fenixframework.example.oo7.domain.OO7Application appDocuments) {
        ApplicationHasDocuments.add((pt.ist.fenixframework.example.oo7.domain.Document)this, appDocuments);
    }
    
    public boolean hasAppDocuments() {
        return (getAppDocuments() != null);
    }
    
    public void removeAppDocuments() {
        setAppDocuments(null);
    }
    
    public void set$appDocuments(pt.ist.fenixframework.example.oo7.domain.OO7Application appDocuments) {
        this.appDocuments.setFromOJB(this, "appDocuments", appDocuments);
    }
    
    protected boolean checkDisconnected() {
        if (hasAnyCp4()) return false;
        if (hasAppDocuments()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        set$title(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TITLE"), txNumber);
        set$id(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "ID"), txNumber);
        set$text(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TEXT"), txNumber);
        set$keyAppDocuments(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_APP_DOCUMENTS"), txNumber);
    }
    
}
