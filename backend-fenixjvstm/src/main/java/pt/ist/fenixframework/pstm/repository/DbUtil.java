package pt.ist.fenixframework.pstm.repository;



public class DbUtil {

    public static String getFkName(String slotName) {
        return "OID_" + convertToDBStyle(slotName);
    }

    public static String convertToDBStyle(String string) {
	StringBuilder result = new StringBuilder(string.length() + 10);
	boolean first = true;
	for (char c : string.toCharArray()) {
	    if (first) {
		first = false;
	    } else if (Character.isUpperCase(c)) {
		result.append('_');
	    }
	    result.append(Character.toUpperCase(c));
	}

	return result.toString();
    }
}
