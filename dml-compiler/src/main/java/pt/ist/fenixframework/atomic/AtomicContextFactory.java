package pt.ist.fenixframework.atomic;

import pt.ist.fenixframework.Atomic;

public abstract class AtomicContextFactory {

    public static AtomicContext newAtomicContext(Atomic atomic) {
        throw new RuntimeException("A concrete AtomicContextFactory must define this method.");
    }

}
