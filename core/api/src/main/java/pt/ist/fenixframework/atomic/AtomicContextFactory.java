package pt.ist.fenixframework.atomic;

import pt.ist.esw.advice.Advice;
import pt.ist.esw.advice.AdviceFactory;
import pt.ist.fenixframework.Atomic;

/**
 */
public final class AtomicContextFactory extends AdviceFactory<Atomic> {

    private final static AdviceFactory<Atomic> instance = new AtomicContextFactory();

    public static AdviceFactory<Atomic> getInstance() {
        return instance;
    }

    // Instead of processing the parameters of the Atomic to create a concrete parameterized
    // AtomicContext (which was the original motivation here), this factory simply passes the Atomic
    // to the AtomicContext.  This is because the implementation of the AtomicContext,
    // delegates the behavior to the TransactionManager.withTransaction(Callable, Atomic) method.
    // This decision was taken because, the algorithm of the withTransaction is both
    // backend-dependent and atomic-dependent.
    @Override
    public Advice newAdvice(Atomic atomic) {
        return new AtomicContext(atomic);
    }
}
