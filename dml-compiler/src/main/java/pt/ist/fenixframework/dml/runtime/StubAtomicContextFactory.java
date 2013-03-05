package pt.ist.fenixframework.dml.runtime;

import java.util.concurrent.Callable;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.atomic.AtomicContext;
import pt.ist.fenixframework.atomic.AtomicContextFactory;
import pt.ist.fenixframework.atomic.AtomicContextFactoryViaReflection;

/**
 * This class is a stub used by the DML compiler's default code generator. Whenever a module that contains a DML file is compiled,
 * we need to generate the _Base classes, even though later they are not packaged in the JAR file. This class is used whenever an
 * {@link AtomicContextFactory} is required, so that any required processing of those _Base classes 'thinks' that there is already
 * an {@link AtomicContextFactory} as expected.
 * 
 * @see AtomicContextFactoryViaReflection
 */
public class StubAtomicContextFactory extends AtomicContextFactory {

    private static final AtomicContext STUB_ATOMIC_CONTEXT = new AtomicContext() {
        @Override
        public <V> V doTransactionally(Callable<V> method) throws Exception {
            throw new UnsupportedOperationException("This is a stub and should not be used."
                    + "  A real AtomicContext should be provided by the concrete BackEnd.");
        }
    };

    public static AtomicContext newAtomicContext(Atomic atomic) {
        return STUB_ATOMIC_CONTEXT;
    }

}
