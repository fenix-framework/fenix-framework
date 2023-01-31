package pt.ist.fenixframework.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class DomainObjectAllocatorTest {

    static abstract class AbstractDO extends AbstractDomainObject {
        private final Object oid;

        public AbstractDO() {
            throw new RuntimeException("Thow shall not be called!");
        }

        public AbstractDO(DomainObjectAllocator.OID oid) {
            super(oid);
            this.oid = oid.oid;
        }

        @Override
        public String getExternalId() {
            return oid.toString();
        }
    }

    static class MyADO extends AbstractDO {
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
    public void createAllocator() {
        new DomainObjectAllocator(MyADO.class);
    }

    @Test
    public void createAllocatorWithObjectNotExtendsOid() {
        assertThrows(Error.class, () -> {
            new DomainObjectAllocator(Object.class);
        });
    }

    @Test
    public void allocateObjectAbstractDO() {
        DomainObjectAllocator allocator = new DomainObjectAllocator(AbstractDO.class);
        assertThrows(RuntimeException.class, () -> {
            allocator.allocateObject(AbstractDO.class, "xpto");
        });
    }

    @Test
    public void allocateObjectMyADO() {
        DomainObjectAllocator allocator = new DomainObjectAllocator(MyADO.class);
        AbstractDomainObject object = allocator.allocateObject(MyADO.class, "xpto");
        assertEquals(object.getClass(), MyADO.class);
        assertEquals(object.getExternalId(), "xpto");
    }

    @Test
    public void allocateObjectMyADOWithNullClass() {
        DomainObjectAllocator allocator = new DomainObjectAllocator(MyADO.class);
        assertThrows(RuntimeException.class, () -> {
            allocator.allocateObject(null, "xpto");
        });
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

    @Test
    public void allocatorAbstractClasses() {
        final DomainObjectAllocator allocator = new DomainObjectAllocator(AbstractDO.class);
        boolean ok = false;
        try {
            allocator.allocateObject(AbstractDO.class, "xpto");
            ok = true;
        } catch (final RuntimeException ex) {
            ok = false;
        }
        if (ok) {
            throw new AssertionError("Allocation of abstract class should fail.");
        }
    }

}
