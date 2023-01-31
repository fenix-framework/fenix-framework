package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleOneTest {
    private static class MyRoleOne extends RoleOne<TestUtils.MyDomainObject, TestUtils.MyDomainObject> {
        private TestUtils.MyDomainObject o1 = null;
        private TestUtils.MyDomainObject o2 = null;

        public MyRoleOne(TestUtils.MyDomainObject o1, TestUtils.MyDomainObject o2) {
            this.o1 = o1;
            this.o2 = o2;
        }

        @Override
        public Role<TestUtils.MyDomainObject, TestUtils.MyDomainObject> getInverseRole() {
            return null;
        }

        @Override
        public TestUtils.MyDomainObject getValue(TestUtils.MyDomainObject o1) {
            return o2;
        }

        @Override
        public void setValue(TestUtils.MyDomainObject o1, TestUtils.MyDomainObject o2) {
            this.o1 = o1;
            this.o2 = o2;
        }
    }

    private MyRoleOne role;
    private TestUtils.MyDomainObject o1 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o2 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o3 = new TestUtils.MyDomainObject();

    @BeforeEach
    public void beforeEach() {
        role = new MyRoleOne(o1, o2);
    }

    @Test
    public void add() {
        assertTrue(role.add(o1, o2, new TestUtils.MyRelation()));
        assertEquals(o2, role.getValue(o1));
        assertTrue(role.add(o1, o3, new TestUtils.MyRelation()));
        assertEquals(o3, role.getValue(o1));
        assertTrue(role.add(null, o2, new TestUtils.MyRelation()));
        assertEquals(o3, role.getValue(o1));
    }

    @Test
    public void remove() {
        assertTrue(role.remove(o1, o2));
        assertNull(role.getValue(o1));
        role.add(o1, o2, new TestUtils.MyRelation());
        assertTrue(role.remove(null, o2));
        assertEquals(o2, role.getValue(o1));
    }
}
