package pt.ist.fenixframework.pstm;

import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class ResultSetReader {

    public static Long getFromBIGINT(ResultSet rs, String columnName) throws SQLException {
        Long result = rs.getLong(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Boolean getFromBIT(ResultSet rs, String columnName) throws SQLException {
        Boolean result = rs.getBoolean(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static byte[] getFromBLOB(ResultSet rs, String columnName) throws SQLException {
        Blob aBlob = rs.getBlob(columnName);
        return (rs.wasNull() ? null : aBlob.getBytes(1L, (int) aBlob.length()));
    }

    public static String getFromCHAR(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    public static Date getFromDATE(ResultSet rs, String columnName) throws SQLException {
        return rs.getDate(columnName);
    }

    public static Double getFromDOUBLE(ResultSet rs, String columnName) throws SQLException {
        Double result = rs.getDouble(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static Integer getFromINTEGER(ResultSet rs, String columnName) throws SQLException {
        Integer result = rs.getInt(columnName);
        return (rs.wasNull() ? null : result);
    }

    public static String getFromLONGVARCHAR(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    public static Time getFromTIME(ResultSet rs, String columnName) throws SQLException {
        return rs.getTime(columnName);
    }

    public static Timestamp getFromTIMESTAMP(ResultSet rs, String columnName) throws SQLException {
        return rs.getTimestamp(columnName);
    }

    public static String getFromVARCHAR(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    public static Long getFromBIGINT(ResultSet rs, int columnIndex) throws SQLException {
        Long result = rs.getLong(columnIndex);
        return (rs.wasNull() ? null : result);
    }

    public static Boolean getFromBIT(ResultSet rs, int columnIndex) throws SQLException {
        Boolean result = rs.getBoolean(columnIndex);
        return (rs.wasNull() ? null : result);
    }

    public static byte[] getFromBLOB(ResultSet rs, int columnIndex) throws SQLException {
        Blob aBlob = rs.getBlob(columnIndex);
        return (rs.wasNull() ? null : aBlob.getBytes(1L, (int) aBlob.length()));
    }

    public static String getFromCHAR(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    public static Date getFromDATE(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getDate(columnIndex);
    }

    public static Double getFromDOUBLE(ResultSet rs, int columnIndex) throws SQLException {
        Double result = rs.getDouble(columnIndex);
        return (rs.wasNull() ? null : result);
    }

    public static Integer getFromINTEGER(ResultSet rs, int columnIndex) throws SQLException {
        Integer result = rs.getInt(columnIndex);
        return (rs.wasNull() ? null : result);
    }

    public static String getFromLONGVARCHAR(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

    public static Time getFromTIME(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getTime(columnIndex);
    }

    public static Timestamp getFromTIMESTAMP(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getTimestamp(columnIndex);
    }

    public static String getFromVARCHAR(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex);
    }

}
