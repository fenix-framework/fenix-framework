package test.backend.jvstm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.ist.fenixframework.Atomic;
import test.backend.jvstm.domain.Counter;

public class SimpleTest {

    @Test
    public void test() {
        int result = runTransaction();
        assertEquals(2, result);
    }

    @Atomic
    private int runTransaction() {
        Counter c = new Counter();
        c.inc();
        c.inc();
        return c.getValue();
    }

}
