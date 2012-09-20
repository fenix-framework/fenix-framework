package pt.ist.fenixframework.atomic;

import pt.ist.fenixframework.Atomic;

public final class DefaultContextFactory extends ContextFactory {

    // Instead of processing the parameters of the Atomic to create a concrete parameterized
    // AtomicContext (which was the original motivation here), this factory simply passes the Atomic
    // to the AtomicContext.  This is because the implementation of the DefaultAtomicContext,
    // delegates the behaviour to the TransactionManager.withTransaction(Callable, Atomic) method.
    // This decision was taken because, the algorithm of the withTransaction is both
    // backend-dependent and atomic-dependent.
    public static AtomicContext newContext(Atomic atomic) {
        return new DefaultAtomicContext(atomic);
    }
}
