package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class RoleManyTest {
    protected static class MyRelationBaseSet implements RelationBaseSet<TestUtils.MyDomainObject> {
        private Set<TestUtils.MyDomainObject> set = new TreeSet<>();

        @Override
        public boolean justAdd(TestUtils.MyDomainObject elem) {
            return set.add(elem);
        }

        @Override
        public boolean justRemove(TestUtils.MyDomainObject elem) {
            return set.remove(elem);
        }

        public boolean contains(TestUtils.MyDomainObject elem) {
            return set.contains(elem);
        }
    }

    private static class MyRoleMany extends RoleMany<TestUtils.MyDomainObject, TestUtils.MyDomainObject> {
        private TestUtils.MyDomainObject o1 = null;
        private TestUtils.MyDomainObject o2 = null;

        private MyRelationBaseSet set;

        public MyRoleMany(TestUtils.MyDomainObject o1, TestUtils.MyDomainObject o2, MyRelationBaseSet set) {
            this.o1 = o1;
            this.o2 = o2;
            this.set = set;
        }

        @Override
        public Role<TestUtils.MyDomainObject, TestUtils.MyDomainObject> getInverseRole() {
            return null;
        }

        @Override
        public RelationBaseSet<TestUtils.MyDomainObject> getSet(TestUtils.MyDomainObject o1) {
            return set;
        }
    }

    private MyRoleMany role;
    private MyRelationBaseSet set;
    private TestUtils.MyDomainObject o1 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o2 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o3 = new TestUtils.MyDomainObject();

    @BeforeEach
    public void beforeEach() {
        set = new MyRelationBaseSet();
        role = new MyRoleMany(o1, o2, set);
    }

    @Test
    public void add() {
        role.add(o1, o2, new TestUtils.MyRelation());
        assertTrue(((MyRelationBaseSet) role.getSet(o1)).contains(o2));

        role.add(null, null, new TestUtils.MyRelation());
        assertFalse(((MyRelationBaseSet) role.getSet(o1)).contains(o3));

        role.add(o1, null, new TestUtils.MyRelation());
        assertFalse(((MyRelationBaseSet) role.getSet(o1)).contains(o3));

        role.add(null, o3, new TestUtils.MyRelation());
        assertFalse(((MyRelationBaseSet) role.getSet(o1)).contains(o3));

        role.add(o1, o3, new TestUtils.MyRelation());
        assertTrue(((MyRelationBaseSet) role.getSet(o1)).contains(o3));
    }

    @Test
    public void remove() {
        role.add(o1, o2, new TestUtils.MyRelation());
        role.remove(null, o2);
        assertTrue(((MyRelationBaseSet) role.getSet(o1)).contains(o2));
        role.remove(o1, o2);
        assertFalse(((MyRelationBaseSet) role.getSet(o1)).contains(o2));
        role.remove(o1, o3);
        assertFalse(((MyRelationBaseSet) role.getSet(o1)).contains(o3));
    }
}
