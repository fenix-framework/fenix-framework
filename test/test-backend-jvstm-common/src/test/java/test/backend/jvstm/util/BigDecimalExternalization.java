package test.backend.jvstm.util;

import java.math.BigDecimal;

public class BigDecimalExternalization {

    public static String bigDecimalAsString(BigDecimal bigDecimal) {
        return bigDecimal.toString();
    }

    public static BigDecimal bigDecimalFromString(String val) {
        return new BigDecimal(val);
    }
}
