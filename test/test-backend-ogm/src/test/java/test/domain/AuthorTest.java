package test.domain;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.FenixFramework;

@RunWith(JUnit4.class)
public class AuthorTest {

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    @Test
    public void testSimpleBootStrap() {
        assertTrue(FenixFramework.isInitialized());
    }

}
