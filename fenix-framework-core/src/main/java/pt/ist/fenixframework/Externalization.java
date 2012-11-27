package pt.ist.fenixframework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import pt.ist.fenixframework.pstm.AbstractDomainObject;

public class Externalization {
    private static class NullClass implements Serializable {
	private static final long serialVersionUID = 1L;
    }
    private static final NullClass NULL_OBJECT = new NullClass();

    public static byte[] externalizeObject(Object obj) {
	if (obj == null) {
	    return externalizeSerializable(NULL_OBJECT);
	} else if (!(obj instanceof Serializable)) {
	    throw new UnsupportedOperationException(obj.getClass().getName());
	}
	return externalizeSerializable((Serializable)obj);
    }

    public static <T> T internalizeObject(byte[] bytes) {
	Object obj = internalizeSerializable(bytes);
	if (obj instanceof NullClass) {
	    return null;
	} else {
	    return (T)obj;
	}
    }

    public static byte[] externalizeSerializable(Serializable obj) {
        return externalizeSerializable(obj, false);
    }

    public static <T> T internalizeSerializable(byte[] bytes) {
        return (T) internalizeSerializable(bytes, false);
    }

    public static byte[] externalizeSerializableGZiped(Serializable obj) {
        return externalizeSerializable(obj, true);
    }

    public static <T> T internalizeSerializableGZiped(byte[] bytes) {
        return (T) internalizeSerializable(bytes, true);
    }



    private static byte[] externalizeSerializable(Serializable obj, boolean gziped) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(gziped ? new GZIPOutputStream(baos) : baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new ExternalizationException(ioe);
        }
    }

    private static Object internalizeSerializable(byte[] bytes, boolean gziped) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(gziped ? new GZIPInputStream(bais) : bais);
            Object result = ois.readObject();
            ois.close();
            return result;
        } catch (IOException ioe) {
            throw new ExternalizationException(ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new ExternalizationException(cnfe);
        }
    }

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
