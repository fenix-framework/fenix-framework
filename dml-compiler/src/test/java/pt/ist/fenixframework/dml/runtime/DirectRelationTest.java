package pt.ist.fenixframework.dml.runtime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DirectRelationTest {
    private DirectRelation<TestUtils.MyDomainObject, TestUtils.MyDomainObject> relation;
    private TestUtils.MyRole role;
    private TestUtils.MyDomainObject o1 = new TestUtils.MyDomainObject();
    private TestUtils.MyDomainObject o2 = new TestUtils.MyDomainObject();
    private TestUtils.MyListener listener;

    @BeforeEach
    public void beforeEach() {
        listener = new TestUtils.MyListener();
        role = new TestUtils.MyRole();
        relation = new DirectRelation<>(role, "MyDirectRelation");
    }

    @Test
    public void getName() {
        assertEquals("MyDirectRelation", relation.getName());
    }

    @Test
    public void getInverseRelation() {
        assertNotNull(relation.getInverseRelation());
    }

    @Test
    public void getListeners() {
        assertNotNull(relation.getListeners());
    }

    @Test
    public void add() {
        // this is false because of the MyRole implementation
        assertFalse(relation.add(o1, o2));
    }

    @Test
    public void remove() {
        // this is false because of the MyRole implementation
        assertFalse(relation.add(o1, o2));
    }

    @Test
    public void addListener() {
        relation.addListener(listener);
        assertTrue(relation.getListeners().contains(listener));
    }

    @Test
    public void removeListener() {
        relation.addListener(listener);
        relation.removeListener(listener);
        assertFalse(relation.getListeners().contains(listener));
    }

    @Test
    public void addCallsListener() {
        relation.addListener(listener);
        relation.add(o1, o2);
        assertTrue(listener.afterAddCalled);
        assertTrue(listener.beforeAddCalled);
        assertFalse(listener.afterRemoveCalled);
        assertFalse(listener.beforeRemoveCalled);
    }

    @Test
    public void removeCallsListener() {
        relation.addListener(listener);
        relation.remove(o1, o2);
        assertFalse(listener.afterAddCalled);
        assertFalse(listener.beforeAddCalled);
        assertTrue(listener.afterRemoveCalled);
        assertTrue(listener.beforeRemoveCalled);
    }
}
