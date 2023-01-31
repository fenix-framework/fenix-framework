package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {
    public static class MyDomainEntity extends DomainEntity {
        public MyDomainEntity(URL sourceFile, String fullName) {
            super(sourceFile, fullName);
        }

        @Override
        public void addRoleSlot(Role role) {
        }
    }

    private MyDomainEntity type;
    private Role role;
    private Role other;
    private DomainRelation relation;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        type = new MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.MyDomainEntity");
        role = new Role("TestRole", type);
        other = new Role("OtherRole", type);
        relation = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.MyDomainEntity",
                null, new ArrayList<>());
        relation.addRole(role);
        relation.addRole(other);
        role.setRelation(relation);
        other.setRelation(relation);
    }

    @Test
    public void getRelation() {
        assertEquals(relation, role.getRelation());
    }

    @Test
    public void isFirstRole() {
        assertFalse(role.isFirstRole());
    }

    @Test
    public void getOtherRole() {
        assertEquals(other, role.getOtherRole());
    }

    @Test
    public void getName() {
        assertEquals("TestRole", role.getName());
    }

    @Test
    public void getType() {
        assertEquals(type, role.getType());
    }

    @Test
    public void getMultiplicity() {
        assertEquals(0, role.getMultiplicityLower());
        assertEquals(1, role.getMultiplicityUpper());
        role.setMultiplicity(-1, -1);
        assertEquals(-1, role.getMultiplicityLower());
        assertEquals(-1, role.getMultiplicityUpper());
    }

    @Test
    public void getIndexProperty() {
        assertNull(role.getIndexProperty());
        role.setIndexProperty("Index");
        assertEquals("Index", role.getIndexProperty());
    }

    @Test
    public void getIndexCardinality() {
        assertEquals(0, role.getIndexCardinality());
        role.setIndexCardinality(1);
        assertEquals(1, role.getIndexCardinality());
    }

    @Test
    public void isIndexed() {
        assertFalse(role.isIndexed());
        role.setIndexProperty("Index");
        assertTrue(role.isIndexed());
    }

    @Test
    public void isOrdered() {
        assertFalse(role.isOrdered());
        role.setOrdered(true);
        assertTrue(role.isOrdered());
    }

    @Test
    public void needsMultiplicityChecks() {
        assertFalse(role.needsMultiplicityChecks());
        role.setMultiplicity(1, -1);
        assertTrue(role.needsMultiplicityChecks());
        role.setMultiplicity(-1, 1);
        assertFalse(role.needsMultiplicityChecks());
        role.setMultiplicity(-1, -1);
        assertFalse(role.needsMultiplicityChecks());
        role.setMultiplicity(1, 1);
        assertTrue(role.needsMultiplicityChecks());
        role.setMultiplicity(0, 2);
        assertTrue(role.needsMultiplicityChecks());
    }

    @Test
    public void isDirect() {
        assertFalse(role.isDirect());
    }
}
