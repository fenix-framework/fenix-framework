package pt.ist.fenixframework.dml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.core.AbstractDomainObject;

@RunWith(JUnit4.class)
public class DeletionBlockerListenerTest {

    @Test
    public void testOwnDeletionBlockers() {
        Collection<String> blockers = new MyDomainObject(new DomainModel(), "Test").blockers();
        assertEquals(blockers.size(), 1);
        assertTrue(blockers.contains("Test"));
    }

    @Test
    public void testNoDeletionBlockers() {
        Collection<String> blockers = new MyDomainObject(new DomainModel(), null).blockers();
        assertEquals(blockers.size(), 0);
    }

    @Test
    public void testBlockerListener() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        model.registerDeletionBlockerListener(MyDomainObject.class, new TestDeletionBlockerListener<MyDomainObject>(
                "Test From Blocker Listener"));

        Collection<String> blockers = new MyDomainObject(model, null).blockers();
        assertEquals(blockers.size(), 1);
        assertTrue(blockers.contains("Test From Blocker Listener"));
    }

    @Test
    public void testBlockerListenerNoBlocker() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        model.registerDeletionBlockerListener(MyDomainObject.class, new TestDeletionBlockerListener<MyDomainObject>(null));

        Collection<String> blockers = new MyDomainObject(model, null).blockers();
        assertEquals(blockers.size(), 0);
    }

    @Test
    public void testBlockerListenerWithOwnBlocker() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        model.registerDeletionBlockerListener(MyDomainObject.class, new TestDeletionBlockerListener<MyDomainObject>(
                "Test From Blocker Listener"));

        Collection<String> blockers = new MyDomainObject(model, "Test From Object").blockers();
        assertEquals(blockers.size(), 2);
        assertTrue(blockers.contains("Test From Blocker Listener"));
        assertTrue(blockers.contains("Test From Object"));
    }

    @Test
    public void testBlockerListenerAbstractType() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        model.registerDeletionBlockerListener(AbstractDomainObject.class, new TestDeletionBlockerListener<AbstractDomainObject>(
                "Test From Blocker Listener"));

        Collection<String> blockers = new MyDomainObject(model, null).blockers();
        assertEquals(blockers.size(), 1);
        assertTrue(blockers.contains("Test From Blocker Listener"));
    }

    @Test
    public void testBlockerListenerWrongType() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        model.registerDeletionBlockerListener(DummyDomainObject.class, new TestDeletionBlockerListener<DummyDomainObject>(
                "Test From Blocker Listener"));

        Collection<String> blockers = new MyDomainObject(model, null).blockers();
        assertEquals(blockers.size(), 0);
    }

    private static class TestDeletionBlockerListener<T extends DomainObject> implements DeletionBlockerListener<T> {

        private final String blocker;

        public TestDeletionBlockerListener(String blocker) {
            this.blocker = blocker;
        }

        @Override
        public void getDeletionBlockers(T object, Collection<String> blockers) {
            if (blocker != null) {
                blockers.add(blocker);
            }
        }

    }

    private static class MyDomainObject extends AbstractDomainObject {

        private final DomainModel model;
        private final String blocker;

        public MyDomainObject(DomainModel model, String blocker) {
            this.model = model;
            this.blocker = blocker;
        }

        @Override
        protected DomainModel getDomainModel() {
            return model;
        }

        @Override
        protected void ensureOid() {
        }

        @Override
        protected void checkForDeletionBlockers(Collection<String> blockers) {
            if (blocker != null) {
                blockers.add(blocker);
            }
        }

        public Collection<String> blockers() {
            return getDeletionBlockers();
        }

    }

    private static final class DummyDomainObject extends AbstractDomainObject {

    }

}
