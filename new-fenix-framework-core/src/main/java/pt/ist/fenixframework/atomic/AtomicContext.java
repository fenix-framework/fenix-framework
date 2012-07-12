package pt.ist.fenixframework.atomic;

import java.util.concurrent.Callable;

public interface AtomicContext {
    public <V> V doTransactionally(Callable<V> method) throws Exception;
}
