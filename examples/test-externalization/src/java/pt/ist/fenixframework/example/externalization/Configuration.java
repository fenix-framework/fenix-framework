package pt.ist.fenixframework.example.externalization;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;
import pt.ist.fenixframework.example.externalization.domain.DataStore;
import pt.ist.fenixframework.example.externalization.domain.EnumType;

import pt.ist.fenixframework.example.externalization.domain.*;

import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Partial;

import java.lang.reflect.*;
import java.util.TimeZone;
import java.util.Iterator;
import java.math.BigDecimal;

import dml.DomainClass;
import dml.Slot;

public class Configuration {

    static void initializeFenixFramework() {
        Config config = new Config() {{
            domainModelPath = "/externalization.dml";
            // this timezone stuff needs to be further investigated
   	    //dbAlias = "//localhost:3306/test?useJDBCCompliantTimezoneShift=true";
            dbAlias = "//localhost:3306/test";
            dbUsername = "test";
            dbPassword = "test";
            rootClass = Root.class;
	    /* uncomment the next line if you want the repository structure automatically updated when your domain definition
	       changes */
	    // updateRepositoryStructureIfNeeded = true;
        }};
        FenixFramework.initialize(config);
    }
    
    static DataStore createTestDataStore() {
	DataStore dataStore = new DataStore();

 	dataStore.setAboolean(true);
	dataStore.setAbyte((byte)1);
	dataStore.setAchar('c');
	dataStore.setAshort((short)2);
 	dataStore.setAnint(3);
	dataStore.setAfloat(4.4f);
	dataStore.setAlong(5l);
	dataStore.setAdouble(6d);

	dataStore.setABoolean(Boolean.TRUE);
	dataStore.setAByte(new Byte("7"));
	dataStore.setACharacter(new Character('d'));
	dataStore.setAShort(new Short("8"));
	dataStore.setAnInteger(new Integer(9));
	dataStore.setAFloat(new Float(10.5));
	dataStore.setALong(new Long(11));
	dataStore.setADouble(new Double(12.4));

	dataStore.setAString("isto é uma string");

	dataStore.setAnEnumType(EnumType.ENUM_VALUE_2);

	dataStore.setAByteArray("isto também é uma string".getBytes());

	// this fails: milliseconds are not represented in the database
	// dataStore.setADateTime(new DateTime(2008, 10, 6, 15, 8, 27, 423));

	dataStore.setADateTime(new DateTime(2008, 10, 6, 15, 8, 27, 0));
	dataStore.setALocalDate(new LocalDate(2008, 10, 15));
	dataStore.setALocalTime(new LocalTime(15, 12, 30));
	dataStore.setAPartialEmpty(new Partial((org.joda.time.Chronology)null)); // any date
 	dataStore.setAPartialLocalTime(new Partial(new LocalTime(15, 12, 30)));
 	dataStore.setAPartialDayOfMonth(new Partial(DateTimeFieldType.dayOfMonth(), 15));

	dataStore.setABigDecimalAuto(new BigDecimal("345647547346"));
 	dataStore.setABigDecimalManual(new BigDecimal("45"));
	dataStore.setACompositeValueType(CompositeValueType.fromComponents(new BigDecimal("301")/*, "euros"*/));
// 	dataStore.setACompositeValueType(CompositeValueType.fromComponents(null/*, "euros"*/));

	return dataStore;
    }

    static void checkDataStoreEquality(DataStore ds1, DataStore ds2) {
	compareObjects(ds1, ds2);
    }

    private static void checkEquality(String message, Object obj1, Object obj2) {
	boolean equal = (obj1 == obj2) || (obj1 != null && obj1.equals(obj2));
	System.out.println((equal ? "ok" : "fail") + ": " + message + ": " + obj1 + " - " + obj2);
    }


    private static boolean checkComparable(Object obj1, Object obj2) {
	if (obj1 == null) {
	    System.out.println("obj1 is null");
	    if (obj2 == null) {
		System.out.println("obj2 is null");
	    }
	    return false;
	}

	if (obj1.getClass() != obj2.getClass()) {
	    System.out.println("obj1.class=" + obj1.getClass() + " differs from " + "obj2.class=" + obj1.getClass());
	    return false;
	}
	return true;
    }

    /** Compares two objects slot-by-slot using reflection. Primitive types are compared with ==; other types are compared with
     *  .equals().  This method refuses to compare two objects if their are not instances of the same class.
     */
    private static void compareObjects(Object obj1, Object obj2) {
	if (!checkComparable(obj1, obj2)) return;
	compareObjectsAtLevel(obj1, obj2, FenixFramework.getDomainModel().findClass(obj1.getClass().getName()));
    }

    private static void compareObjectsAtLevel(Object obj1, Object obj2, DomainClass classLevel) {
	Iterator<Slot> slotsIterator = classLevel.getSlots();
	while (slotsIterator.hasNext()) {
	    Slot slot = slotsIterator.next();
	    String slotName = slot.getName();

	    String slotGetterName = "get" + capitalize(slotName);
	    Method slotGetter = null;
	    try {
		slotGetter = obj1.getClass().getMethod(slotGetterName);
	    } catch (NoSuchMethodException e) {
		System.out.println("WARNING: slot " + slotName + " not comparable, because " + e);
		return;
	    }
 	    compareField(slotGetter, obj1, obj2);
	}
    }
    
    private static void compareField(Method getter, Object obj1, Object obj2) {
	Object value1 = null, value2 = null;
	try {
	    value1 = getter.invoke(obj1);
	    value2 = getter.invoke(obj2);
	} catch (IllegalAccessException e) {
	    System.out.println("Cannot access fields: " + e);
	    return;
	} catch (InvocationTargetException e) {
	    System.out.println("Cannot access fields: " + e);
	    return;
	}
	
	if (getter.getReturnType().isPrimitive()) {
	    checkEquality(getter.getName(), value1, value2);
	} else if (getter.getReturnType().isArray()) {
	    // here I assume byte []
	    checkEquality(getter.getName(), new String((byte[])value1), new String((byte[])value2));
	} else {
	    checkEquality(getter.getName(), value1, value2);
	}

    }

    private static String capitalize(String str) {
        if ((str == null) || Character.isUpperCase(str.charAt(0))) {
            return str;
        } else {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }
}
