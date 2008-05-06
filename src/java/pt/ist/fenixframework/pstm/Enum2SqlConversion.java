package pt.ist.fenixframework.pstm;

import org.apache.ojb.broker.accesslayer.conversions.ConversionException;
import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;

public class Enum2SqlConversion implements FieldConversion {

    private Class<? extends Enum> enumType;
	
    public Enum2SqlConversion(Class<? extends Enum> enumType) {
        this.enumType = enumType;
    }

    public Object javaToSql(Object object) throws ConversionException {
        return (object == null) ? null : ((Enum) object).name();
    }

    public Object sqlToJava(Object object) throws ConversionException {
        return getEnum(this.enumType, (String) object);
    }

    public static Object getEnum(Class<? extends Enum> enumClass, String name) {
        return ((name == null) || name.equals("")) ? null : Enum.valueOf(enumClass, name);
    }
}
