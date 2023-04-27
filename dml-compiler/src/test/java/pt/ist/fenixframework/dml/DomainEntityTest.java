package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class DomainEntityTest {
    public static class MyDomainEntity extends DomainEntity {
        public MyDomainEntity(URL sourceFile, String fullName) {
            super(sourceFile, fullName);
        }

        @Override
        public void addRoleSlot(Role role) {
        }
    }

    private MyDomainEntity myDomainEntity;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        myDomainEntity =
                new MyDomainEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(), "pt.ist.fenixframework.MyDomainEntity");
    }

    @Test
    public void getSourceFile() throws MalformedURLException {
        assertEquals(new File("/tmp/a-dml-file.dml").toURI().toURL(), myDomainEntity.getSourceFile());
    }

    @Test
    public void getFullName() {
        assertEquals("pt.ist.fenixframework.MyDomainEntity", myDomainEntity.getFullName());
    }

    @Test
    public void getFullNameWithPrefix() {
        assertEquals("test.pt.ist.fenixframework.MyDomainEntity", myDomainEntity.getFullName("test"));
    }

    @Test
    public void getPackageName() {
        assertEquals("pt.ist.fenixframework", myDomainEntity.getPackageName());
    }

    @Test
    public void getName() {
        assertEquals("MyDomainEntity", myDomainEntity.getName());
    }

    @Test
    public void getBaseName() {
        assertEquals("MyDomainEntity_Base", myDomainEntity.getBaseName());
    }

    @Test
    public void findSlot() {
        assertNull(myDomainEntity.findSlot("Slot"));
    }

    @Test
    public void findRoleSlot() {
        assertNull(myDomainEntity.findRoleSlot("RoleSlot"));
    }
}
