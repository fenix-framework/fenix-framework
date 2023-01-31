package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumValueTypeTest {
    private EnumValueType enumValueType;

    @BeforeEach
    public void beforeEach() {
        enumValueType = new EnumValueType("DomainName", "EnumTest");
    }

    @Test
    public void getBaseType() {
        assertThrows(Error.class, () -> enumValueType.getBaseType());
    }

    @Test
    public void getDomainName() {
        assertEquals("DomainName", enumValueType.getDomainName());
        enumValueType.setDomainName("DomainName2");
        assertEquals("DomainName2", enumValueType.getDomainName());
    }

    @Test
    public void getFullname() {
        assertEquals("EnumTest", enumValueType.getFullname());
    }

    @Test
    public void isBuiltin() {
        assertTrue(enumValueType.isBuiltin());
    }

    @Test
    public void isEnum() {
        assertTrue(enumValueType.isEnum());
    }

    @Test
    public void getExternalizationElements() {
        assertNull(enumValueType.getExternalizationElements());
    }

    @Test
    public void getInternalizationElements() {
        assertNull(enumValueType.getInternalizationMethodName());
    }
}
