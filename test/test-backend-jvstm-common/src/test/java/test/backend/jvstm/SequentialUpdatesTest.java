package test.backend.jvstm;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import test.backend.jvstm.domain.Counter;

public class SequentialUpdatesTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentUpdatesTest.class);

    public static class Incrementer implements Runnable {
        private final Counter counter;
        private final int numberOfIncrements;

        Incrementer(Counter counter, int numberOfIncrements) {
            this.counter = counter;
            this.numberOfIncrements = numberOfIncrements;
        }

        private void log(String text) {
            logger.debug(Thread.currentThread() + ": " + text);
        }

        @Override
        @Atomic(mode = TxMode.WRITE)
        public void run() {
            log("Incrementing upwards from " + counter.getValue());

            for (int i = 0; i < this.numberOfIncrements; i++) {
                counter.inc();
            }
            log("Done. Counter at = " + counter.getValue());
        }
    }

    private static final int MAX_THREADS = 4;
    private static final int INCS_PER_THREAD = 100000;

    @Test
    public void test() throws InterruptedException {
        // create a counter
        Counter c = createCounter();

        // inc it concurrently times
        Thread[] threads = new Thread[MAX_THREADS];

        for (int i = 0; i < MAX_THREADS; i++) {
            threads[i] = new Thread(new Incrementer(c, INCS_PER_THREAD));
            threads[i].start();
            threads[i].join();
        }

        // total should match # of incs X # of threads
        Assert.assertEquals(MAX_THREADS * INCS_PER_THREAD, getCounterValue(c));
    }

    @Atomic(mode = TxMode.WRITE)
    private Counter createCounter() {
        return new Counter();
    }

    @Atomic
    private int getCounterValue(Counter c) {
        return c.getValue();
    }

    public static void main(String[] args) throws InterruptedException {
        new ConcurrentUpdatesTest().test();
    }

}
