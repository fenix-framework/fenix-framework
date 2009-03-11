package pt.ist.fenixframework.pstm;

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

public class ToSqlConverter {

    // enums
    public static Object getValueForEnum(Enum value) {
        return value.name();
    }

    // primitive types
    public static Object getValueForboolean(boolean value) {
        return value;
    }

    public static Object getValueForbyte(byte value) {
        return value;
    }

    public static Object getValueForchar(char value) {
        return value;
    }

    public static Object getValueForshort(short value) {
        return value;
    }

    public static Object getValueForint(int value) {
        return value;
    }

    public static Object getValueForfloat(float value) {
        return value;
    }

    public static Object getValueForlong(long value) {
        return value;
    }

    public static Object getValueFordouble(double value) {
        return value;
    }

    
    // wrapper types
    public static Object getValueForBoolean(Boolean value) {
        return value;
    }

    public static Object getValueForByte(Byte value) {
        return value;
    }

    public static Object getValueForCharacter(Character value) {
        return value;
    }

    public static Object getValueForShort(Short value) {
        return value;
    }

    public static Object getValueForInteger(Integer value) {
        return value;
    }

    public static Object getValueForFloat(Float value) {
        return value;
    }

    public static Object getValueForLong(Long value) {
        return value;
    }

    public static Object getValueForDouble(Double value) {
        return value;
    }

    public static Object getValueForString(String value) {
        return value;
    }

    public static Object getValueForbytearray(byte[] value) {
        // which one is best to return?
        // the byte[] or a ByteArrayInputStream?
        return value;
    }

    public static Object getValueForDateTime(DateTime value) {
        return (value == null ? null : new java.sql.Timestamp(value.getMillis()));
    }

    /* See ResultSetReader.readLocalDate() for an explanation of why we use Strings instead of dates in the database */
    public static Object getValueForLocalDate(LocalDate value) {
        return (value == null ? null : LocalDateExternalization.localDateToString(value));
    }

    public static Object getValueForLocalTime(LocalTime value) {
	return (value == null ? null : new java.sql.Time(value.getHourOfDay(), value.getMinuteOfHour(), value.getSecondOfMinute()));
    }

    public static Object getValueForPartial(Partial value) {
        return (value == null) ? null : PartialExternalization.partialToString(value);
    }

//     public static Period readPeriod(ResultSet rs, String columnName) throws SQLException {
//     }
}
