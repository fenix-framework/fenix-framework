package pt.ist.fenixframework.test.core;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.core.SharedIdentityMap;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.test.Classes.MyDomainObject;

import static org.junit.jupiter.api.Assertions.*;

public class SharedIdentityMapTest {

    @Test
    public void getCacheTest() {
        assertNotNull(SharedIdentityMap.getCache());
    }

    @Test
    public void constructorTest() {
        assertNotNull(new SharedIdentityMap());
    }

    @Test
    public void sizeTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        assertEquals(0, cache.size());
    }

    @Test
    public void cacheTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        MyDomainObject obj = new MyDomainObject(model);
        assertEquals(obj, cache.cache(obj));
    }

    @Test
    public void cacheRepeatedTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        MyDomainObject obj = new MyDomainObject(model);
        cache.cache(obj);
        assertEquals(obj, cache.cache(obj));
    }

    @Test
    public void cacheDifferentTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model1 = new DomainModel();
        model1.finalizeDomain();
        DomainModel model2 = new DomainModel();
        model2.finalizeDomain();
        MyDomainObject obj1 = new MyDomainObject(model1);
        MyDomainObject obj2 = new MyDomainObject(model2);
        assertEquals(obj1, cache.cache(obj1));
        assertEquals(obj2, cache.cache(obj2));
    }

    @Test
    public void lookupExistingTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        MyDomainObject obj = new MyDomainObject(model);
        cache.cache(obj);
        assertEquals(obj, cache.lookup(obj.getOid()));
    }

    @Test
    public void lookupNonExistingTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        assertNull(cache.lookup(new Object()));
    }

    @Test
    public void printCacheTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        MyDomainObject obj = new MyDomainObject(model);
        cache.cache(obj);
        cache.printCachedObjects();
    }

    @Test
    public void shutdownTest() {
        SharedIdentityMap cache = new SharedIdentityMap();
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        MyDomainObject obj = new MyDomainObject(model);
        cache.cache(obj);
        cache.shutdown();
        assertEquals(0, cache.size());
    }
}
