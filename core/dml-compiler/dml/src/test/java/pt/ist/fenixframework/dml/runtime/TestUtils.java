package pt.ist.fenixframework.dml.runtime;

import pt.ist.fenixframework.DomainObject;

import java.util.Base64;
import java.util.Random;

public class TestUtils {
    public static class MyDomainObject implements DomainObject, Comparable<MyDomainObject> {
        private String id;

        public MyDomainObject() {
            byte[] array = new byte[7]; // length is bounded by 7
            new Random().nextBytes(array);
            id = Base64.getEncoder().encodeToString(array);
        }

        @Override
        public String getExternalId() {
            return id;
        }

        @Override
        public int compareTo(MyDomainObject o) {
            return id.compareTo(o.id);
        }
    }

    public static class MyRole implements Role<MyDomainObject, MyDomainObject> {
        @Override
        public boolean add(MyDomainObject o1, MyDomainObject o2, Relation<MyDomainObject, MyDomainObject> rel) {
            return false;
        }

        @Override
        public boolean remove(MyDomainObject o1, MyDomainObject o2) {
            return false;
        }

        @Override
        public Role<MyDomainObject, MyDomainObject> getInverseRole() {
            return new MyRole();
        }
    }

    public static class MyRelation implements Relation<MyDomainObject, MyDomainObject> {
        public boolean addCalled = false;
        public boolean removeCalled = false;

        @Override
        public boolean add(MyDomainObject o1, MyDomainObject o2) {
            addCalled = true;
            return false;
        }

        @Override
        public boolean remove(MyDomainObject o1, MyDomainObject o2) {
            removeCalled = true;
            return false;
        }

        @Override
        public Relation<MyDomainObject, MyDomainObject> getInverseRelation() {
            return null;
        }

        @Override
        public String getName() {
            return "TestRelation";
        }
    }

    public static class MyListener implements RelationListener<MyDomainObject, MyDomainObject> {
        public boolean beforeAddCalled = false;
        public boolean afterAddCalled = false;
        public boolean beforeRemoveCalled = false;
        public boolean afterRemoveCalled = false;

        @Override
        public void beforeAdd(Relation<MyDomainObject, MyDomainObject> rel, MyDomainObject o1, MyDomainObject o2) {
            beforeAddCalled = true;
        }

        @Override
        public void afterAdd(Relation<MyDomainObject, MyDomainObject> rel, MyDomainObject o1, MyDomainObject o2) {
            afterAddCalled = true;
        }

        @Override
        public void beforeRemove(Relation<MyDomainObject, MyDomainObject> rel, MyDomainObject o1, MyDomainObject o2) {
            beforeRemoveCalled = true;
        }

        @Override
        public void afterRemove(Relation<MyDomainObject, MyDomainObject> rel, MyDomainObject o1, MyDomainObject o2) {
            afterRemoveCalled = true;
        }
    }
}
