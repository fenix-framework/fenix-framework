package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.SharedIdentityMap;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by diutsu on 30/11/16.
 */
public class InMemDomainObject extends JVSTMDomainObject {

    @Override
    protected void deleteDomainObject() {
        this.invokeDeletionListeners();
        try {
            Field cacheField = SharedIdentityMap.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            ConcurrentHashMap<Object, ?> cache = (ConcurrentHashMap<Object, ?>) cacheField.get(SharedIdentityMap.getCache());
            cache.remove(this.getOid());
            ((InMemDomainObjectValidBackEnd) FenixFramework.getConfig().getBackEnd()).deleteObject(this.getOid());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
