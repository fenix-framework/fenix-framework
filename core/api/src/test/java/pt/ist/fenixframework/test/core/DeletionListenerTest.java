package pt.ist.fenixframework.test.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.AbstractDomainObjectAdapter;
import pt.ist.fenixframework.dml.DeletionListener;
import pt.ist.fenixframework.dml.DomainModel;

@RunWith(JUnit4.class)
public class DeletionListenerTest {

    @Test
    public void testDeletionListener() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        AtomicBoolean bool = new AtomicBoolean(false);
        model.registerDeletionListener(MyDomainObject.class, new MyDeletionListener<MyDomainObject>(bool));
        new MyDomainObject(model).deleteDomainObject();
        assertTrue(bool.get());
    }

    @Test
    public void testDeletionListenerAbstractType() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        AtomicBoolean bool = new AtomicBoolean(false);
        model.registerDeletionListener(AbstractDomainObject.class, new MyDeletionListener<AbstractDomainObject>(bool));
        new MyDomainObject(model).deleteDomainObject();
        assertTrue(bool.get());
    }

    @Test
    public void testDeletionListenerWrongType() {
        DomainModel model = new DomainModel();
        model.finalizeDomain();
        AtomicBoolean bool = new AtomicBoolean(false);
        model.registerDeletionListener(DummyDomainObject.class, new MyDeletionListener<DummyDomainObject>(bool));
        new MyDomainObject(model).deleteDomainObject();
        assertFalse(bool.get());
    }

    private static final class MyDeletionListener<T extends DomainObject> implements DeletionListener<T> {

        private final AtomicBoolean bool;

        public MyDeletionListener(AtomicBoolean bool) {
            this.bool = bool;
        }

        @Override
        public void deleting(T target) {
            bool.set(true);
        }

    }

    private static final class MyDomainObject extends AbstractDomainObjectAdapter {

        private final DomainModel model;

        public MyDomainObject(DomainModel model) {
            this.model = model;
        }

        @Override
        protected void ensureOid() {
        }

        @Override
        protected void deleteDomainObject() {
            invokeDeletionListeners();
        }

        @Override
        protected DomainModel getDomainModel() {
            return model;
        }

    }

    private static final class DummyDomainObject extends AbstractDomainObjectAdapter {

    }

}
