package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

import pt.ist.esw.advice.Advice;

/**
 * This class is just an adapter to transform the {@link Advice} API to another ({@link #perform(Callable)} into
 * {@link #doTransactionally(Callable)}).
 * 
 * @see Advice
 */
public abstract class AtomicContext implements Advice {
    public abstract <V> V doTransactionally(Callable<V> method) throws Exception;

    @Override
    public final <V> V perform(Callable<V> method) throws Exception {
        return doTransactionally(method);
    }
}
