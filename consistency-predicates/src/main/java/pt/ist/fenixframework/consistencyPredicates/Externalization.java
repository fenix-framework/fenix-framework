package pt.ist.fenixframework.consistencyPredicates;

import java.lang.reflect.Method;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class Externalization {

    public static Method internalizePredicateMethod(String methodToString) {
        try {
            String[] strings = methodToString.split(" ");
            String classAndMethodNames = strings[strings.length - 1];
            strings = classAndMethodNames.split("[.]");

            String methodName = strings[strings.length - 1];
            if (methodName.substring(methodName.indexOf('(') + 1, methodName.indexOf(')')).length() > 0) {
                throw new Error("Consistency Predicate Methods cannot have arguments.");
            }
            methodName = methodName.substring(0, methodName.length() - 2);

            String className = "";
            for (int i = 0; i < (strings.length - 1); i++) {
                className += strings[i] + ".";
            }
            className = className.substring(0, className.length() - 1);
            Class<? extends AbstractDomainObject> objectClass = (Class<? extends AbstractDomainObject>) Class.forName(className);
            Method declaredMethod = objectClass.getDeclaredMethod(methodName);
            // Recheck the method's modifiers in case they were changed by the programmer
            if (!declaredMethod.toString().equals(methodToString)) {
                // If they were changed the predicate needs to be re-calculated
                return null;
            }
            return declaredMethod;
        } catch (ClassNotFoundException e) {
            System.out
                    .println("The following domain class has been removed, therefore any contained consistency predicates have also been removed:");
            System.out.println(e.getMessage());
            System.out.println();
            System.out.flush();
        } catch (NoSuchMethodException e) {
            System.out.println("The following consistency predicate has been removed:");
            System.out.println(e.getMessage());
            System.out.println();
            System.out.flush();
        }
        return null;
    }

}
