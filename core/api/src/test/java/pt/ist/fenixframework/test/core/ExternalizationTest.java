package pt.ist.fenixframework.test.core;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.core.Externalization;
import pt.ist.fenixframework.test.Classes;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalizationTest {
    @Test
    public void externalizeNullObjectTest() {
        byte[] ext = Externalization.externalizeObject(null);
        assertNull(Externalization.internalizeObject(ext));
    }

    @Test
    public void externalizeJsonObjectTest() {
        JsonObject obj = new JsonObject();
        obj.addProperty("test", 1);
        byte[] ext = Externalization.externalizeObject(obj);
        assertEquals(obj, Externalization.internalizeObject(ext));
    }

    @Test
    public void externalizeSerializableTest() {
        Classes.CustomSerializable obj = new Classes.CustomSerializable("test");
        byte[] ext = Externalization.externalizeObject(obj);
        assertTrue(obj.equals(Externalization.internalizeObject(ext)));
    }

    @Test
    public void externalizeNotSerializableTest() {
        Object obj = new Object();
        assertThrows(UnsupportedOperationException.class, () -> Externalization.externalizeObject(obj));
    }
}
