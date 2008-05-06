package pt.ist.fenixframework.pstm;

import java.util.Properties;

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.cache.ObjectCache;

public class FenixCacheWrapper implements ObjectCache {

    private FenixCache delegate = Transaction.getCache();

    public FenixCacheWrapper(PersistenceBroker arg0, Properties props) {
    }


    public void cache(Identity oid, Object obj) {
	delegate.cache(oid, obj);
    }


    public Object lookup(Identity oid) {
	return delegate.lookup(oid);
    }


    public void remove(Identity oid) {
	delegate.remove(oid);
    }

    public void clear() {
	delegate.clear();
    }

    public static int getNumberOfCachedItems() {
        return FenixCache.getNumberOfCachedItems();
    }
}
