package pt.ist.fenixframework.test.core;

import pt.ist.fenixframework.test.Classes.*;

import org.junit.jupiter.api.Test;

import javax.transaction.Status;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractTransactionManagerTest {
    @Test
    public void beginTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        manager.begin();
        assertEquals(Status.STATUS_ACTIVE, manager.getStatus());
    }

    @Test
    public void commitTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        manager.begin();
        manager.commit();
        assertEquals(Status.STATUS_COMMITTED, manager.getStatus());
    }

    @Test
    public void rollbackTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        manager.begin();
        manager.rollback();
        assertEquals(Status.STATUS_ROLLEDBACK, manager.getStatus());
    }

    @Test
    public void commitListenerCommitTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        CustomCommitListener listener = new CustomCommitListener();
        manager.addCommitListener(listener);
        manager.begin();
        manager.commit();
        assertEquals(2, listener.getStatus());
        manager.removeCommitListener(listener);
    }

    @Test
    public void commitListenerRollbackTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        CustomCommitListener listener = new CustomCommitListener();
        manager.addCommitListener(listener);
        manager.begin();
        manager.rollback();
        assertEquals(2, listener.getStatus());
        manager.removeCommitListener(listener);
    }

    @Test
    public void setRollbackOnlyTest() throws Exception {
        CustomTransactionManager manager = new CustomTransactionManager();
        manager.begin();
        manager.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, manager.getStatus());
    }

    @Test
    public void emptyTransactionTest() {
        CustomTransactionManager manager = new CustomTransactionManager();
        assertThrows(IllegalStateException.class, () -> manager.commit());
        assertThrows(IllegalStateException.class, () -> manager.rollback());
        assertThrows(IllegalStateException.class, () -> manager.setRollbackOnly());
    }
}
