package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParamValueTypeTest {

    private ExternalizationElement externalizationElement;
    private PlainValueType plainValueType;
    private ParamValueType paramValueType;

    @BeforeEach
    public void beforeEach() {
        plainValueType = new PlainValueType("DomainName", "PlainTest");
        externalizationElement = new ExternalizationElement(new PlainValueType("Temp"), "Temp");
        plainValueType.addExternalizationElement(externalizationElement);
        plainValueType.setInternalizationMethodName("pt.ist.fenixframework.dummy");
        paramValueType = new ParamValueType(plainValueType, "ParamTest");
    }

    @Test
    public void getBaseType() {
        assertEquals(plainValueType, paramValueType.getBaseType());
    }

    @Test
    public void getDomainName() {
        assertEquals("DomainName", paramValueType.getDomainName());
    }

    @Test
    public void getFullname() {
        assertEquals("PlainTest" + "ParamTest", paramValueType.getFullname());
    }

    @Test
    public void isBuiltin() {
        assertEquals(plainValueType.isBuiltin(), paramValueType.isBuiltin());
    }

    @Test
    public void isEnum() {
        assertEquals(plainValueType.isEnum(), paramValueType.isEnum());
    }

    @Test
    public void getExternalizationElements() {
        assertEquals(plainValueType.getExternalizationElements(), paramValueType.getExternalizationElements());
        assertEquals(1, paramValueType.getExternalizationElements().size());
        assertEquals(externalizationElement, paramValueType.getExternalizationElements().get(0));
    }

    @Test
    public void getInternalizationElements() {
        assertEquals(plainValueType.getInternalizationMethodName(), paramValueType.getInternalizationMethodName());
        assertEquals("pt.ist.fenixframework.dummy", paramValueType.getInternalizationMethodName());
    }
}
