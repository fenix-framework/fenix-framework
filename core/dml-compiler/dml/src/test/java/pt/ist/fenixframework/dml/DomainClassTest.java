package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

public class DomainClassTest {

    private DomainClass parentClass;
    private DomainClass childClass;
    private DomainClass otherClass;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        parentClass = new DomainClass(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.ParentClass", null, null);
        childClass =
                new DomainClass(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.ChildClass", parentClass, null);
        otherClass = new DomainClass(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.dml.test.OtherClass", null, null);
    }

    @Test
    public void hasSuperclassParentClass() throws MalformedURLException {
        assertFalse(parentClass.hasSuperclass());
    }

    @Test
    public void hasSuperclassChieldClass() throws MalformedURLException {
        assertTrue(childClass.hasSuperclass());
    }

    @Test
    public void getSuperclass() throws MalformedURLException {
        assertEquals(parentClass, childClass.getSuperclass());
    }

    @Test
    public void getSuperclassNameChield() throws MalformedURLException {
        assertEquals("ParentClass", childClass.getSuperclassName());
    }

    @Test
    public void getSuperclassNameParent() throws MalformedURLException {
        assertNull(parentClass.getSuperclassName());
    }

    @Test
    public void getInterfaceNamesIterator() throws MalformedURLException {
        assertFalse(childClass.getInterfaceNamesIterator().hasNext());
    }

    @Test
    public void getInterfacesNames() throws MalformedURLException {
        assertEquals(0, childClass.getInterfacesNames().size());
    }

    @Test
    public void slots() throws MalformedURLException {
        Slot slotWithOption = new Slot("name", new PlainValueType("String"));
        slotWithOption.addOption(Slot.Option.REQUIRED);
        childClass.addSlot(slotWithOption);
        childClass.addSlot(new Slot("date", new PlainValueType("String")));
        parentClass.addSlot(new Slot("version", new PlainValueType("String")));
        assertTrue(childClass.hasSlots());
        assertTrue(childClass.getSlots().hasNext());
        assertEquals("name", childClass.findSlot("name").getName());
        assertEquals("version", childClass.findSlot("version").getName());
        assertNull(childClass.findSlot(null));
        assertEquals(2, childClass.getSlotsList().size());
    }

    @Test
    public void roles() throws MalformedURLException {
        Role role = new Role("other", otherClass);
        childClass.addRoleSlot(role);
        assertTrue(childClass.getRoleSlots().hasNext());
        assertEquals(role, childClass.findRoleSlot("other"));
    }

    @Test
    public void hasSlotWithOption() throws MalformedURLException {
        assertFalse(parentClass.hasSlotWithOption(Slot.Option.REQUIRED));
        Slot slotWithOption = new Slot("name", new PlainValueType("String"));
        slotWithOption.addOption(Slot.Option.REQUIRED);
        childClass.addSlot(slotWithOption);
        assertTrue(childClass.hasSlotWithOption(Slot.Option.REQUIRED));
    }
}
