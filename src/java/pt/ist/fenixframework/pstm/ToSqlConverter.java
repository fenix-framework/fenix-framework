package pt.ist.fenixframework.pstm;

import org.joda.time.DateTimeZone;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

public class ToSqlConverter {

    // enums
    public static String getValueForEnum(Enum value) {
        return value.name();
    }

    // primitive types
    public static boolean getValueForboolean(boolean value) {
        return value;
    }

    public static byte getValueForbyte(byte value) {
        return value;
    }

    public static char getValueForchar(char value) {
        return value;
    }

    public static short getValueForshort(short value) {
        return value;
    }

    public static int getValueForint(int value) {
        return value;
    }

    public static float getValueForfloat(float value) {
        return value;
    }

    public static long getValueForlong(long value) {
        return value;
    }

    public static double getValueFordouble(double value) {
        return value;
    }

    
    // wrapper types
    public static Boolean getValueForBoolean(Boolean value) {
        return value;
    }

    public static Byte getValueForByte(Byte value) {
        return value;
    }

    public static Character getValueForCharacter(Character value) {
        return value;
    }

    public static Short getValueForShort(Short value) {
        return value;
    }

    public static Integer getValueForInteger(Integer value) {
        return value;
    }

    public static Float getValueForFloat(Float value) {
        return value;
    }

    public static Long getValueForLong(Long value) {
        return value;
    }

    public static Double getValueForDouble(Double value) {
        return value;
    }

    public static String getValueForString(String value) {
        return value;
    }

    public static Object getValueForbytearray(byte[] value) {
        // which one is best to return?
        // the byte[] or a ByteArrayInputStream?
        return value;
    }

    public static java.sql.Timestamp getValueForDateTime(DateTime value) {
        return (value == null ? null : new java.sql.Timestamp(value.getMillis()));
    }

    /* See ResultSetReader.readLocalDate() for an explanation of why we use Strings instead of dates in the database */
    public static String getValueForLocalDate(LocalDate value) {
        return (value == null ? null : LocalDateExternalization.localDateToString(value));
    }

    public static java.sql.Time getValueForLocalTime(LocalTime value) {
        // Creating the java.sql.Time with hours, minutes, and seconds
        // creates an instant interpreting those values in the default
        // time zone.  This is needed because currently OJB is sending
        // instances of TIME to a preparedStatement without specifying
        // the time zone, which means that it will be interpreted as
        // being the default time zone.
        //
        // So, beware, that we may not change this into a new
        // java.sql.Time(value.getMillisOfDay()), because, in that
        // case, the millis would be interpreted as being an instant
        // relative to the 01/01/1970 00:00:00 GMT.
	return (value == null ? null : new java.sql.Time(value.getHourOfDay(), value.getMinuteOfHour(), value.getSecondOfMinute()));
    }

    public static String getValueForPartial(Partial value) {
        return (value == null) ? null : PartialExternalization.partialToString(value);
    }

//     public static Period readPeriod(ResultSet rs, String columnName) throws SQLException {
//     }
}
