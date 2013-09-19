package pt.ist.fenixframework.util;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.Externalization;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Utility class containing methods to convert the
 * Framework's builtin ValueTypes to and from Json.
 * 
 * The methods of this class are Code Generator friendly, so
 * they can be used to convert any slot to a {@link JsonElement}
 * 
 * @author João Carvalho (joao.pedro.carvalho@ist.utl.pt)
 */
public final class JsonConverter {

    // Externalizers

    public static JsonElement getJsonFor(Enum<?> value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.name());
    }

    public static JsonElement getJsonFor(Boolean value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value);
    }

    public static JsonElement getJsonFor(Character value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value);
    }

    public static JsonElement getJsonFor(Number value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value);
    }

    public static JsonElement getJsonFor(String value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value);
    }

    public static JsonElement getJsonFor(byte[] value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(DatatypeConverter.printHexBinary(value));
    }

    public static JsonElement getJsonFor(DateTime value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.getMillis());
    }

    public static JsonElement getJsonFor(LocalDate value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.toString());
    }

    public static JsonElement getJsonFor(LocalTime value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.toString());
    }

    public static JsonElement getJsonFor(Partial value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        JsonObject json = new JsonObject();
        for (DateTimeField field : value.getFields()) {
            json.addProperty(field.getName(), value.get(field.getType()));
        }
        return json;
    }

    public static JsonElement getJsonFor(Serializable value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return getJsonFor(Externalization.externalizeSerializableGZiped(value));
    }

    public static JsonElement getJsonFor(DomainObject value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return new JsonPrimitive(value.getExternalId());
    }

    public static JsonElement getJsonFor(JsonElement value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        return value;
    }

    // Internalizers

    public static <T extends Enum<T>> T getEnumFromJson(Class<T> enumClass, JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return Enum.valueOf(enumClass, value.getAsString());
    }

    public static <T extends DomainObject> T getDomainObjectFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return FenixFramework.getDomainObject(value.getAsString());
    }

    public static Serializable getSerializableFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return Externalization.internalizeSerializableGZiped(getBytearrayFromJson(value));
    }

    public static DateTime getDateTimeFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return new DateTime(value.getAsLong());
    }

    public static LocalDate getLocalDateFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return new LocalDate(value.getAsString());
    }

    public static LocalTime getLocalTimeFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return new LocalTime(value.getAsString());
    }

    public static Partial getPartialFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        Set<Entry<String, JsonElement>> entries = value.getAsJsonObject().entrySet();
        DateTimeFieldType types[] = new DateTimeFieldType[entries.size()];
        int values[] = new int[entries.size()];
        int i = 0;
        for (Entry<String, JsonElement> entry : entries) {
            types[i] = getFieldByName(entry.getKey());
            values[i] = entry.getValue().getAsInt();
            i++;
        }
        return new Partial(types, values);
    }

    public static String getStringFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsString();
    }

    public static byte[] getBytearrayFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return DatatypeConverter.parseHexBinary(value.getAsString());
    }

    public static JsonElement getJsonElementFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value;
    }

    public static Double getDoubleFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsDouble();
    }

    public static Long getLongFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsLong();
    }

    public static Boolean getBooleanFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsBoolean();
    }

    public static Byte getByteFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsByte();
    }

    public static Character getCharacterFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsCharacter();
    }

    public static char getCharFromJson(JsonElement value) {
        return value.getAsCharacter();
    }

    public static int getIntFromJson(JsonElement value) {
        return value.getAsInt();
    }

    public static Short getShortFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsShort();
    }

    public static Integer getIntegerFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsInt();
    }

    public static Float getFloatFromJson(JsonElement value) {
        if (value.isJsonNull()) {
            return null;
        }
        return value.getAsFloat();
    }

    private static DateTimeFieldType[] DATE_TIME_FIELDS = new DateTimeFieldType[] { DateTimeFieldType.era(),
            DateTimeFieldType.yearOfEra(), DateTimeFieldType.centuryOfEra(), DateTimeFieldType.yearOfCentury(),
            DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.weekyearOfCentury(), DateTimeFieldType.weekyear(), DateTimeFieldType.weekOfWeekyear(),
            DateTimeFieldType.dayOfWeek(), DateTimeFieldType.halfdayOfDay(), DateTimeFieldType.hourOfHalfday(),
            DateTimeFieldType.clockhourOfHalfday(), DateTimeFieldType.clockhourOfDay(), DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfDay(), DateTimeFieldType.minuteOfHour(), DateTimeFieldType.secondOfDay(),
            DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfDay(), DateTimeFieldType.millisOfSecond() };

    private static DateTimeFieldType getFieldByName(String name) {
        for (DateTimeFieldType fieldType : DATE_TIME_FIELDS) {
            if (name.equals(fieldType.getName())) {
                return fieldType;
            }
        }

        return null;
    }

}
