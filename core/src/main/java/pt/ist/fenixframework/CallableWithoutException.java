package pt.ist.fenixframework;

import java.util.concurrent.Callable;

/**
 *  This interface represents the same behaviour as the {@link java.util.concurrent.Callable}
 *  interface, except that its <code>call</code> method does not throw a cheched exception.
 */
public interface CallableWithoutException<V> extends Callable<V> {

    public V call();
}