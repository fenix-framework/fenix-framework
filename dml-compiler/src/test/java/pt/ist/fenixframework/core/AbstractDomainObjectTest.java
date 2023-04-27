package pt.ist.fenixframework.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.dml.DomainModel;

import java.io.ObjectStreamException;
import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractDomainObjectTest {

    static class DefaultDO extends AbstractDomainObject {

        public DefaultDO() {
            super();
        }

        // This contructor will not run, in the following tests
        public DefaultDO(DomainObjectAllocator.OID oid) {
            super(oid);
        }
    }

    static class DefaultDOWithEnsureAndExternal extends AbstractDomainObject {
        private String oid;

        public DefaultDOWithEnsureAndExternal() {
            super();
        }

        // This contructor will not run, in the following tests
        public DefaultDOWithEnsureAndExternal(DomainObjectAllocator.OID oid) {
            super(oid);
        }

        @Override
        public String getExternalId() {
            return this.oid;
        }

        @Override
        protected void ensureOid() {
            this.oid = String.valueOf((new Random()).nextInt(10000));
        }

        // serialization code

        @Override
        protected SerializedForm makeSerializedForm() {
            return new SerializedForm(this);
        }

        protected static class SerializedForm extends AbstractDomainObject.SerializedForm {
            private SerializedForm(AbstractDomainObject obj) {
                super(obj);
            }
        }
    }

    static class DefaultDOWithSerializedForm extends AbstractDomainObject {
        private String oid;

        public DefaultDOWithSerializedForm() {
            super();
        }

        public DefaultDOWithSerializedForm(String oid) {
            super();
            this.oid = oid;
        }

        // This contructor will not run, in the following tests
        public DefaultDOWithSerializedForm(DomainObjectAllocator.OID oid) {
            super(oid);
        }

        @Override
        public String getExternalId() {
            return this.oid;
        }

        @Override
        protected void ensureOid() {
            this.oid = String.valueOf((new Random()).nextInt(10000));
        }

        // serialization code

        @Override
        protected SerializedForm makeSerializedForm() {
            return new SerializedForm(this);
        }

        protected static class SerializedForm extends AbstractDomainObject.SerializedForm {
            private SerializedForm(AbstractDomainObject obj) {
                super(obj);
            }

            protected DomainObject fromExternalId(String externalId) {
                return new DefaultDOWithSerializedForm(externalId);
            }
        }
    }

    private static DomainObjectAllocator allocator;
    private static DefaultDO defaultDO;
    private static DefaultDOWithEnsureAndExternal defaultDOWithEnsureAndExternal;
    private static DefaultDOWithSerializedForm defaultDOWithSerializedForm;

    @BeforeAll
    public static void beforeAll() {
        allocator = new DomainObjectAllocator(AbstractDomainObject.class);
        // Inicialized with AbstractDomainObject constructor, but is a DefaultDO
        // The id will not be saved, because the constructor of DefaultDO is not run
        defaultDO = allocator.allocateObject(DefaultDO.class, "this-is-will-not-be-saved");
        // Inicialized with AbstractDomainObject constructor, but is a DefaultDOWithEnsureAndExternal
        defaultDOWithEnsureAndExternal = new DefaultDOWithEnsureAndExternal();
        defaultDOWithSerializedForm = new DefaultDOWithSerializedForm();
    }

    @Test
    public void defaultGetExternalId() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.getExternalId();
        });
    }

    @Test
    public void defaultGetOid() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.getOid();
        });
    }

    @Test
    public void defaultEnsureOid() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.ensureOid();
        });
    }

    @Test
    public void defaultInit$Instance() {
        defaultDO.init$Instance(true);
    }

    @Test
    public void defaultHashCode() {
        DefaultDO defaultDOCopy = allocator.allocateObject(DefaultDO.class, "id1");
        assertNotEquals(defaultDO.hashCode(), defaultDOCopy.hashCode());
    }

    @Test
    public void defaultEquals() {
        DefaultDO defaultDOCopy = allocator.allocateObject(DefaultDO.class, "id1");
        assertFalse(defaultDO.equals(defaultDOCopy));
    }

    @Test
    public void defaultGetDeletionBlockers() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.getDeletionBlockers();
        });
    }

    @Test
    public void defaultCheckForDeletionBlockers() {
        defaultDO.checkForDeletionBlockers(null);
    }

    @Test
    public void defaultDeleteDomainObject() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.deleteDomainObject();
        });
    }

    @Test
    public void defaultWriteReplaceAbstract() {
        assertThrows(UnsupportedOperationException.class, () -> {
            defaultDO.writeReplace();
        });
    }

    @Test
    public void defaultWriteReplace() throws ObjectStreamException {
        DefaultDOWithSerializedForm.SerializedForm serializedForm =
                (DefaultDOWithSerializedForm.SerializedForm) defaultDOWithSerializedForm.writeReplace();
    }

    @Test
    public void defaultToString() {
        assertTrue(defaultDOWithEnsureAndExternal.toString()
                .contains("pt.ist.fenixframework.core.AbstractDomainObjectTest$DefaultDOWithEnsureAndExternal:"));
    }

    @Test
    public void makeSerializedFormAbstract() {
        DefaultDOWithEnsureAndExternal.SerializedForm serializedForm = defaultDOWithEnsureAndExternal.makeSerializedForm();
        assertThrows(UnsupportedOperationException.class, () -> {
            serializedForm.readResolve();
        });
    }

    @Test
    public void makeSerializedForm() throws ObjectStreamException {
        DefaultDOWithSerializedForm.SerializedForm serializedForm = defaultDOWithSerializedForm.makeSerializedForm();
        DomainObject domainObject = (DomainObject) serializedForm.readResolve();
        assertEquals(domainObject.getExternalId(), defaultDOWithSerializedForm.getExternalId());
    }
}
