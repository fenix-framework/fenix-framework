package pt.ist.fenixframework.example.oo7.domain;

import pt.ist.fenixframework.pstm.VBox;
import pt.ist.fenixframework.pstm.RelationList;
import pt.ist.fenixframework.pstm.OJBFunctionalSetWrapper;
public abstract class Manual_Base extends pt.ist.fenixframework.pstm.AbstractDomainObject {
    public static dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module> role$$md1 = new dml.runtime.RoleMany<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module>() {
        public dml.runtime.RelationBaseSet<pt.ist.fenixframework.example.oo7.domain.Module> getSet(pt.ist.fenixframework.example.oo7.domain.Manual o1) {
            return ((Manual_Base)o1).md1;
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.Module,pt.ist.fenixframework.example.oo7.domain.Manual> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.Module.role$$manual;
        }
        
    };
    public static dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.OO7Application> role$$appManuals = new dml.runtime.RoleOneFenix<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.OO7Application>("appManuals") {
        public VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> getBox(pt.ist.fenixframework.example.oo7.domain.Manual o1) {
            return ((Manual_Base)o1).appManuals;
        }
        public void setFk(pt.ist.fenixframework.example.oo7.domain.Manual o1, java.lang.Integer newFk) {
            o1.setKeyAppManuals(newFk);
        }
        public dml.runtime.Role<pt.ist.fenixframework.example.oo7.domain.OO7Application,pt.ist.fenixframework.example.oo7.domain.Manual> getInverseRole() {
            return pt.ist.fenixframework.example.oo7.domain.OO7Application.role$$manuals;
        }
        
    };
    public static dml.runtime.Relation<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module> ModuleHasManual;
    public static dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.OO7Application> ApplicationHasManuals = new dml.runtime.DirectRelation<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.OO7Application>(role$$appManuals);
    static {
        pt.ist.fenixframework.example.oo7.domain.OO7Application.ApplicationHasManuals = ApplicationHasManuals.getInverseRelation();
    }
    
    private VBox<java.lang.String> title;
    private VBox<java.lang.Long> id;
    private VBox<java.lang.String> text;
    private VBox<java.lang.Long> textLength;
    private VBox<java.lang.Integer> keyAppManuals;
    private RelationList<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module> md1;
    private VBox<pt.ist.fenixframework.example.oo7.domain.OO7Application> appManuals;
    
    
    private void initInstance() {
        initInstance(true);
    }
    
    private void initInstance(boolean allocateOnly) {
        title = VBox.makeNew(allocateOnly, false);
        id = VBox.makeNew(allocateOnly, false);
        text = VBox.makeNew(allocateOnly, false);
        textLength = VBox.makeNew(allocateOnly, false);
        keyAppManuals = VBox.makeNew(allocateOnly, false);
        md1 = new RelationList<pt.ist.fenixframework.example.oo7.domain.Manual,pt.ist.fenixframework.example.oo7.domain.Module>((pt.ist.fenixframework.example.oo7.domain.Manual)this, ModuleHasManual, "md1", allocateOnly);
        appManuals = VBox.makeNew(allocateOnly, true);
    }
    
    {
        initInstance(false);
    }
    
    protected  Manual_Base() {
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
    
    public java.lang.Long getTextLength() {
        return this.textLength.get(this, "textLength");
    }
    
    public void setTextLength(java.lang.Long textLength) {
        this.textLength.put(this, "textLength", textLength);
    }
    
    private Object get$textLength() {
        java.lang.Long value = this.textLength.get(this, "textLength");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForLong(value);
    }
    
    private final void set$textLength(java.lang.Long arg0, int txNumber) {
        this.textLength.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public java.lang.Integer getKeyAppManuals() {
        return this.keyAppManuals.get(this, "keyAppManuals");
    }
    
    public void setKeyAppManuals(java.lang.Integer keyAppManuals) {
        this.keyAppManuals.put(this, "keyAppManuals", keyAppManuals);
    }
    
    private Object get$keyAppManuals() {
        java.lang.Integer value = this.keyAppManuals.get(this, "keyAppManuals");
        return (value == null) ? null : pt.ist.fenixframework.pstm.ToSqlConverter.getValueForInteger(value);
    }
    
    private final void set$keyAppManuals(java.lang.Integer arg0, int txNumber) {
        this.keyAppManuals.persistentLoad((arg0 == null) ? null : arg0, txNumber);
    }
    
    public int getMd1Count() {
        return this.md1.size();
    }
    
    public boolean hasAnyMd1() {
        return (! this.md1.isEmpty());
    }
    
    public boolean hasMd1(pt.ist.fenixframework.example.oo7.domain.Module md1) {
        return this.md1.contains(md1);
    }
    
    public java.util.Set<pt.ist.fenixframework.example.oo7.domain.Module> getMd1Set() {
        return this.md1;
    }
    
    public void addMd1(pt.ist.fenixframework.example.oo7.domain.Module md1) {
        ModuleHasManual.add((pt.ist.fenixframework.example.oo7.domain.Manual)this, md1);
    }
    
    public void removeMd1(pt.ist.fenixframework.example.oo7.domain.Module md1) {
        ModuleHasManual.remove((pt.ist.fenixframework.example.oo7.domain.Manual)this, md1);
    }
    
    public java.util.List<pt.ist.fenixframework.example.oo7.domain.Module> getMd1() {
        return md1;
    }
    
    public void set$md1(OJBFunctionalSetWrapper md1) {
        this.md1.setFromOJB(this, "md1", md1);
    }
    
    public java.util.Iterator<pt.ist.fenixframework.example.oo7.domain.Module> getMd1Iterator() {
        return md1.iterator();
    }
    
    public pt.ist.fenixframework.example.oo7.domain.OO7Application getAppManuals() {
        return this.appManuals.get(this, "appManuals");
    }
    
    public void setAppManuals(pt.ist.fenixframework.example.oo7.domain.OO7Application appManuals) {
        ApplicationHasManuals.add((pt.ist.fenixframework.example.oo7.domain.Manual)this, appManuals);
    }
    
    public boolean hasAppManuals() {
        return (getAppManuals() != null);
    }
    
    public void removeAppManuals() {
        setAppManuals(null);
    }
    
    public void set$appManuals(pt.ist.fenixframework.example.oo7.domain.OO7Application appManuals) {
        this.appManuals.setFromOJB(this, "appManuals", appManuals);
    }
    
    protected boolean checkDisconnected() {
        if (hasAnyMd1()) return false;
        if (hasAppManuals()) return false;
        return true;
        
    }
    
    protected void readSlotsFromResultSet(java.sql.ResultSet rs, int txNumber) throws java.sql.SQLException {
        set$title(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TITLE"), txNumber);
        set$id(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "ID"), txNumber);
        set$text(pt.ist.fenixframework.pstm.ResultSetReader.readString(rs, "TEXT"), txNumber);
        set$textLength(pt.ist.fenixframework.pstm.ResultSetReader.readLong(rs, "TEXT_LENGTH"), txNumber);
        set$keyAppManuals(pt.ist.fenixframework.pstm.ResultSetReader.readInteger(rs, "KEY_APP_MANUALS"), txNumber);
    }
    
}
