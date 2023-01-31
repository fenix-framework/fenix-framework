package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.core.AbstractDomainObject;

import java.io.Serializable;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RelationAwareSetTest {
    private static final class DummyDomainObject extends AbstractDomainObject {
        private String id;

        public DummyDomainObject() {
            byte[] array = new byte[7]; // length is bounded by 7
            new Random().nextBytes(array);
            id = Base64.getEncoder().encodeToString(array);
        }

        @Override
        public String getExternalId() {
            return "External" + id;
        }

        @Override
        public Comparable getOid() {
            return "Oid" + id;
        }

        @Override
        protected void ensureOid() {
        }
    }

    private static final class MyDomainBasedMap implements DomainBasedMap<DummyDomainObject> {
        private Map<String, DummyDomainObject> map = new TreeMap<>();

        @Override
        public String getExternalId() {
            return "ExternalId";
        }

        @Override
        public DummyDomainObject get(Comparable key) {
            return map.get((String) key);
        }

        @Override
        public boolean putIfMissing(Comparable key, DummyDomainObject value) {
            return map.putIfAbsent((String) key, value) != null;
        }

        @Override
        public void put(Comparable key, DummyDomainObject value) {
            map.put((String) key, value);
        }

        @Override
        public boolean remove(Comparable key) {
            return map.remove((String) key) != null;
        }

        @Override
        public boolean contains(Comparable key) {
            return map.containsKey((String) key);
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public Iterator<DummyDomainObject> iterator() {
            return map.values().iterator();
        }
    }

    public static class MyRelation implements Relation<DummyDomainObject, DummyDomainObject> {
        public boolean removeCalled = false;
        public boolean addCalled = false;

        @Override
        public boolean add(DummyDomainObject o1, DummyDomainObject o2) {
            addCalled = true;
            return false;
        }

        @Override
        public boolean remove(DummyDomainObject o1, DummyDomainObject o2) {
            removeCalled = true;
            return false;
        }

        @Override
        public Relation<DummyDomainObject, DummyDomainObject> getInverseRelation() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    private static class MyKeyFunction implements KeyFunction<String, DummyDomainObject> {
        @Override
        public String getKey(DummyDomainObject obj) {
            return obj.toString();
        }

        @Override
        public boolean allowMultipleKeys() {
            return false;
        }
    }

    private RelationAwareSet<DummyDomainObject, DummyDomainObject> relationSet;
    private DummyDomainObject owner;
    private DummyDomainObject o1;
    private DummyDomainObject o2;
    private MyRelation relation;
    private MyKeyFunction keyFunction;
    private MyDomainBasedMap domainBasedMap;

    @BeforeEach
    public void beforeEach() {
        owner = new DummyDomainObject();
        o1 = new DummyDomainObject();
        o2 = new DummyDomainObject();
        relation = new MyRelation();
        keyFunction = new MyKeyFunction();
        domainBasedMap = new MyDomainBasedMap();
        relationSet = new RelationAwareSet<>(owner, relation, domainBasedMap, keyFunction);
    }

    @Test
    public void justAdd() {
        relationSet.justAdd(o1);
        assertTrue(domainBasedMap.contains(keyFunction.getKey(o1)));
        assertFalse(domainBasedMap.contains(keyFunction.getKey(o2)));
    }

    @Test
    public void add() {
        relationSet.add(o1);
        assertTrue(relation.addCalled);
    }

    @Test
    public void justRemove() {
        relationSet.justAdd(o1);
        relationSet.justRemove(o1);
        assertFalse(domainBasedMap.contains(keyFunction.getKey(o1)));
        assertFalse(domainBasedMap.contains(keyFunction.getKey(o2)));
    }

    @Test
    public void remove() {
        relationSet.add(o1);
        relationSet.remove("invalid");
        assertFalse(relation.removeCalled);
        relationSet.remove(o1);
        assertTrue(relation.removeCalled);
    }

    @Test
    public void size() {
        assertEquals(0, relationSet.size());
        relationSet.justAdd(o1);
        assertEquals(1, relationSet.size());
        relationSet.justAdd(o2);
        assertEquals(2, relationSet.size());
        relationSet.justRemove(o2);
        assertEquals(1, relationSet.size());
        relationSet.justRemove(o1);
        assertEquals(0, relationSet.size());
    }

    @Test
    public void get() {
        relationSet.justAdd(o1);
        assertEquals(o1, relationSet.get(keyFunction.getKey(o1)));
        assertNull(relationSet.get("invalid"));
    }

    @Test
    public void contains() {
        relationSet.justAdd(o1);
        assertTrue(relationSet.contains(o1));
        assertFalse(relationSet.contains(o2));
        assertFalse(relationSet.contains(new Object()));
    }

    @Test
    public void iteratorNext() {
        relationSet.justAdd(o1);
        relationSet.justAdd(o2);
        // Iterators do not maintain order
        List<DummyDomainObject> res = new ArrayList<>();
        Iterator<DummyDomainObject> iter = relationSet.iterator();
        assertTrue(iter.hasNext());
        res.add(iter.next());
        assertTrue(iter.hasNext());
        res.add(iter.next());
        assertFalse(iter.hasNext());
        assertTrue(res.contains(o1));
        assertTrue(res.contains(o2));
    }

    @Test
    public void iteratorRemove() {
        relationSet.justAdd(o1);
        relationSet.justAdd(o2);
        Iterator<DummyDomainObject> iter = relationSet.iterator();
        assertThrows(IllegalStateException.class, iter::remove);
        iter.next();
        iter.remove();
        assertTrue(relation.removeCalled);
        assertThrows(IllegalStateException.class, iter::remove);
    }
}
