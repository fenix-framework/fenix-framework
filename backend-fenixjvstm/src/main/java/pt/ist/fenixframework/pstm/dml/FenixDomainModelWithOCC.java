package pt.ist.fenixframework.pstm.dml;

import dml.DomainClass;
import dml.DomainEntity;
import dml.Slot;

// This class is temporary and should be removed once we get rid of
// the ojbConcreteClass fields.
public class FenixDomainModelWithOCC extends FenixDomainModel {

    /*
     * This must be reevaluated. In Fenix there are several cases of relations
     * that are differentiations of another relation.
     * 
     * For instance, there are many relations between the RootDomainObject and
     * other classes, but there is only one keyRootDomainObject in the
     * DomainObject class, rather than one for each subclass.
     * 
     * I guess that the proper way to deal with this was to declare an abstract
     * relation between RootDomainObject and DomainObject, and then add concrete
     * relations between the different subclasses of DomainObject and the
     * RootDomainObject.
     * 
     * This, however, needs further brainstorming... So, I will leave this
     * disabled for now.
     */

    @Override
    public void finalizeDomain(boolean checkForMissingExternals) {
	super.finalizeDomain(checkForMissingExternals);

	for (final DomainClass domainClass : classes.values()) {
	    final int domainClassHierarchyLevel = calculateHierarchyLevel(domainClass);
	    if (domainClassHierarchyLevel > 1) {
		final DomainClass domainObjectDescendent = findDirectDomainObjectDecendent(domainClass);
		final Slot ojbConcreteClassSlot = domainObjectDescendent.findSlot("ojbConcreteClass");
		if (ojbConcreteClassSlot == null) {
		    domainObjectDescendent.addSlot(new Slot("ojbConcreteClass", findValueType("String")));
		}
	    }
	}

	checkForRepeatedSlots();
    }

    private DomainClass findDirectDomainObjectDecendent(final DomainClass domainClass) {
	final int domainClassHierarchyLevel = calculateHierarchyLevel(domainClass);
	return domainClassHierarchyLevel == 1 ? domainClass : findDirectDomainObjectDecendent((DomainClass) domainClass
		.getSuperclass());
    }

    private int calculateHierarchyLevel(final DomainClass domainClass) {
	final DomainEntity domainEntity = domainClass.getSuperclass();
	return domainEntity == null || !isDomainClass(domainEntity) ? 0 : calculateHierarchyLevel((DomainClass) domainEntity) + 1;
    }

    private boolean isDomainClass(final DomainEntity domainEntity) {
	return domainEntity instanceof DomainClass;
    }
}
