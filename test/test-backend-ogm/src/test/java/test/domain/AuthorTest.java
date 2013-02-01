package test.domain;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

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
