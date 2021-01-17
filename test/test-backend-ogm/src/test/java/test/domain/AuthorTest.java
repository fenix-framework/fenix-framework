package test.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import pt.ist.fenixframework.FenixFramework;

public class AuthorTest {

    @AfterAll
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    @Test
    public void testSimpleBootStrap() {
        assertTrue(FenixFramework.isInitialized());
    }

}
