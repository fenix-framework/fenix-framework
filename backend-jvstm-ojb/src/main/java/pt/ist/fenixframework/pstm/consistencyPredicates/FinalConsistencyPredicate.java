package pt.ist.fenixframework.pstm.consistencyPredicates;

import java.lang.reflect.Method;

import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.pstm.DomainMetaClass;

/**
 * A <code>FinalConsistencyPredicate</code> is a {@link PublicConsistencyPredicate} that represents a predicate method that is
 * either public or protected, and is final. It can override other {@link PublicConsistencyPredicate}s, but cannot be overridden.
 * 
 * Therefore, during the initialization, unlike the {@link PublicConsistencyPredicate}, the execution of a new
 * <code>FinalConsistencyPredicate</code> does not need to check subclasses for
 * overriding methods.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 **/
@NoDomainMetaObjects
public class FinalConsistencyPredicate extends FinalConsistencyPredicate_Base {

    public FinalConsistencyPredicate(Method predicateMethod, DomainMetaClass metaClass) {
        super();
        setPredicate(predicateMethod);
        setDomainMetaClass(metaClass);
        System.out.println("[DomainConsistencyPredicate] Created a " + getClass().getSimpleName() + " for " + getPredicate());
    }

    @Override
    public boolean isFinal() {
        return true;
    }

    /**
     * Executes this consistency predicate for all objects of the given {@link DomainMetaClass}, and all objects of subclasses.
     * Because the
     * predicate is final, it cannot be overridden at any subclass.
     * 
     * @param metaClass
     *            the {@link DomainMetaClass} for which to execute this
     *            predicate.
     */
    @Override
    public void executeConsistencyPredicateForMetaClassAndSubclasses(DomainMetaClass metaClass) {
        executeConsistencyPredicateForExistingDomainObjects(metaClass);

        for (DomainMetaClass metaSubclass : metaClass.getDomainMetaSubclasses()) {
            executeConsistencyPredicateForMetaClassAndSubclasses(metaSubclass);
        }
    }

    /**
     * This method does nothing. A <code>FinalConsistencyPredicate</code> cannot
     * be overridden by other predicates.
     */
    @Override
    public void checkOverridingMethods(DomainMetaClass metaClass) {
    }
}
