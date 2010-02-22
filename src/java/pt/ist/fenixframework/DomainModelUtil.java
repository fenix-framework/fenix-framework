package pt.ist.fenixframework;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import dml.DomainClass;
import dml.DomainModel;

public class DomainModelUtil {

    public static List<Class<? extends DomainObject>> getDomainClassHierarchy(final Class superClass,
	    boolean shouldContainSuperClass, boolean shouldContainAbstractClass) {
	final DomainModel domainModel = FenixFramework.getDomainModel();
	List<Class<? extends DomainObject>> classNames = new ArrayList<Class<? extends DomainObject>>();
	for (DomainClass domainClass : domainModel.getDomainClasses()) {
	    if (isClassInstance(domainClass, superClass, shouldContainSuperClass)) {
		try {
		    final Class<? extends DomainObject> clazz = (Class<? extends DomainObject>) Class.forName(domainClass
			    .getFullName());
		    if (shouldContainAbstractClass || !Modifier.isAbstract(clazz.getModifiers())) {
			classNames.add(clazz);
		    }
		} catch (ClassNotFoundException e) {
		    // ignore error
		    e.printStackTrace();
		}
	    }
	}

	return classNames;
    }

    public static DomainClass getDomainClassFor(final Class<? extends DomainObject> className) {
	final DomainModel domainModel = FenixFramework.getDomainModel();
	for (DomainClass domainClass : domainModel.getDomainClasses()) {
	    if (domainClass.getFullName().equals(className.getName())) {
		return domainClass;
	    }
	}
	return null;
    }

    public static List<Class<? extends DomainObject>> getDomainClassHierarchy(final Class superClass,
	    boolean shouldContainSuperClass) {
	return getDomainClassHierarchy(superClass, shouldContainSuperClass, true);
    }

    private static boolean isClass(final DomainClass domainClass, final Class superClass) {
	return domainClass != null && domainClass.getFullName().equals(superClass.getName());
    }

    private static boolean isClassInstance(final DomainClass domainClass, final Class superClass, boolean shouldContainSuperClass) {
	if (domainClass == null) {
	    return false;
	}
	if (isClass(domainClass, superClass)) {
	    return shouldContainSuperClass;
	}
	final DomainClass superclass = (DomainClass) domainClass.getSuperclass();
	return isClass(superclass, superClass) || isClassInstance(superclass, superClass, shouldContainSuperClass);
    }
}
