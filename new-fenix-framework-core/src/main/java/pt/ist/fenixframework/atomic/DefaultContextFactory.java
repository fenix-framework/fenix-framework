package pt.ist.fenixframework.atomic;

import pt.ist.fenixframework.Atomic;

public final class DefaultContextFactory extends ContextFactory {

    public static AtomicContext newContext(Atomic atomic) {
        if (atomic.readOnly()) return DefaultAtomicContext.FLATTEN_READONLY;
        if (!atomic.canFail()) return DefaultAtomicContext.FLATTEN_READWRITE;
        if (atomic.speculativeReadOnly()) return DefaultAtomicContext.READ_ONLY;
        return DefaultAtomicContext.READ_WRITE;
    }

}
