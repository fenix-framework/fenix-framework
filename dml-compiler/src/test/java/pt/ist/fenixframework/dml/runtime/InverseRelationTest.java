package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InverseRelationTest {
    private InverseRelation<TestUtils.MyDomainObject, TestUtils.MyDomainObject> relation;
    private TestUtils.MyRelation other;

    private TestUtils.MyDomainObject o1 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o2 = new TestUtils.MyDomainObject();

    @BeforeEach
    public void beforeEach() {
        other = new TestUtils.MyRelation();
        relation = new InverseRelation<>(other, "MyRelation");
    }

    @Test
    public void getName() {
        assertEquals("MyRelation", relation.getName());
    }

    @Test
    public void getInverseRelation() {
        assertEquals(other, relation.getInverseRelation());
    }

    @Test
    public void add() {
        relation.add(o1, o2);
        assertTrue(other.addCalled);
        assertFalse(other.removeCalled);
    }

    @Test
    public void remove() {
        relation.remove(o1, o2);
        assertTrue(other.removeCalled);
        assertFalse(other.addCalled);
    }
}
