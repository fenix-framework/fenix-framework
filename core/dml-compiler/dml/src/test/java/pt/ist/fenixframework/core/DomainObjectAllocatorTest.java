package pt.ist.fenixframework.core;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DomainObjectAllocatorTest {

    static class MyADO extends AbstractDomainObject {
        private final Object oid;

        public MyADO() {
            throw new RuntimeException("Thow shall not be called!");
        }

        public MyADO(DomainObjectAllocator.OID oid) {
            super(oid);
            this.oid = oid.oid;
        }

        @Override
        public String getExternalId() {
            return oid.toString();
        }
    }

    // "Concrete" domain object, notice there is no special constructors
    private static class MyDO extends MyADO {

    }

    @Test
    public void shouldCreateAllocator() {
        new DomainObjectAllocator(MyADO.class);
    }

    @Test(expected = Error.class)
    public void shouldThrowErrorOnMissingConstructor() {
        new DomainObjectAllocator(Object.class);
    }

    @Test
    public void shouldAllocateObject() {
        DomainObjectAllocator allocator = new DomainObjectAllocator(MyADO.class);
        AbstractDomainObject object = allocator.allocateObject(MyDO.class, "xpto");
        assertEquals(object.getClass(), MyDO.class);
        assertEquals(object.getExternalId(), "xpto");
    }

    private static final int MAX = 100_000_000;

    @Test
    public void measureAllocatorPerformance() {
        DomainObjectAllocator allocator = new DomainObjectAllocator(MyADO.class);
        long start = System.nanoTime();
        for (int i = 0; i < MAX; i++) {
            allocator.allocateObject(MyDO.class, "xpto");
        }
        BigDecimal total = BigDecimal.valueOf(System.nanoTime() - start).divide(BigDecimal.valueOf(MAX));
        System.out.println("Each allocation with new mechanism took " + total + "ns");
    }

}
