package test.backend.jvstm;

import org.junit.Assert;
import org.junit.Test;

import pt.ist.fenixframework.Atomic;
import test.backend.jvstm.domain.Counter;

public class SimpleTest {

    @Test
    public void test() {
        int result = runTransaction();
        Assert.assertEquals(2, result);
    }

    @Atomic
    private int runTransaction() {
        Counter c = new Counter();
        c.inc();
        c.inc();
        return c.getValue();
    }

}
