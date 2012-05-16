package pt.ist.fenixframework.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.IllegalWriteException;
import pt.ist.fenixframework.pstm.Transaction;

public class ServiceManager {
    public static final Logger LOGGER = Logger.getLogger(ServiceManager.class);

    public static final Map<String, String> KNOWN_WRITE_SERVICES = new ConcurrentHashMap<String, String>();

    private static ThreadLocal<Boolean> isInServiceVar = new ThreadLocal<Boolean>();

    /**
     * Invoker for services implemented in static methods.
     * 
     * @param classname
     *            The full name of the class where the method is.
     * @param methodname
     *            The method name. A static method with this named must exist in
     *            the specified class.
     * @param arguments
     *            an array to be passed onto the service.
     * @return returns whatever the service invocation returns.
     * @throws ServiceManagerException
     *             Thrown if the reflection mechanism fails to find and invoke
     *             the desired service.
     */
    public static Object invokeServiceByName(String classname, String methodname, Object[] arguments)
	    throws ServiceManagerException {
	try {
	    Class<?> clazz = Class.forName(classname);
	    Method[] methods = clazz.getDeclaredMethods();
	    List<Method> matchingMethods = new LinkedList<Method>();
	    for (Method method : methods) {
		Class<?>[] types = method.getParameterTypes();
		if (method.getName().equals(methodname) && types.length == arguments.length
			&& Modifier.isStatic(method.getModifiers())) {
		    boolean isGood = true;
		    for (int i = 0; i < types.length; i++) {
			if (arguments[i] != null && !types[i].getClass().isAssignableFrom(arguments[i].getClass())) {
			    isGood = false;
			}
		    }
		    if (isGood) {
			matchingMethods.add(method);
		    }
		}
	    }
	    if (matchingMethods.size() == 1) {
		Method method = matchingMethods.get(0);
		return method.invoke(null, arguments);
	    } else if (matchingMethods.isEmpty()) {
		throw new ServiceManagerException("Could not find a method compatible with: "
			+ constructMethodPrint(classname, methodname, arguments));
	    } else {
		throw new ServiceManagerException("Found more than one matching method for: "
			+ constructMethodPrint(classname, methodname, arguments));
	    }
	} catch (ClassNotFoundException e) {
	    throw new ServiceManagerException("Inexisting Service Class on this call: "
		    + constructMethodPrint(classname, methodname, arguments), e);
	} catch (IllegalArgumentException e) {
	    throw new ServiceManagerException("Unable to invoke method for this call: "
		    + constructMethodPrint(classname, methodname, arguments), e);
	} catch (IllegalAccessException e) {
	    throw new ServiceManagerException("Unable to invoke method for this call:"
		    + constructMethodPrint(classname, methodname, arguments), e);
	} catch (InvocationTargetException e) {
	    throw new ServiceManagerException("Unable to invoke method for this call:"
		    + constructMethodPrint(classname, methodname, arguments), e);
	}
    }

    private static String constructMethodPrint(String classname, String methodname, Object[] arguments) {
	StringBuilder print = new StringBuilder();
	print.append(classname);
	print.append(methodname);
	print.append("(");
	for (int i = 0; i < arguments.length; i++) {
	    print.append(arguments[i] != null ? arguments[i].getClass() : "<NULL>");
	    print.append(", ");
	}
	if (print.toString().endsWith(", ")) {
	    print.delete(print.length() - 2, print.length());
	}
	print.append(")");
	return print.toString();
    }

    public static boolean isInsideService() {
	return isInServiceVar.get() != null;
    }

    public static void enterService() {
	isInServiceVar.set(Boolean.TRUE);
    }

    public static void exitService() {
	isInServiceVar.remove();
    }

    public static void initServiceInvocation(final String serviceName) {
	Transaction.setDefaultReadOnly(!ServiceManager.KNOWN_WRITE_SERVICES.containsKey(serviceName));
    }

    public static void beginTransaction() {
	if (jvstm.Transaction.current() != null) {
	    jvstm.Transaction.commit();
	}
	Transaction.begin();
    }

    public static void commitTransaction() {
	jvstm.Transaction.checkpoint();
	Transaction.currentFenixTransaction().setReadOnly();
    }

    public static void abortTransaction() {
	Transaction.abort();
	Transaction.begin();
	Transaction.currentFenixTransaction().setReadOnly();
    }

    public static void logTransactionRestart(String service, Throwable cause, int tries) {
	LOGGER.info("Service " + service + " has been restarted " + tries + " times because of "
		+ cause.getClass().getSimpleName());
    }

    public static void execute(final ServicePredicate servicePredicate) {
	final String serviceName = servicePredicate.getClass().getName();
	if (isInsideService()) {
	    servicePredicate.execute();
	} else {
	    enterService();
	    try {
		ServiceManager.initServiceInvocation(serviceName);

		boolean keepGoing = true;
		int tries = 0;
		try {
		    while (keepGoing) {
			tries++;
			try {
			    try {
				beginTransaction();
				servicePredicate.execute();
				ServiceManager.commitTransaction();
				keepGoing = false;
			    } finally {
				if (keepGoing) {
				    ServiceManager.abortTransaction();
				}
			    }
			} catch (jvstm.CommitException commitException) {
			    if (tries > 3) {
				logTransactionRestart(serviceName, commitException, tries);
			    }
			} catch (AbstractDomainObject.UnableToDetermineIdException unableToDetermineIdException) {
			    if (tries > 3) {
				logTransactionRestart(serviceName, unableToDetermineIdException, tries);
			    }
			} catch (IllegalWriteException illegalWriteException) {
			    ServiceManager.KNOWN_WRITE_SERVICES.put(servicePredicate.getClass().getName(), servicePredicate
				    .getClass().getName());
			    Transaction.setDefaultReadOnly(false);
			    if (tries > 3) {
				logTransactionRestart(serviceName, illegalWriteException, tries);
			    }
			}
		    }
		} finally {
		    Transaction.setDefaultReadOnly(false);
		    if (tries > 1) {
			LOGGER.info("Service " + serviceName + "took " + tries + " tries.");
		    }
		}
	    } finally {
		ServiceManager.exitService();
	    }
	}
    }
}
