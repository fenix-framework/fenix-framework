package pt.ist.fenixframework.backend.jvstmojb.ojb;

import java.util.Properties;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.cache.ObjectCache;

public class FenixCacheWrapper implements ObjectCache {

    public FenixCacheWrapper(PersistenceBroker arg0, Properties props) {
    }

    @Override
    public void cache(Identity oid, Object obj) {
        // do nothing, because the cache is already managed by the fenix framework code
    }

    @Override
    public Object lookup(Identity oid) {
        // do nothing, because the cache is already managed by the fenix framework code
        return null;
    }

    @Override
    public void remove(Identity oid) {
        // do nothing, because the cache is already managed by the fenix framework code
    }

    @Override
    public void clear() {
        // do nothing, because the cache is already managed by the fenix framework code
    }
}
