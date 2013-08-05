package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.messaging.ThreadPoolRequestProcessor;
import pt.ist.fenixframework.messaging.RequestProcessor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Pedro Ruivo
 * @since 2.10
 */
@RunWith(JUnit4.class)
public class ThreadPoolLoadManagerTest {

    @Test
    public void testOverloadedAndUnderloadedWithNotification() {
        ThreadPoolRequestProcessor processor = new ThreadPoolRequestProcessor(1, 1, 1, 1, 1, "dummy");
        Listener listener = new Listener();
        processor.setLoadListener(listener);
        processor.setMaxQueueSizeNotificationThreshold(100);
        processor.setMinQueueSizeNotificationThreshold(10);
        assertState(processor, 100, 10, 0, true, false);
        assertListener(listener, false, false, false);

        //add 99 tasks
        for (int i = 1; i <= 99; ++i) {
            processor.simulateEnqueueTask();
            assertState(processor, 100, 10, i, true, false);
            assertListener(listener, false, false, false);
        }

        //add the 100th
        processor.simulateEnqueueTask();
        assertState(processor, 100, 10, 100, false, true);
        assertListener(listener, false, true, true);
        //... and add the 101st to ensure no extra notification
        processor.simulateEnqueueTask();
        assertState(processor, 100, 10, 101, false, true);
        assertListener(listener, false, false, false);

        //now remote tasks
        for (int i = 100; i > 10; --i) {
            processor.simulateDequeueTask();
            assertState(processor, 100, 10, i, false, true);
            assertListener(listener, false, false, false);
        }

        //now remove the 10th
        processor.simulateDequeueTask();
        assertState(processor, 100, 10, 10, true, false);
        assertListener(listener, true, false, true);
        //... and remove the 9th to ensure no extra notification
        processor.simulateDequeueTask();
        assertState(processor, 100, 10, 9, true, false);
        assertListener(listener, false, false, false);
    }

    @Test
    public final void testThresholdSets() {
        ThreadPoolRequestProcessor processor = new ThreadPoolRequestProcessor(1, 1, 1, 1, 1, "dummy");
        assertState(processor, 2, 1, 0, true, false);
        processor.setMinQueueSizeNotificationThreshold(10);
        assertState(processor, 11, 10, 0, true, false);
        processor.setMinQueueSizeNotificationThreshold(0);
        assertState(processor, 11, 1, 0, true, false);
        processor.setMinQueueSizeNotificationThreshold(-1);
        assertState(processor, 11, 1, 0, true, false);
        processor.setMaxQueueSizeNotificationThreshold(0);
        assertState(processor, 2, 1, 0, true, false);
        processor.setMaxQueueSizeNotificationThreshold(-1);
        assertState(processor, 2, 1, 0, true, false);
    }

    @Test
    public final void testQueue() throws InterruptedException {
        final AtomicBoolean results[] = new AtomicBoolean[3];
        final CountDownLatch[] latches = new CountDownLatch[3];
        for (int i = 0; i < 3; ++i) {
            latches[i] = new CountDownLatch(1);
            results[i] = new AtomicBoolean(false);
        }
        FenixFramework.registerReceiver(new RequestProcessor() {
            @Override
            public Object onRequest(String data) {
                int index = 0;
                try {
                    if ("dummy".equals(data)) {
                        index = 0;
                    } else if ("dummy2".equals(data)) {
                        index = 1;
                    } else if ("dummy3".equals(data)) {
                        index = 2;
                    }
                    latches[index].await();
                    results[index].set(true);
                } catch (InterruptedException e) {
                    results[index].set(false);
                    return "ERROR";
                }
                return "OK";
            }
        });
        ThreadPoolRequestProcessor processor = new ThreadPoolRequestProcessor(1, 1, 1, 1, 1, "dummy");
        Listener listener = new Listener();
        processor.setLoadListener(listener);
        assertState(processor, 2, 1, 0, true, false);
        assertListener(listener, false, false, false);
        processor.execute("dummy", null);
        Thread.sleep(1000);
        assertState(processor, 2, 1, 0, true, false);
        assertListener(listener, false, false, false);

        processor.execute("dummy2", null);
        Thread.sleep(1000);
        assertState(processor, 2, 1, 1, true, false);
        assertListener(listener, false, false, false);

        processor.execute("dummy3", null);
        Thread.sleep(1000);
        assertState(processor, 2, 1, 2, false, true);
        assertListener(listener, false, true, true);

        latches[0].countDown();
        Thread.sleep(1000);
        assertState(processor, 2, 1, 1, true, false);
        assertListener(listener, true, false, true);

        latches[1].countDown();
        Thread.sleep(1000);
        assertState(processor, 2, 1, 0, true, false);
        assertListener(listener, false, false, false);

        latches[2].countDown();
        Thread.sleep(1000);
        assertState(processor, 2, 1, 0, true, false);
        assertListener(listener, false, false, false);

        for (AtomicBoolean atomicBoolean : results) {
            assertTrue("Error reported!", atomicBoolean.get());
        }
    }

    private void assertState(ThreadPoolRequestProcessor processor, int max, int min, int current, boolean under,
                             boolean over) {
        assertEquals("Wrong max queue size", max, processor.getMaxQueueSizeNotificationThreshold());
        assertEquals("Wrong min queue size", min, processor.getMinQueueSizeNotificationThreshold());
        assertEquals("Wrong queue size", current, processor.getLoadManagerQueueSize());
        assertEquals("Wrong underloaded signal", under, processor.isUnderloaded());
        assertEquals("Wrong overloaded signal", over, processor.isOverloaded());
    }

    private void assertListener(Listener listener, boolean under, boolean over, boolean reset) {
        assertEquals("Wrong underloaded signal", under, listener.underloaded);
        assertEquals("Wrong overloaded signal", over, listener.overloaded);
        if (reset) {
            listener.underloaded = false;
            listener.overloaded = false;
        }
    }

    private class Listener implements ThreadPoolRequestProcessor.LoadListener {

        private volatile boolean overloaded;
        private volatile boolean underloaded;

        @Override
        public void overloaded() {
            overloaded = true;
        }

        @Override
        public void underloaded() {
            underloaded = true;
        }
    }
}
