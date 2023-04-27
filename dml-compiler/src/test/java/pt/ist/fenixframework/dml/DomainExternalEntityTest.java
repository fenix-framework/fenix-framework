package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;

public class DomainExternalEntityTest {
    private DomainExternalEntity domainExternalEntity;

    @BeforeEach
    public void beforeEach() throws MalformedURLException {
        domainExternalEntity = new DomainExternalEntity(new File("/tmp/a-dml-file.dml").toURI().toURL(),
                "pt.ist.fenixframework.DomainExternalEntity");
    }

    @Test
    public void addRoleSlot() {
        domainExternalEntity.addRoleSlot(new Role("Temp", domainExternalEntity));
        assertNull(domainExternalEntity.findRoleSlot("Temp"));
    }

    @Test
    public void getFullName() {
        assertEquals("pt.ist.fenixframework.DomainExternalEntity", domainExternalEntity.getFullName("test"));
    }
}
