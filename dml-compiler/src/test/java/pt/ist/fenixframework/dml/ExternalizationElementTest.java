package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalizationElementTest {
    private ExternalizationElement externalizationElement;
    private PlainValueType type;

    @BeforeEach
    public void beforeEach() {
        type = new PlainValueType("Test");
        externalizationElement = new ExternalizationElement(type, "MyMethod");
    }

    @Test
    public void getType() {
        assertEquals(type, externalizationElement.getType());
    }

    @Test
    public void getMethodName() {
        assertEquals("MyMethod", externalizationElement.getMethodName());
    }
}
