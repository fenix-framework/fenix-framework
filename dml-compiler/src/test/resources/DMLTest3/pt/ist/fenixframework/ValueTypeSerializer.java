package pt.ist.fenixframework;

import com.google.gson.JsonObject;
import pt.ist.fenixframework.util.JsonConverter;

@SuppressWarnings("all")
public final class ValueTypeSerializer {
    
    // VT: TestExtendsBar serializes as java.lang.String
    public static final java.lang.String serialize$TestExtendsBar(bar.TestExtends obj) {
        return (obj == null) ? null : (java.lang.String)obj.toString();
    }
    public static final bar.TestExtends deSerialize$TestExtendsBar(java.lang.String obj) {
        return (obj == null) ? null : (bar.TestExtends)new bar.TestExtends(obj);
    }
    
    // VT: TestBar serializes as java.lang.String
    public static final java.lang.String serialize$TestBar(bar.Test obj) {
        return (obj == null) ? null : (java.lang.String)obj.toString();
    }
    public static final bar.Test deSerialize$TestBar(java.lang.String obj) {
        return (obj == null) ? null : (bar.Test)new bar.Test(obj);
    }
    
}
