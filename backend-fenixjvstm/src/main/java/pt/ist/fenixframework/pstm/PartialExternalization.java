package pt.ist.fenixframework.pstm;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

public class PartialExternalization {

    private static DateTimeFieldType[] DATE_TIME_FIELDS = new DateTimeFieldType[] { DateTimeFieldType.era(),
	    DateTimeFieldType.yearOfEra(), DateTimeFieldType.centuryOfEra(), DateTimeFieldType.yearOfCentury(),
	    DateTimeFieldType.year(), DateTimeFieldType.monthOfYear(), DateTimeFieldType.dayOfMonth(),
	    DateTimeFieldType.weekyearOfCentury(), DateTimeFieldType.weekyear(), DateTimeFieldType.weekOfWeekyear(),
	    DateTimeFieldType.dayOfWeek(), DateTimeFieldType.halfdayOfDay(), DateTimeFieldType.hourOfHalfday(),
	    DateTimeFieldType.clockhourOfHalfday(), DateTimeFieldType.clockhourOfDay(), DateTimeFieldType.hourOfDay(),
	    DateTimeFieldType.minuteOfDay(), DateTimeFieldType.minuteOfHour(), DateTimeFieldType.secondOfDay(),
	    DateTimeFieldType.secondOfMinute(), DateTimeFieldType.millisOfDay(), DateTimeFieldType.millisOfSecond() };


    public static String partialToString(Partial partial) {
        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < DATE_TIME_FIELDS.length; i++) {
            DateTimeFieldType field = DATE_TIME_FIELDS[i];

            if (partial.isSupported(field)) {
                if (buf.length() > 0) {
                    buf.append(",");
                }

                buf.append(field.getName() + "=" + partial.get(field));
            }
        }

        return buf.toString();
    }

    public static Partial partialFromString(String partialAsString) {
	// special case: empty string means "any date", i.e. new Partial()
	if (partialAsString == "") {
	    return new Partial();
	}

        String[] fieldValues = partialAsString.split(",");

        DateTimeFieldType[] usedFields = new DateTimeFieldType[fieldValues.length];
        int[] usedValues = new int[fieldValues.length];

        for (int i = 0; i < fieldValues.length; i++) {
            String fieldValue = fieldValues[i];

            String[] fieldValueParts = fieldValue.split("=");
            if (fieldValueParts.length != 2) {
                throw new PartialFormatException("invalid field format '" + fieldValue + "', should be '<name>=<value>'");
            }

            String name = fieldValueParts[0];
            DateTimeFieldType fieldType = getFieldByName(name);
            if (fieldType == null) {
                throw new PartialFormatException("invalid partial field name '" + name + "'");
            }

            int value;
            try {
                value = Integer.parseInt(fieldValueParts[1]);
            } catch (NumberFormatException e) {
                throw new PartialFormatException("value for field '" + name + "' is not a number: " + fieldValueParts[1], e);
            }
            
            usedFields[i] = fieldType;
            usedValues[i] = value;
        }
        
        return new Partial(usedFields, usedValues);
    }

    private static DateTimeFieldType getFieldByName(String name) {
	for (int i = 0; i < DATE_TIME_FIELDS.length; i++) {
	    DateTimeFieldType fieldType = DATE_TIME_FIELDS[i];

	    if (name.equals(fieldType.getName())) {
		return fieldType;
	    }
	}

	return null;
    }

}
