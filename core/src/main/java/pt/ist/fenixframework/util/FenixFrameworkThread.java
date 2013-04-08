package pt.ist.fenixframework.util;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass of all Threads spawned by the Fenix Framework. By extending this class,
 * the Framework's internal Threads are guaranteed to be shut down whenever the Framework
 * is shut down.
 * 
 * Implementation Note: Subclasses MUST be Interrupt-Aware, or the thread will not be
 * properly shut down, delaying the Framework's shutdown process.
 * 
 * @author Joao Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
public abstract class FenixFrameworkThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(FenixFrameworkThread.class);

    private static final ConcurrentLinkedQueue<FenixFrameworkThread> threads = new ConcurrentLinkedQueue<FenixFrameworkThread>();

    protected FenixFrameworkThread(String name) {
        super(name);
        threads.add(this);
    }

    /**
     * Shuts down the current {@link FenixFrameworkThread}. At this level, it interrupts
     * the Thread, and waits for it to complete.
     * 
     * Note that if the current thread is not Interrupt-Aware, this method will wait
     * for 10 seconds until it finishes.
     */
    protected void shutdown() {
        try {
            this.interrupt();
            this.join(10 * 1000);
            logger.info("Shutting down thread {}", this);
        } catch (InterruptedException e) {
            logger.warn("Exception in shutdown", e);
        }
    }

    /**
     * Shuts down all {@link FenixFrameworkThread}s spawned by the current application.
     */
    public static void shutdownAllThreads() {
        for (FenixFrameworkThread thread : threads) {
            thread.shutdown();
        }
    }
}
