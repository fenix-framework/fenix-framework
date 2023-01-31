package pt.ist.fenixframework.dml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModifiableEntityTest {
    public static class MyModifiableEntity extends ModifiableEntity {
    }

    public MyModifiableEntity myModifiableEntity;

    @BeforeEach
    public void beforeEach() {
        myModifiableEntity = new MyModifiableEntity();
    }

    @Test
    public void getModifiers() {
        assertEquals(0, myModifiableEntity.getModifiers().size());
    }

    @Test
    public void addModifier() {
        myModifiableEntity.addModifier(Modifier.PUBLIC);
        assertEquals(1, myModifiableEntity.getModifiers().size());
        assertTrue(myModifiableEntity.getModifiers().contains(Modifier.PUBLIC));
    }

    @Test
    public void hasModifier() {
        myModifiableEntity.addModifier(Modifier.PROTECTED);
        assertTrue(myModifiableEntity.hasModifier(Modifier.PROTECTED));
        assertFalse(myModifiableEntity.hasModifier(Modifier.PUBLIC));
    }

}
