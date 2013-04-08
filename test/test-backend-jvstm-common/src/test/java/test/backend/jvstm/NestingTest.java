package test.backend.jvstm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.core.WriteOnReadError;
import test.backend.jvstm.domain.Counter;

public class NestingTest {

    private static final Logger logger = LoggerFactory.getLogger(NestingTest.class);

    Counter counter;

    @Before
    @Atomic(speculativeReadOnly = false)
    public void createCounter() {
        logger.info("Creating counter");
        this.counter = new Counter();
    }

    /**
     * Confirm that a committed nested increment is undone when afterwards the speculative top-level transaction needs to restart
     * because of a write. This bug was first found in the jvstm-ojb backend due to the flattening that was being performed.
     */
    @Test
    public void testNestedIncrementBeforeOuterIncrement() {
        incTopLevelAfterNestedInc(this.counter);

        Assert.assertEquals(2, getCounterValue(this.counter));
    }

    /**
     * Confirm that a top-level read-only transaction does not allow writes to occur.
     */
    @Test
    public void testWriteFailsInTopLevelReadOnly() {
        try {
            topLevelReadOnlyInc(this.counter);
        } catch (WriteOnReadError e) {
            Assert.assertEquals(0, getCounterValue(this.counter));
            return;
        }

        Assert.fail("Expected a WriteOnReadError");
    }

    /**
     * Confirm that a top-level read-only transaction does not allow writes to occur within write nested transactions.
     */
    @Test
    public void testNestedWriteFailsInTopLevelReadOnly() {
        try {
            topLevelReadOnlyIncNested(this.counter);
        } catch (WriteOnReadError e) {
            Assert.assertEquals(0, getCounterValue(this.counter));
            return;
        }

        Assert.fail("Expected a WriteOnReadError");
    }

    @Atomic(readOnly = true)
    private void topLevelReadOnlyInc(Counter c) {
        // this inc should not be allowed
        c.inc();
    }

    @Atomic(readOnly = true)
    private void topLevelReadOnlyIncNested(Counter c) {
        // this inc should not be allowed
        incNested(c);
    }

    @Atomic
    private int getCounterValue(Counter c) {
        return c.getValue();
    }

    @Atomic(readOnly = false, speculativeReadOnly = true)
    private void incTopLevelAfterNestedInc(Counter c) {
        logger.info("Begin incTopLevel");

        incNested(c);
        c.inc();

        logger.info("End incTopLevel");
    }

    @Atomic(readOnly = false, speculativeReadOnly = false)
    private void incNested(Counter c) {
        logger.info("Begin incNested");

        c.inc();

        logger.info("End incNested");
    }

    public static void main(String[] args) throws InterruptedException {
        new NestingTest().testNestedIncrementBeforeOuterIncrement();
    }
}
