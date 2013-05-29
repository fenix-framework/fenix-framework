package pt.ist.fenixframework.consistencyPredicates;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import jvstm.Transaction;
import jvstm.cps.ConsistencyCheckTransaction;
import jvstm.cps.Depended;
import jvstm.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.DomainFenixFrameworkRoot;
import pt.ist.fenixframework.DomainMetaClass;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.adt.bplustree.BPlusTree;

/**
 * A <code>DomainConsistencyPredicate</code> is the persistent domain entity
 * that represents a consistency predicate method inside a domain class. The {@link DomainFenixFrameworkRoot} uses this
 * entity after each initialization, to determine which predicates were already known, and which are new.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 **/
@NoDomainMetaObjects
public abstract class DomainConsistencyPredicate extends DomainConsistencyPredicate_Base {

    private static final Logger logger = LoggerFactory.getLogger(DomainConsistencyPredicate.class);

    public DomainConsistencyPredicate() {
        super();
        checkFrameworkNotInitialized();
        setInitialized(false);
    }

    /**
     * Checks that the {@link FenixFramework} is not initialized, throws an exception otherwise.
     * Should be called before any changes are made to {@link DomainMetaClass}es or to {@link DomainConsistencyPredicate}s.
     * 
     * @throws RuntimeException
     *             if the framework was already initialized
     **/
    protected void checkFrameworkNotInitialized() {
        if (FenixFramework.isInitialized()) {
            throw new RuntimeException("Instances of " + getClass().getSimpleName()
                    + " cannot be edited after the FenixFramework has been initialized.");
        }
    }

    public abstract boolean isPublic();

    public abstract boolean isPrivate();

    public abstract boolean isFinal();

    public boolean isInitialized() {
        return getInitialized();
    }

    @Override
    public void setInitialized(Boolean initialized) {
        checkFrameworkNotInitialized();
        super.setInitialized(initialized);
    }

    @Override
    public Method getPredicate() {
        Method predicateMethod = super.getPredicate();
        if (predicateMethod != null) {
            predicateMethod.setAccessible(true);
        }
        return predicateMethod;
    }

    @Override
    public void setPredicate(Method predicate) {
        checkFrameworkNotInitialized();
        super.setPredicate(predicate);
    }

    @Override
    public void setDomainMetaClass(DomainMetaClass domainMetaClass) {
        checkFrameworkNotInitialized();
        super.setDomainMetaClass(domainMetaClass);
    }

    /**
     * Finds and initializes the {@link PublicConsistencyPredicate} that is
     * being overridden by this predicate (if any).
     */
    public abstract void initConsistencyPredicateOverridden();

    /**
     * Finds and updates the {@link PublicConsistencyPredicate} that is being
     * overridden by this predicate (if any). Only performs changes if the
     * current information about the overridden predicate is outdated.
     */
    public abstract void updateConsistencyPredicateOverridden();

    /**
     * Executes this consistency predicate for all objects of the given {@link DomainMetaClass},
     * and objects of subclasses. The predicate will NOT be executed for objects of any subclass
     * that overrides this consistency predicate.
     * 
     * @param metaClass
     *            the {@link DomainMetaClass} for which to execute this
     *            predicate.
     */
    public abstract void executeConsistencyPredicateForMetaClassAndSubclasses(DomainMetaClass metaClass);

    /**
     * Checks all the subclasses of this consistency predicate for any methods that override it.
     * For each method found, checks that it has the {@link ConsistencyPredicate} annotation.
     * 
     * @throws Error
     *             if this predicate is being overridden by a non-predicate
     *             method
     */
    public abstract void checkOverridingMethods(DomainMetaClass metaClass);

