package test.backend.jvstm;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.core.WriteOnReadError;
import test.backend.jvstm.domain.Counter;

public class NestingTest {

    public class MyException extends Exception {
    }

    private static final Logger logger = LoggerFactory.getLogger(NestingTest.class);

    Counter counter;

    @BeforeEach
    @Atomic(mode = TxMode.WRITE)
    public void createCounter() {
        logger.info("Creating counter");
        this.counter = new Counter();
    }

    //////////////////////////////////////////////////////////////////////////////

    /**
     * Confirm that a committed nested increment is undone when afterwards the speculative top-level transaction needs to restart
     * because of a write. This bug was first found in the jvstm-ojb backend due to the flattening that was being performed.
     */
    @Test
    public void testNestedIncrementBeforeOuterIncrement() {
        incTopLevelAfterNestedInc(this.counter);

        assertEquals(2, getCounterValue(this.counter));
    }

    @Atomic(mode = TxMode.SPECULATIVE_READ)
    private void incTopLevelAfterNestedInc(Counter c) {
        logger.info("Begin incTopLevel");

        incNested(c);
        c.inc();

        logger.info("End incTopLevel");
    }

    //////////////////////////////////////////////////////////////////////////////

    /**
     * Confirm that a top-level read-only transaction does not allow writes to occur.
     */
    @Test
    public void testWriteFailsInTopLevelReadOnly() {
        try {
            topLevelReadOnlyInc(this.counter);
        } catch (WriteOnReadError e) {
            assertEquals(0, getCounterValue(this.counter));
            return;
        }

        fail("Expected a WriteOnReadError");
    }

    @Atomic(mode = TxMode.READ)
    private void topLevelReadOnlyInc(Counter c) {
        // this inc should not be allowed
        c.inc();
    }

    //////////////////////////////////////////////////////////////////////////////

    /**
     * Confirm that a top-level read-only transaction does not allow writes to occur within nested write transactions.
     */
    @Test
    public void testNestedWriteFailsInTopLevelReadOnly() {
        try {
            topLevelReadOnlyIncNested(this.counter);
        } catch (WriteOnReadError e) {
            assertEquals(0, getCounterValue(this.counter));
            return;
        }

        fail("Expected a WriteOnReadError");
    }

    @Atomic(mode = TxMode.READ)
    private void topLevelReadOnlyIncNested(Counter c) {
        // this inc should not be allowed
        incNested(c);
        fail("expected an exception before this point");
    }

    //////////////////////////////////////////////////////////////////////////////

    @Test
    public void testNestedFailureIsHidden() throws NotSupportedException, SystemException {
        topLevelIncCallsNestedIncThatRollsback(this.counter);
        assertEquals(1, getCounterValue(this.counter));
    }

    @Atomic(mode = TxMode.WRITE)
    private void topLevelIncCallsNestedIncThatRollsback(Counter c) {
        c.inc();
        try {
            incAndFail(c);
        } catch (MyException e) {
            assertTrue(e instanceof MyException, "Unexpected exception: " + e.getClass().getCanonicalName());
            return;
        }
        fail("Expected an exception from nested transaction");
    }

    @Atomic(flattenNested = false)
    private void incAndFail(Counter c) throws MyException {
        c.inc();
        throw new MyException();
    }

    //////////////////////////////////////////////////////////////////////////////

    @Test
    public void testFlattenNestedFailureIsNotHidden() throws NotSupportedException, SystemException {
        topLevelIncCallsFlattenNestedIncThatRollsback(this.counter);
        assertEquals(2, getCounterValue(this.counter));
    }

    @Atomic(mode = TxMode.WRITE)
    private void topLevelIncCallsFlattenNestedIncThatRollsback(Counter c) {
        c.inc();
        try {
            flattenIncAndFail(c);
        } catch (MyException e) {
            assertTrue(e instanceof MyException, "Unexpected exception: " + e.getClass().getCanonicalName());
            return;
        }
        fail("Expected an exception from flatten nested transaction");
    }

    @Atomic(flattenNested = true)
    private void flattenIncAndFail(Counter c) throws MyException {
        c.inc();
        throw new MyException();
    }

    //////////////////////////////////////////////////////////////////////////////
    // Common methods

    @Atomic(mode = TxMode.SPECULATIVE_READ)
    private void incNested(Counter c) {
        logger.info("Begin incNested");

        c.inc();

        logger.info("End incNested");
    }

    @Atomic
    private int getCounterValue(Counter c) {
        return c.getValue();
    }

    public static void main(String[] args) throws InterruptedException {
        new NestingTest().testNestedIncrementBeforeOuterIncrement();
    }
}
