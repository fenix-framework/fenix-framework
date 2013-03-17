package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.esw.advice.Advice;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.TransactionManager;

public class AtomicContext implements Advice {

    private static final Logger logger = LoggerFactory.getLogger(AtomicContext.class);

    private final Atomic atomic;

    AtomicContext(Atomic atomic) {
        this.atomic = atomic;
    }

    @Override
    public final <V> V perform(final Callable<V> method) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Handling @Atomic call from " + Thread.currentThread().getStackTrace()[2]);
        }
        TransactionManager tm = FenixFramework.getTransactionManager();

        return tm.withTransaction(method, this.atomic);
    }
}