    /**
     * Executes this consistency predicate for all objects in the given <code>List</code>.
     * For each object, after the execution, this method creates a {@link DomainDependenceRecord} based on
     * the dependencies of that object.
     * 
     * @param metaClass
     *            the {@link DomainMetaClass} of {@link DomainObject} for which to execute this predicate.
     */
    protected void executeConsistencyPredicateForExistingDomainObjects(DomainMetaClass metaClass) {
        checkFrameworkNotInitialized();
        BPlusTree<DomainMetaObject> metaObjects = metaClass.getExistingDomainMetaObjects();
        if (metaObjects.isEmpty() || getPredicate() == null) {
            return;
        }
        logger.info("Executing startup consistency predicate: " + getPredicate().getName() + " for the class: "
                + metaClass.getDomainClass().getSimpleName());

        int count = 0;
        for (DomainMetaObject existingMetaObject : metaObjects) {
            count++;
            if ((count % (ConsistencyPredicateSupport.getInstance().getBatchSize() / 2)) == 0) {
                // Commits the current, and starts a new write transaction.
                // This is necessary to split the load of the mass creation of DomainDependenceRecords among several transactions.
                // Each transaction processes a maximum of objects in order to avoid OutOfMemoryExceptions.
                // This limit is half the batch size defined in the ConsistencyPredicate Support class.
                // Because this method checks for repeated OwnDependenceRecords of each meta object being checked, there is no problem with
                // processing only an incomplete part of the objects of the given class.
                logger.info("Transaction finished. Number of processed " + metaClass.getDomainClass().getSimpleName()
                        + " objects: " + count);
                DomainFenixFrameworkRoot.checkpointTransaction();
            }

            // The predicate was already checked during a previous incomplete initialization of this DomainConsistencyPredicate
            if (existingMetaObject.hasOwnDependenceRecord(this)) {
                continue;
            }
            Pair pair = executePredicateForOneObject(existingMetaObject.getDomainObject(), getPredicate());
            // If an object is consistent and only depends on itself, the DomainDependenceRecord is not necessary.
            if (!isConsistent(pair) || !dependsOnlyOnItself(pair)) {
                new DomainDependenceRecord(existingMetaObject.getDomainObject(), this, (Set<Depended>) pair.first,
                        (Boolean) pair.second);
            }
        }

        logger.info("Transaction finished. Number of processed " + metaClass.getDomainClass().getSimpleName() + " objects: "
                + count);
        DomainFenixFrameworkRoot.checkpointTransaction();
    }

    public static boolean isConsistent(Pair pair) {
        return (Boolean) pair.second;
    }

    public static boolean dependsOnlyOnItself(Pair pair) {
        return ((Set<Depended>) pair.first).isEmpty();
    }

    /**
     * Executes a certain consistency predicate method for a single object, with
     * the only goal of returning information about the consistency of the
     * object, and its dependencies. Even if the predicate returns false, no
     * exception is thrown, and no transaction is aborted.<br>
     * This method should only be invoked during the initialization of the {@link FenixFramework}.
     * 
     * @param obj
     *            the <code>Object</code> for which to execute the predicate
     * @param predicate
     *            the <code>Method</code> of the predicate to execute
     * @return a {@link Pair} that contains a <code>Set</code> of {@link Depended} with the dependencies of the object's
     *         consistency, and a boolean that indicates if the object is
     *         consistent or not
     */
    private Pair executePredicateForOneObject(DomainObject obj, Method predicate) {
        checkFrameworkNotInitialized();
        // starts a new transaction where the readSet used by the predicate will be collected.
        ConsistencyCheckTransaction tx =
                ConsistencyPredicateSupport.getInstance().createNewConsistencyCheckTransactionForObject(obj);
        tx.start();

        try {
            // returns a pair with the depended of the transaction (readSet)
            // and the result of the predicate (consistent or not)
            Boolean predicateResult = (Boolean) predicate.invoke(obj);
            Transaction.commit();

            // Do not register the own object as a depended
            Set<Depended> depended = tx.getDepended();
            if (!depended.isEmpty()) {
                depended.remove(DomainMetaObject.getDomainMetaObjectFor(obj));
            }
            return new Pair(depended, predicateResult);
        } catch (InvocationTargetException ite) {
            // if an exception is thrown during the execution of the predicate,
            // the object is NOT consistent
            Transaction.commit();

            // Do not register the own object as a depended
            Set<Depended> depended = tx.getDepended();
            if (!depended.isEmpty()) {
                depended.remove(DomainMetaObject.getDomainMetaObjectFor(obj));
            }
            return new Pair(depended, false);
        } catch (Throwable t) {
            // any other kind of throwable is an Error in the framework that should be fixed
            throw new Error(t);
        }
    }

