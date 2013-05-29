package pt.ist.fenixframework.consistencyPredicates;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.adt.bplustree.BPlusTree;

/**
 * A <code>PublicConsistencyPredicate</code> is a {@link DomainConsistencyPredicate} that represents a predicate method that is
 * either public or protected. It can override and be overridden by other <code>PublicConsistencyPredicates</code>.
 * 
 * Therefore, during the initialization, the {@link DomainDependenceRecord}s of the overridden predicate (if any)
 * are removed from this class downwards, and the new <code>PublicConsistencyPredicate</code> is executed
 * for all instances of the declaring domain class and subclasses that do not override the predicate method.
 * Likewise, on deletion, all its {@link DomainDependenceRecord}s are removed, and the overridden predicate (if any)
 * is executed for all instances of the declaring domain class and subclasses that do not override the predicate method.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 **/
@NoDomainMetaObjects
public class PublicConsistencyPredicate extends PublicConsistencyPredicate_Base {

    private static final Logger logger = LoggerFactory.getLogger(PublicConsistencyPredicate.class);

    public PublicConsistencyPredicate() {
        super();
    }

    public PublicConsistencyPredicate(Method predicateMethod, DomainMetaClass metaClass) {
        super();
        setPredicate(predicateMethod);
        setDomainMetaClass(metaClass);
        logger.info("Created a " + getClass().getSimpleName() + " for " + getPredicate());
    }

