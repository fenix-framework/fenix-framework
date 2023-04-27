package pt.ist.fenixframework.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.core.exception.SpecifiedDmlFileNotFoundException;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class DmlFileTest {

    private static DmlFile f1;
    private static DmlFile f2;
    private static DmlFile f3;

    @BeforeAll
    public static void beforeAll() {
        f1 = new DmlFile(getResource("TestResource1.dml"), "test1");
        f2 = new DmlFile(getResource("TestResource2.dml"), "test1");
        f3 = new DmlFile(getResource("TestResource1.dml"), null);
    }

    @Test
    public void equalsTest() {
        assertTrue(f1.equals(f3));
        assertFalse(f1.equals(f2));
        assertFalse(f2.equals(f3));
        assertFalse(f1.equals(new Object()));
    }

    @Test
    public void toStringTest() {
        assertEquals(f1.toString(), "test1");
        assertTrue(f3.toString().contains("/"));
    }

    @Test
    public void URLTest() {
        assertEquals(f1.getUrl(), getResource("TestResource1.dml"));
    }

    @Test
    public void hashCodeTest() {
        assertEquals(f1.hashCode(), getResource("TestResource1.dml").hashCode());
    }

    @Test
    public void parseDependencyTest() throws SpecifiedDmlFileNotFoundException {
        assertEquals(DmlFile.parseDependencyDmlFiles("TestResource1.dml").get(0).getUrl(), getResource("TestResource1.dml"));
    }

    @Test
    public void parseDependencyExceptionTest() {
        assertThrows(SpecifiedDmlFileNotFoundException.class, () -> DmlFile.parseDependencyDmlFiles("blahblahblah.dml"));
    }

    // Note: i know there is a resource to url function but i want to keep package's tests separated
    static private URL getResource(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }
}
