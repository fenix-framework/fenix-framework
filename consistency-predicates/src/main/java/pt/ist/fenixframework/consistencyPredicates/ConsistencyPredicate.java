package pt.ist.fenixframework.consistencyPredicates;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jvstm.cps.ConsistencyException;

/**
 * Annotates a method inside a domain class, and declares it as a consistency
 * predicate. The method must receive no arguments and return a boolean.<br>
 * A consistency predicate is used to implement a consistency rule about the
 * contents of domain slots and relations defined in DML. It should return true
 * if the domain object is consistent, and false otherwise. Instead of returning
 * false, you may choose to throw a {@link ConsistencyException}, or a subclass.<br>
 * The consistency predicates are automatically checked by the framework, at the
 * end of each write transaction.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 * @author João Cachopo - joao.cachopo@ist.utl.pt
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConsistencyPredicate {
    /**
     * The subclass of {@link ConsistencyException} that the framework should
     * throw, whenever the object is not consistent. The default value is <code>ConsistencyException.class</code>.
     */
    Class<? extends ConsistencyException> value() default ConsistencyException.class;

    /**
     * If set to <code>true</code>, the framework will allow transactions to
     * commit inconsistent objects, if they were already inconsistent before.
     * The default value is <code>false</code>.
     */
    boolean inconsistencyTolerant() default false;
}