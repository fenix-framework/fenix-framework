package pt.ist.fenixframework.pstm.consistencyPredicates;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jvstm.Transaction;
import jvstm.cps.ConsistentTransaction;
import jvstm.cps.ConsistentTransactionFactory;
import jvstm.util.Cons;

/**
 * This class contains the old JVSTM code to determine which consistency
 * predicates affect which domain classes. This class is used by the framework
 * only when it is NOT allowed to create {@link DomainMetaObjects}. Otherwise,
 * the DomainMetaClass should be used under normal circumstances.
 */
public class ConsistencyPredicateSystem {

    // keeps a map of the consistency predicates for each class
    private final static Map<Class, Cons<Method>> PREDICATES_PER_CLASS = new ConcurrentHashMap<Class, Cons<Method>>();

    public static Cons<Method> getPredicatesFor(Object obj) {
        Class objClass = obj.getClass();
        Cons<Method> predicates = PREDICATES_PER_CLASS.get(objClass);
        if (predicates == null) {
            predicates = computePredicatesForClass(objClass);
            PREDICATES_PER_CLASS.put(objClass, predicates);
        }
        return predicates;
    }

    private static Cons<Method> computePredicatesForClass(Class objClass) {
        if (objClass != null) {
            Cons<Method> predicates = computePredicatesForClass(objClass.getSuperclass());
            for (Method m : objClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ConsistencyPredicate.class)
                        || m.isAnnotationPresent(jvstm.cps.ConsistencyPredicate.class)) {
                    m.setAccessible(true);
                    predicates = predicates.cons(m);
                }
            }
            return predicates;
        } else {
            return Cons.empty();
        }
    }

    public static void initialize() {
        Transaction.setTransactionFactory(new ConsistentTransactionFactory());
    }

    public static void registerNewObject(Object obj) {
        ((ConsistentTransaction) Transaction.current()).registerNewObject(obj);
    }
}
