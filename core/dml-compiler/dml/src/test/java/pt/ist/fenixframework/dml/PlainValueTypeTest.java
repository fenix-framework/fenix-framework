package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlainValueTypeTest {
    private PlainValueType plainValueType;

    @BeforeEach
    public void beforeEach() {
        plainValueType = new PlainValueType("DomainName", "PlainTest");
    }

    @Test
    public void getBaseType() {
        assertEquals(plainValueType, plainValueType.getBaseType());
    }

    @Test
    public void setDomainName() {
        assertEquals("DomainName", plainValueType.getDomainName());
        plainValueType.setDomainName("DomainName2");
        assertEquals("DomainName2", plainValueType.getDomainName());
    }

    @Test
    public void getFullname() {
        assertEquals("PlainTest", plainValueType.getFullname());
    }

    @Test
    public void isBuiltin() {
        assertTrue(plainValueType.isBuiltin());
        ExternalizationElement externalizationElement = new ExternalizationElement(new PlainValueType("Temp"), "Temp");
        plainValueType.addExternalizationElement(externalizationElement);
        assertFalse(plainValueType.isBuiltin());
    }

    @Test
    public void isEnum() {
        assertFalse(plainValueType.isEnum());
    }

    @Test
    public void externalizationElement() {
        ExternalizationElement externalizationElement = new ExternalizationElement(new PlainValueType("Temp"), "Temp");
        plainValueType.addExternalizationElement(externalizationElement);
        assertEquals(1, plainValueType.getExternalizationElements().size());
        assertEquals(externalizationElement, plainValueType.getExternalizationElements().get(0));
    }

    @Test
    public void internalizationMethodName() {
        plainValueType.setInternalizationMethodName("pt.ist.fenixframework.dummy");
        assertEquals("pt.ist.fenixframework.dummy", plainValueType.getInternalizationMethodName());
    }
}
