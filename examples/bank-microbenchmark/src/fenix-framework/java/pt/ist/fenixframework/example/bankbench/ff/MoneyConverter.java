package pt.ist.fenixframework.example.bankbench.ff;

import org.apache.ojb.broker.accesslayer.conversions.FieldConversion;

import pt.ist.fenixframework.example.bankbench.Money;

public class MoneyConverter implements FieldConversion {

    public Object javaToSql(Object source) {
	return ((Money)source).exportAsString();
    }

    public Object sqlToJava(Object source) {	
	return Money.fromString((String) source);
    }

}
