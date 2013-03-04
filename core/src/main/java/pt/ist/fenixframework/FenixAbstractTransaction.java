package pt.ist.fenixframework;

import java.util.HashMap;
import java.util.Map;

public abstract class FenixAbstractTransaction implements Transaction {

    /**
     * Arbitrary context per transaction.
     */
    private final Map<String, Object> context = new HashMap<String, Object>();

    public Object putInContext(String key, Object value) {
        return this.context.put(key, value);
    }

    public Object getFromContext(String key) {
        return this.context.get(key);
    }
}