    @Override
    public boolean isPublic() {
        return true;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isFinal() {
        return false;
    }

    @Override
    public void setPublicConsistencyPredicateOverridden(PublicConsistencyPredicate publicConsistencyPredicateOverridden) {
        checkFrameworkNotInitialized();
        super.setPublicConsistencyPredicateOverridden(publicConsistencyPredicateOverridden);
    }

    @Override
    public void addPublicConsistencyPredicateOverriding(PublicConsistencyPredicate publicConsistencyPredicatesOverriding) {
        checkFrameworkNotInitialized();
        super.addPublicConsistencyPredicateOverriding(publicConsistencyPredicatesOverriding);
    }

    @Override
    public void removePublicConsistencyPredicateOverriding(PublicConsistencyPredicate publicConsistencyPredicatesOverriding) {
        checkFrameworkNotInitialized();
        super.removePublicConsistencyPredicateOverriding(publicConsistencyPredicatesOverriding);
    }

    /**
     * Finds and initializes the {@link PublicConsistencyPredicate} that is being overridden by this predicate (if any).
     * If such a predicate is found, deletes all of that predicate's {@link DomainDependenceRecord}s
     * from this predicate's class downwards.
     */
    @Override
    public void initConsistencyPredicateOverridden() {
        checkFrameworkNotInitialized();
        PublicConsistencyPredicate overriddenPredicate = findOverriddenPredicate();
        if (overriddenPredicate == null) {
            return;
        }

        overriddenPredicate.removeDomainDependenceRecordsForMetaClassAndSubclasses(getDomainMetaClass());
        setPublicConsistencyPredicateOverridden(overriddenPredicate);

        logger.info("Initializing overridden predicate of " + getPredicate() + " to " + overriddenPredicate.getPredicate());
    }

    /**
     * Deletes all of this predicate's {@link DomainDependenceRecord}s from the given metaClass downwards.
     */
    private void removeDomainDependenceRecordsForMetaClassAndSubclasses(DomainMetaClass metaClass) {
        removeDomainDependenceRecordsForExistingObjects(metaClass);

        for (DomainMetaClass metaSubclass : metaClass.getDomainMetaSubclassSet()) {
            removeDomainDependenceRecordsForMetaClassAndSubclasses(metaSubclass);
        }
    }

    /**
     * Deletes this predicate's {@link DomainDependenceRecord}s for the existing
     * objects of the given {@link DomainMetaClass}.
     */
    private void removeDomainDependenceRecordsForExistingObjects(DomainMetaClass metaClass) {
        BPlusTree<DomainMetaObject> existingObjects = metaClass.getExistingDomainMetaObjects();
        for (DomainMetaObject metaObject : existingObjects) {
            DomainDependenceRecord dependenceRecord = metaObject.getOwnDependenceRecord(this);
            if (dependenceRecord != null) {
                dependenceRecord.delete();
            }
        }
    }

    /**
     * Finds and updates the {@link PublicConsistencyPredicate} that is being overridden by this predicate (if any).
     * Only performs changes if the current information about the overridden predicate is outdated.
     */
    @Override
    public void updateConsistencyPredicateOverridden() {
        checkFrameworkNotInitialized();
        PublicConsistencyPredicate overriddenPredicate = findOverriddenPredicate();
        if (overriddenPredicate == getPublicConsistencyPredicateOverridden()) {
            return;
        }
        setPublicConsistencyPredicateOverridden(overriddenPredicate);

        logger.info("Updating overridden predicate of " + getPredicate() + " to "
                + ((overriddenPredicate == null) ? "null" : overriddenPredicate.getPredicate()));
    }

    /**
     * Searches the consecutive superclasses of this predicate's class to find the first predicate that is being overridden.
     * 
     * @return the <code>PublicConsistencyPredicate</code> that is being directly overridden by this predicate
     */
    private PublicConsistencyPredicate findOverriddenPredicate() {
        DomainMetaClass metaSuperclass = getDomainMetaClass().getDomainMetaSuperclass();
        while (metaSuperclass != null) {
            Method overriddenMethod = null;
            try {
                overriddenMethod = metaSuperclass.getDomainClass().getDeclaredMethod(getPredicate().getName());
            } catch (NoSuchMethodException e) {
                // No overridden method found, look in the next superclass
                metaSuperclass = metaSuperclass.getDomainMetaSuperclass();
                continue;
            }
            if (!overriddenMethod.isAnnotationPresent(ConsistencyPredicate.class)
                    && !overriddenMethod.isAnnotationPresent(jvstm.cps.ConsistencyPredicate.class)) {
                // Found an overridden method, but it's not a consistency predicate
                return null;
            }
            if (Modifier.isPrivate(overriddenMethod.getModifiers())) {
                // The consistency predicate at the superclass is private, so it is not being overridden.
                return null;
            }

            return DomainConsistencyPredicate.readDomainConsistencyPredicate(overriddenMethod);
        }
        return null;
    }

    /**
     * Executes this consistency predicate for all objects of the given {@link DomainMetaClass}, and objects of subclasses.
     * The predicate will NOT be executed for objects of any subclass that overrides this consistency predicate.
     * 
     * @param metaClass
     *            the {@link DomainMetaClass} for which to execute this predicate.
     */
    @Override
    public void executeConsistencyPredicateForMetaClassAndSubclasses(DomainMetaClass metaClass) {
        if (metaClass == getDomainMetaClass()) {
            // The metaClass is this very predicate's declaring class, so it is not a subclass yet.
            executeConsistencyPredicateForExistingDomainObjects(metaClass);
        } else {
            try {
                metaClass.getDomainClass().getDeclaredMethod(getPredicate().getName());
                // If no exception was thrown, the method is being overridden from this class downwards,
                // so stop and don't search in subclasses.
                return;
            } catch (NoSuchMethodException e) {
                // The method is not being overridden here, so proceed with the execution for this subclass.
                executeConsistencyPredicateForExistingDomainObjects(metaClass);
            }
        }

        // Continue searching in subclasses for overriding predicates
        for (DomainMetaClass metaSubclass : metaClass.getDomainMetaSubclassSet()) {
            executeConsistencyPredicateForMetaClassAndSubclasses(metaSubclass);
        }
    }

    /**
     * Checks all the subclasses of this consistency predicate for any methods that override it.
     * For each method found, checks that it has the {@link ConsistencyPredicate} annotation.
     * 
     * @throws Error
     *             if this predicate is being overridden by a non-predicate method
     */
    @Override
    public void checkOverridingMethods(DomainMetaClass metaClass) {
        if (metaClass == getDomainMetaClass()) {
            // The metaClass is this very predicate's declaring class, so it is not a subclass yet.
        } else {
            try {
                Method overridingMethod = metaClass.getDomainClass().getDeclaredMethod(getPredicate().getName());
                // Check that the overriding method is a consistency predicate
                if (!overridingMethod.isAnnotationPresent(ConsistencyPredicate.class)
                        && !overridingMethod.isAnnotationPresent(jvstm.cps.ConsistencyPredicate.class)) {
                    throw new Error("The method " + overridingMethod.getDeclaringClass().getName() + "."
                            + overridingMethod.getName()
                            + "() overrides a consistency predicate, so it must also have the @ConsistencyPredicate annotation.");
                }
                // If no exception was thrown, the method is being overridden from this class downwards,
                // so stop and don't search in subclasses.
                return;
            } catch (NoSuchMethodException e) {
                // The method is not being overridden here
            }
        }

        // Continue searching in subclasses for overriding predicates
        for (DomainMetaClass metaSubclass : metaClass.getDomainMetaSubclassSet()) {
            checkOverridingMethods(metaSubclass);
        }
    }

    /**
     * Deletes this <code>PublicConsistencyPredicate</code>.<br>
     * A <code>PublicConsistencyPredicate</code> should be deleted when the consistency predicate method
     * is removed from the code, or the containing class is removed.<br>
     * 
     * This method is called when the predicate is being removed, and not the class.
     * In this case, the previously-overridden predicate must be executed from this class downwards.
     * 
     * @see PublicConsistencyPredicate#classDelete()
     **/
    @Override
    public void delete() {
        PublicConsistencyPredicate overriddenPredicate = getPublicConsistencyPredicateOverridden();
        if (overriddenPredicate != null) {
            logger.info("The deleted predicate "
                    + getPredicate()
                    + ((getPredicate() == null) ? " of " + getDomainMetaClass().getDomainClass() : "")
                    + "was overriding the predicate: "
                    + overriddenPredicate.getPredicate()
                    + ((overriddenPredicate.getPredicate() == null) ? " of "
                            + overriddenPredicate.getDomainMetaClass().getDomainClass() : "")
                    + " which must be executed from the former class downwards.");
            overriddenPredicate.executeConsistencyPredicateForMetaClassAndSubclasses(getDomainMetaClass());

            for (PublicConsistencyPredicate predicatesOverriding : getPublicConsistencyPredicateOverridingSet()) {
                predicatesOverriding.setPublicConsistencyPredicateOverridden(overriddenPredicate);
            }
        }

        classDelete();
    }

    /**
     * Deletes this <code>PublicConsistencyPredicate</code>.<br>
     * A <code>PublicConsistencyPredicate</code> should be deleted when the* consistency predicate method
     * is removed from the code, or the containing class is removed.<br>
     * 
     * This method is called when the predicate's class is being removed.
     * In this case, there is no need to execute the previously-overridden predicate.
     * 
     * @see PublicConsistencyPredicate#delete()
     **/
    @Override
    public void classDelete() {
        for (PublicConsistencyPredicate predicatesOverriding : getPublicConsistencyPredicateOverridingSet()) {
            removePublicConsistencyPredicateOverriding(predicatesOverriding);
        }
        setPublicConsistencyPredicateOverridden(null);

        super.classDelete();
    }
}
