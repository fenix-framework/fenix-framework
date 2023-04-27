package pt.ist.fenixframework;

import java.util.HashMap;
import java.util.Map;

public abstract class FenixAbstractTransaction implements Transaction {

    /**
     * Arbitrary context per transaction.
     */
    private Map<String, Object> context = null;

    @Override
    public void putInContext(String key, Object value) {
        if (context == null) {
            this.context = new HashMap<String, Object>();
        }
        this.context.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getFromContext(String key) {
        if (this.context == null) {
            return null;
        }

        return (T) this.context.get(key);
    }
}
