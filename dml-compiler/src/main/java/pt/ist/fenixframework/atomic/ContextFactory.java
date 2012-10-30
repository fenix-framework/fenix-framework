package pt.ist.fenixframework.atomic;

import pt.ist.fenixframework.Atomic;

public abstract class ContextFactory {

    public static AtomicContext newContext(Atomic atomic) {
        throw new RuntimeException("ContextFactories must define this method.");
    }

}
