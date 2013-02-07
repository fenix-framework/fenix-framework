package pt.ist.fenixframework;

import java.util.HashMap;
import java.util.Map;

import pt.ist.fenixframework.txintrospector.TxStats;

public abstract class FenixAbstractTransaction implements Transaction {

    /**
     * TxIntrospector associated with this transaction.
     */
    private final TxStats introspector = TxStats.newInstance();
    
    /*
     * nmld: Not sure if it is possible that two concurrent threads access this context?
     * That would be solved with a ConcurrentHashMap.
     */
    /**
     * Arbitrary context per transaction.
     */
    private final Map<String, Object> context = new HashMap<String, Object>();
    
    @Override
    public TxStats getTxIntrospector() {
	return introspector;
    }
    
    public Object putInContext(String key, Object value) {
	return this.context.put(key, value);
    }
    
    public Object getFromContext(String key) {
	return this.context.get(key);
    }
}
