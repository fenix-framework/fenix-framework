package test;

import org.infinispan.CacheException;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.InfinispanConfig;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
@RunWith(JUnit4.class)
public class TransactionalTest {

    private static final String ISPN_CONFIG_FILE = "ispn.xml";

    @BeforeClass
    public static void init() throws Exception {
        InfinispanConfig infinispanConfig = new InfinispanConfig();
        infinispanConfig.appNameFromString("fenix-framework-test-backend-ispn");
        ConfigurationBuilderHolder holder = new ParserRegistry(Thread.currentThread().getContextClassLoader())
                .parseFile(ISPN_CONFIG_FILE);
        infinispanConfig.setDefaultConfiguration(holder.getDefaultConfigurationBuilder().build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        FenixFramework.initialize(infinispanConfig);
    }

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    @Test(expected = Exception.class)
    public void testApplicationException() throws Exception {
        throwException();
        fail("Exception expected!");
    }

    @Test(expected = RuntimeException.class)
    public void testApplicationRuntimeException() throws Exception {
        throwRuntimeException();
        fail("Exception expected!");
    }

    @Test(expected = Exception.class)
    public void testApplicationExceptionInNestedTransaction() throws Exception {
        FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throwException();
                fail("Exception expected!");
                return null;
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void testApplicationRuntimeExceptionInNestedTransaction() throws Exception {
        FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                throwRuntimeException();
                fail("Exception expected!");
                return null;
            }
        });
    }

    @Test
    public void testRetry() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (counter.getAndIncrement() == 5) {
                    return null;
                }
                throw new CacheException("Expected!");
            }
        });
        assertEquals("Wrong retry number", 6, counter.get());
    }

    @Test
    public void testRetryOnNestedTransaction() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicInteger nestedCounter = new AtomicInteger(0);
        FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (counter.getAndIncrement() == 5) {
                    return null;
                }
                FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (nestedCounter.getAndIncrement() == 5) {
                            return null;
                        }
                        throw new CacheException("Expected");
                    }
                });
                return null;

            }
        });
        assertEquals("Wrong retry number", 6, counter.get());
        assertEquals("Wrong retry number in nested", 5, nestedCounter.get());
    }

    @Test
    public void testRetryOnNestedTransaction2() throws Exception {
        final AtomicInteger counter = new AtomicInteger(0);
        final AtomicInteger nestedCounter = new AtomicInteger(0);
        FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (counter.getAndIncrement() == 5) {
                    return null;
                }
                FenixFramework.getTransactionManager().withTransaction(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        if (nestedCounter.getAndIncrement() == 5) {
                            return null;
                        }
                        throw new CacheException("Expected");
                    }
                });
                Author author = new Author(1, 1);
                author.setAge(2);
                author.setId(1);
                fail("Transaction should be invalid after nested has fail");
                return null;

            }
        });
        assertEquals("Wrong retry number", 6, counter.get());
        assertEquals("Wrong retry number in nested", 5, nestedCounter.get());
    }

    @Atomic
    private void throwException() throws Exception {
        throw new Exception("Expected!");
    }

    @Atomic
    private void throwRuntimeException() throws RuntimeException {
        throw new RuntimeException("Expected!");
    }

}
