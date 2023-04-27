package pt.ist.fenixframework.test.backend;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.backend.OID;
import pt.ist.fenixframework.core.exception.MissingObjectException;
import pt.ist.fenixframework.util.Misc;

import static org.junit.jupiter.api.Assertions.*;

public class OIDTest {
    @Test
    public void makeNewTest() {
        OID oid = OID.makeNew(Misc.class);
        assertEquals(Misc.class, oid.getObjClass());
    }

    @Test
    public void makeNewDomainRootTest() {
        OID oid = OID.makeNew(DomainRoot.class);
        assertEquals(OID.ROOT_OBJECT_ID, oid);
    }

    @Test
    public void constructorTest() {
        OID oid = new OID("pt.ist.fenixframework.util.Misc@1234");
        assertEquals(Misc.class, oid.getObjClass());
        assertEquals("pt.ist.fenixframework.util.Misc@1234", oid.getFullId());
        assertEquals("pt.ist.fenixframework.util.Misc@1234", oid.toExternalId());
        assertEquals("pt.ist.fenixframework.util.Misc@1234", oid.toString());
    }

    @Test
    public void constructorExceptionTest() {
        assertThrows(MissingObjectException.class, () -> new OID("testblah@test1234"));
    }

    @Test
    public void equalsFalseTest() {
        OID oid = new OID("pt.ist.fenixframework.util.Misc@1234");
        OID other = new OID("pt.ist.fenixframework.util.Misc@1235");
        OID other2 = new OID("pt.ist.fenixframework.util.TxMap@1234");
        assertFalse(oid.equals(other));
        assertNotEquals(0, oid.compareTo(other));
        assertFalse(oid.equals(other2));
        assertNotEquals(0, oid.compareTo(other2));
        assertFalse(oid.equals(new Object()));
    }

    @Test
    public void equalsTrue() {
        OID oid = new OID("pt.ist.fenixframework.util.Misc@1234");
        OID other = new OID("pt.ist.fenixframework.util.Misc@1234");
        assertTrue(oid.equals(oid));
        assertTrue(oid.equals(other));
        assertEquals(0, oid.compareTo(other));
    }

    @Test
    public void hashCodeTest() {
        OID oid = new OID("pt.ist.fenixframework.util.Misc@1234");
        assertEquals("pt.ist.fenixframework.util.Misc@1234".hashCode(), oid.hashCode());
    }
}
