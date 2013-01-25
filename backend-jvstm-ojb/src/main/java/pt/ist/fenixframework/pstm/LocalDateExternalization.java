package pt.ist.fenixframework.pstm;

import org.joda.time.LocalDate;

public class LocalDateExternalization {

    public static String localDateToString(LocalDate localDate) {
	if (localDate != null) {
	    final String dateString = String.format("%d-%02d-%02d", localDate.getYear(), localDate.getMonthOfYear(),
						    localDate.getDayOfMonth());
	    return dateString.length() != 10 ? null : dateString;
	}
	return null;
    }

    public static LocalDate localDateFromString(String localDateAsString) {
	if (localDateAsString == null || localDateAsString.length() == 0) {
	    return null;
	}
	
	int year = Integer.parseInt(localDateAsString.substring(0, 4));
	int month = Integer.parseInt(localDateAsString.substring(5, 7));
	int day = Integer.parseInt(localDateAsString.substring(8, 10));
	return year == 0 || month == 0 || day == 0 ? null : new LocalDate(year, month, day);
    }

}
