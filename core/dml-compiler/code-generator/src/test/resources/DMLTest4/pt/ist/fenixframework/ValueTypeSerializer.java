package pt.ist.fenixframework;

import com.google.gson.JsonObject;
import pt.ist.fenixframework.util.JsonConverter;

@SuppressWarnings("all")
public final class ValueTypeSerializer {
    
    // VT: Locale serializes as com.google.gson.JsonElement
    public static final com.google.gson.JsonElement serialize$Locale(java.util.Locale obj) {
        if (obj == null) return null;
        JsonObject json = new JsonObject();
        json.add("toLanguageTag", JsonConverter.getJsonFor((java.lang.String)obj.toLanguageTag()));
        json.add("toJson", JsonConverter.getJsonFor((com.google.gson.JsonElement)obj.toJson()));
        return json;
    }
    public static final java.util.Locale deSerialize$Locale(com.google.gson.JsonElement obj) {
        return (obj == null) ? null : (java.util.Locale)java.util.Locale.forLanguageTag(
        JsonConverter.getStringFromJson(obj.getAsJsonObject().get("toLanguageTag")),
        JsonConverter.getJsonElementFromJson(obj.getAsJsonObject().get("toJson")));
    }
    
}
