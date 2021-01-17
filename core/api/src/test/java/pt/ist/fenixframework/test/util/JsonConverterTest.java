package pt.ist.fenixframework.test.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

import org.junit.jupiter.api.Test;

import pt.ist.fenixframework.util.JsonConverter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonConverterTest {

    @Test
    public void testBoolean() {
        Boolean original = new Boolean(true);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Boolean: " + element.toString());
        Boolean other = JsonConverter.getBooleanFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testByte() {
        Byte original = new Byte("12");
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Byte: " + element.toString());
        Byte other = JsonConverter.getByteFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testCharacter() {
        Character original = new Character('c');
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Character: " + element.toString());
        Character other = JsonConverter.getCharacterFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testShort() {
        Short original = new Short("42");
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Short: " + element.toString());
        Short other = JsonConverter.getShortFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testInteger() {
        Integer original = new Integer(42);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Integer: " + element.toString());
        Integer other = JsonConverter.getIntegerFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testFloat() {
        Float original = new Float(42.0);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Float: " + element.toString());
        Float other = JsonConverter.getFloatFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testLong() {
        Long original = new Long(424242424242l);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Long: " + element.toString());
        Long other = JsonConverter.getLongFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testDouble() {
        Double original = new Double(42.0d);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Double: " + element.toString());
        Double other = JsonConverter.getDoubleFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testString() {
        String original = new String("Hello World");
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("String: " + element.toString());
        String other = JsonConverter.getStringFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testbytearray() {
        byte[] original = new byte[2];
        original[0] = 4;
        original[1] = 2;
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("bytearray: " + element.toString());
        byte[] other = JsonConverter.getBytearrayFromJson(element);
        assertTrue(Arrays.equals(original, other));
    }

    @Test
    public void testDateTime() {
        DateTime original = new DateTime();
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("DateTime: " + element.toString());
        DateTime other = JsonConverter.getDateTimeFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testLocalDate() {
        LocalDate original = new LocalDate();
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("LocalDate: " + element.toString());
        LocalDate other = JsonConverter.getLocalDateFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testLocalTime() {
        LocalTime original = new LocalTime();
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("LocalTime: " + element.toString());
        LocalTime other = JsonConverter.getLocalTimeFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testPartial() {
        Partial original = new Partial(DateTimeFieldType.hourOfDay(), 10).with(DateTimeFieldType.era(), 0);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Partial: " + element.toString());
        Partial other = JsonConverter.getPartialFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testSerializable() {
        Serializable original = new DateTime();
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("Serializable: " + element.toString());
        Serializable other = JsonConverter.getSerializableFromJson(element);
        assertEquals(original, other);
    }

    @Test
    public void testJsonElement() {
        JsonObject original = new JsonObject();
        original.addProperty("it's sad but it's", true);
        JsonElement element = JsonConverter.getJsonFor(original);
        System.out.println("JsonElement: " + element.toString());
        JsonElement other = JsonConverter.getJsonElementFromJson(element);
        assertEquals(original, other);
    }

}
