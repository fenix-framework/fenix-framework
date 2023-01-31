package pt.ist.fenixframework.test.core;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.core.JsonElementWrapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonElementWrapperTest {
    @Test
    public void jsonWrapperTest() {
        JsonObject obj = new JsonObject();
        obj.addProperty("test", 1);
        JsonElementWrapper wrapper = new JsonElementWrapper(obj);
        assertNotNull(wrapper);
    }
}
