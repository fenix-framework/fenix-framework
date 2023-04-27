package pt.ist.fenixframework.test.util;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.util.Misc;

public class MiscTest {
    @Test
    public void LoggerTraceTest() {
        Misc.traceClassLoaderHierarchy();
    }
}
