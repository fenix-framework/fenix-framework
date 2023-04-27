package pt.ist.fenixframework.test.util;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.util.FenixFrameworkThread;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FenixFrameworkThreadTest {
    public static class CustomThread extends FenixFrameworkThread {
        private int status = 0;

        public CustomThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                status = 1;
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Successfully interrupted
                status = 2;
            }
        }

        public int getStatus() {
            return this.status;
        }
    }

    @Test
    public void shutdownAllThreadsTest() throws InterruptedException {
        CustomThread t = new CustomThread("test");
        t.start();
        FenixFrameworkThread.shutdownAllThreads();
        assertEquals(2, t.getStatus());
    }
}
