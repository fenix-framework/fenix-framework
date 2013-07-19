package pt.ist.fenixframework.core;

import java.io.Serializable;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class JsonElementWrapper implements Serializable {

    private static final long serialVersionUID = 8938180404801436209L;

    private final String json;

    public JsonElementWrapper(JsonElement element) {
        this.json = element.toString();
    }

    private static final JsonParser PARSER = new JsonParser();

    protected Object readResolve() {
        return PARSER.parse(this.json);
    }
}
