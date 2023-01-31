package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoleEmptyTest {
    private RoleEmpty<TestUtils.MyDomainObject, TestUtils.MyDomainObject> role;
    private final TestUtils.MyRole inverse = new TestUtils.MyRole();
    private final TestUtils.MyDomainObject do1 = new TestUtils.MyDomainObject();
    private final TestUtils.MyDomainObject do2 = new TestUtils.MyDomainObject();

    @BeforeEach
    public void beforeEach() {
        role = new RoleEmpty<>(inverse);
    }

    @Test
    public void add() {
        assertTrue(role.add(do1, do2, new TestUtils.MyRelation()));
    }

    @Test
    public void remove() {
        assertTrue(role.remove(do1, do2));
    }

    @Test
    public void getInverseRole() {
        assertEquals(inverse, role.getInverseRole());
    }
}
