package pt.ist.fenixframework.pstm;

import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

import pt.ist.fenixframework.DomainObject;


public class ResultSetReader {

    public static <T extends DomainObject> T readDomainObject(ResultSet rs, String columnName) throws SQLException {
        long oid = rs.getLong(columnName);
        return (T)((oid == 0) ? null : AbstractDomainObject.fromOID(oid));
    }

    public static <T extends Enum<T>> T readEnum(Class<T> enumClass, ResultSet rs, String columnName) throws SQLException {
        String name = rs.getString(columnName);
        return ((name == null) || name.equals("")) ? null : Enum.valueOf(enumClass, name);
    }

    public static boolean readboolean(ResultSet rs, String columnName) throws SQLException {
        return rs.getBoolean(columnName);
    }

    public static byte readbyte(ResultSet rs, String columnName) throws SQLException {
        return rs.getByte(columnName);
    }

    public static char readchar(ResultSet rs, String columnName) throws SQLException {
        String txt = rs.getString(columnName);
        if ((txt == null) || (txt.length() != 1)) {
            throw new SQLException("Couldn't load a char for column " + columnName);
        }
        return txt.charAt(0);
    }

    public static short readshort(ResultSet rs, String columnName) throws SQLException {
        return rs.getShort(columnName);
    }

    public static int readint(ResultSet rs, String columnName) throws SQLException {
        return rs.getInt(columnName);
    }

    public static float readfloat(ResultSet rs, String columnName) throws SQLException {
        return rs.getFloat(columnName);
    }

    public static long readlong(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }

    public static double readdouble(ResultSet rs, String columnName) throws SQLException {
        return rs.getDouble(columnName);
    }


    public static Boolean readBoolean(ResultSet rs, String columnName) throws SQLException {
        Boolean result = rs.getBoolean(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Byte readByte(ResultSet rs, String columnName) throws SQLException {
        Byte result = rs.getByte(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Character readCharacter(ResultSet rs, String columnName) throws SQLException {
        String txt = rs.getString(columnName);
        if (txt == null) {
            return null;
        }
        if (txt.length() != 1) {
            throw new SQLException("Column " + columnName + " doesn't hold a single character");
        }
        return txt.charAt(0);
    }

    public static Short readShort(ResultSet rs, String columnName) throws SQLException {
        Short result = rs.getShort(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Integer readInteger(ResultSet rs, String columnName) throws SQLException {
        Integer result = rs.getInt(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Float readFloat(ResultSet rs, String columnName) throws SQLException {
        Float result = rs.getFloat(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Long readLong(ResultSet rs, String columnName) throws SQLException {
        Long result = rs.getLong(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Double readDouble(ResultSet rs, String columnName) throws SQLException {
        Double result = rs.getDouble(columnName);
        return (rs.wasNull() ? null : result);
    }


    public static String readString(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    public static byte[] readbytearray(ResultSet rs, String columnName) throws SQLException {
        Blob aBlob = rs.getBlob(columnName);
        return (rs.wasNull() ? null : aBlob.getBytes(1L, (int) aBlob.length()));
    }

    public static DateTime readDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp tstamp = rs.getTimestamp(columnName);
        return (rs.wasNull() ? null : new DateTime(tstamp.getTime()));
    }

    public static LocalDate readLocalDate(ResultSet rs, String columnName) throws SQLException {
	/* Ideally, we would like to use an SQL DATE to store a LocalDate, but there is a bug in the mysql driver in the
	 * rs.getDate(...) method.  The driver internally loses the timezone information and then always uses the default time
	 * zone.  I.e., we would like to write something like:
	 *
	 * Date date = rs.getDate(columnName, new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC")));
	 *
	 * Additionally, trying to solve the problem by changing the JVM's default timezone to match UTC is not acceptable.
	 */
        String dateAsString = rs.getString(columnName);
        return (rs.wasNull() ? null : LocalDateExternalization.localDateFromString(dateAsString));
    }

    public static LocalTime readLocalTime(ResultSet rs, String columnName) throws SQLException {
        // Get the time without specifying a Calendar for the time
        // zone, because OJB does not use it when binding the
        // preparedStatement also.  So, we must get the java.sql.Time
        // in the default time zone.
        // 
        // Moreover, if later we decide to change this, we must
        // investigate the various options for the MySQL driver,
        // because by default I think that it does not behave as
        // expected by the JDBC specification.  Check the options
        // "noTimezoneConversionForTimeType", "useTimezone",
        // "useGmtMillisForDatetimes",
        // "useJDBCCompliantTimezoneShift", "useLegacyDatetimeCode",
        // and "useSSPSCompatibleTimezoneShift" (at least...).
        Time time = rs.getTime(columnName);

        // Construct the LocalTime with hours, minutes, and seconds,
        // for symmetry with the ToSqlConverter code (see the comment
        // there, also).
	return (rs.wasNull() ? null : new LocalTime(time.getHours(), time.getMinutes(), time.getSeconds()));
    }

    public static Partial readPartial(ResultSet rs, String columnName) throws SQLException {
        String partialAsString = rs.getString(columnName);
        return (partialAsString == null) ? null : PartialExternalization.partialFromString(partialAsString);
    }

//     public static Period readPeriod(ResultSet rs, String columnName) throws SQLException {
//     }
}
