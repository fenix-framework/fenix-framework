package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DomainRelationTest {
    public static class MyDomainEntity extends DomainEntity {
        public MyDomainEntity(URL sourceFile, String fullName) {
            super(sourceFile, fullName);
        }

        @Override
        public void addRoleSlot(Role role) {
        }
    }

    private DomainRelation relation;
    private DomainRelation other;
    private Role role;
    private Role role2;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        relation = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.DomainRelation",
                null, new ArrayList<>());
        other = new DomainRelation(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.OtherDomainRelation",
                relation, new ArrayList<>());
        MyDomainEntity type =
                new MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.MyDomainEntity");
        role = new Role("TestRole", type);
        role2 = new Role("TestRole2", type);
    }

    @Test
    public void getSuperrelation() {
        assertNull(relation.getSuperrelation());
        assertEquals(relation, other.getSuperrelation());
    }

    @Test
    public void countRoles() {
        assertEquals(0, relation.countRoles());
        assertEquals(0, other.countRoles());
        relation.addRole(role);
        other.addRole(role);
        assertEquals(1, relation.countRoles());
        assertEquals(2, other.countRoles());
    }

    @Test
    public void getRoles() {
        assertEquals(0, relation.getRoles().size());
        relation.addRole(role);
        relation.addRole(role2);
        assertEquals(2, relation.getRoles().size());
    }

    @Test
    public void getFirstRole() {
        relation.addRole(role);
        relation.addRole(role2);
        assertEquals(role, relation.getFirstRole());
    }

    @Test
    public void getSecondRole() {
        relation.addRole(role);
        relation.addRole(role2);
        assertEquals(role2, relation.getSecondRole());
    }

}
