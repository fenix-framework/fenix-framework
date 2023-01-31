package pt.ist.fenixframework;

@SuppressWarnings("all")
public final class ValueTypeSerializer {
    
    // VT: ModuleData serializes as com.google.gson.JsonElement
    public static final com.google.gson.JsonElement serialize$ModuleData(pt.ist.fenixframework.data.ModuleData obj) {
        return (obj == null) ? null : (com.google.gson.JsonElement)obj.json();
    }
    public static final pt.ist.fenixframework.data.ModuleData deSerialize$ModuleData(com.google.gson.JsonElement obj) {
        return (obj == null) ? null : (pt.ist.fenixframework.data.ModuleData)new pt.ist.fenixframework.data.ModuleData(obj);
    }
    
}
