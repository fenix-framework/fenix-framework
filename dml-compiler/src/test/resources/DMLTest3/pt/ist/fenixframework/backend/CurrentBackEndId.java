package pt.ist.fenixframework.backend;

@SuppressWarnings("all")
public class CurrentBackEndId extends BackEndId {
    
    public String getBackEndName() {
        return "N/A";
    }
    
    public Class<? extends pt.ist.fenixframework.Config> getDefaultConfigClass() {
        try {
            return (Class<? extends pt.ist.fenixframework.Config>)Class.forName("N/A");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public Class<? extends pt.ist.fenixframework.core.AbstractDomainObject> getDomainClassRoot() {
        return pt.ist.fenixframework.core.AbstractDomainObject.class;
    }
    
    public String getAppName() {
        return null;
    }
    
}