    /**
     * Deletes this <code>DomainConsistencyPredicate</code>.<br>
     * A <code>DomainConsistencyPredicate</code> should be deleted when the
     * consistency predicate method is removed from the code, or the containing
     * class is removed. In either case, the framework can delete all the
     * associated {@link DomainDependenceRecords}.<br>
     * 
     * This method is called when the predicate is being removed, and not the class.
     * 
     * @see DomainConsistencyPredicate#classDelete()
     **/
    public void delete() {
        classDelete();
    }

    /**
     * Deletes this <code>DomainConsistencyPredicate</code>.<br>
     * A <code>DomainConsistencyPredicate</code> should be deleted when the
     * consistency predicate method is removed from the code, or the containing
     * class is removed. In either case, the framework can delete all the
     * associated {@link DomainDependenceRecords}.<br>
     * 
     * This method is called when the predicate's class is being removed.
     * 
     * @see DomainConsistencyPredicate#delete()
     **/
    public void classDelete() {
        checkFrameworkNotInitialized();
        logger.info("Deleting predicate " + getPredicate()
                + ((getPredicate() == null) ? " of " + getDomainMetaClass().getDomainClass() : ""));
        setInitialized(false);

        int count = 0;
        for (DomainDependenceRecord dependenceRecord : getDomainDependenceRecordSet()) {
            count++;
            if ((count % (ConsistencyPredicateSupport.getInstance().getBatchSize() / 2)) == 0) {
                // Commits the current, and starts a new write transaction.
                // This is necessary to split the load of the mass deletion of DomainDependenceRecords among several transactions.
                // Each transaction processes a maximum of objects in order to avoid OutOfMemoryExceptions.
                // This limit is half the batch size defined in the ConsistencyPredicate Support class.
                // Because this method sets the current predicate as not finalized, there is no problem with processing only an incomplete part
                // of the DomainDependenceRecords.
                logger.info("Transaction finished. Number of deleted DomainDependenceRecords: " + count);
                DomainFenixFrameworkRoot.checkpointTransaction();
            }
            dependenceRecord.delete();
        }
        setDomainMetaClass(null);
        deleteDomainObject();
    }

    public static <PredicateT extends DomainConsistencyPredicate> PredicateT readDomainConsistencyPredicate(
            Class<? extends DomainObject> domainClass, String predicateName) {
        return (PredicateT) DomainMetaClass.readDomainMetaClass(domainClass).getDeclaredConsistencyPredicate(predicateName);
    }

    public static <PredicateT extends DomainConsistencyPredicate> PredicateT readDomainConsistencyPredicate(Method predicateMethod) {
        return (PredicateT) DomainMetaClass.readDomainMetaClass(
                (Class<? extends DomainObject>) predicateMethod.getDeclaringClass()).getDeclaredConsistencyPredicate(
                predicateMethod);
    }

    /**
     * Creates a new <code>DomainConsistencyPredicate</code> of the correct type,
     * to represent the given predicate method, inside the given meta class.<br>
     * The <code>DomainConsistencyPredicate</code> returned is not yet initialized.
     */
    public static <PredicateT extends DomainConsistencyPredicate> PredicateT createNewDomainConsistencyPredicate(
            Method predicateMethod, DomainMetaClass metaClass) {
        DomainConsistencyPredicate newDomainConsistencyPredicate;
        int methodModifiers = predicateMethod.getModifiers();
        if (Modifier.isPrivate(methodModifiers)) {
            newDomainConsistencyPredicate = new PrivateConsistencyPredicate(predicateMethod, metaClass);
        } else if (Modifier.isFinal(methodModifiers)) {
            newDomainConsistencyPredicate = new FinalConsistencyPredicate(predicateMethod, metaClass);
        } else {
            newDomainConsistencyPredicate = new PublicConsistencyPredicate(predicateMethod, metaClass);
        }
        return (PredicateT) newDomainConsistencyPredicate;
    }
}
