package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SlotTest {
    private PlainValueType type;
    private Slot slot;

    @BeforeEach
    public void beforeEach() {
        type = new PlainValueType("TestVT");
        slot = new Slot("TestSlot", type);
    }

    @Test
    public void getName() {
        assertEquals("TestSlot", slot.getName());
    }

    @Test
    public void getTypeName() {
        assertEquals("TestVT", slot.getTypeName());
    }

    @Test
    public void getType() {
        assertEquals("TestVT", slot.getType());
    }

    @Test
    public void getSlotName() {
        assertEquals(type, slot.getSlotType());
    }

    @Test
    public void hasOption() {
        assertEquals(0, slot.getOptions().size());
        slot.addOption(Slot.Option.REQUIRED);
        assertEquals(1, slot.getOptions().size());
        assertTrue(slot.hasOption(Slot.Option.REQUIRED));
        slot.addOption(Slot.Option.REQUIRED);
        assertEquals(1, slot.getOptions().size());
    }
}
