package pt.ist.fenixframework.test.core;

import org.junit.jupiter.api.Test;

import javax.transaction.*;
import pt.ist.fenixframework.test.Classes.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractTransactionTest {
    @Test
    public void commitTest() throws Exception {
        CustomTransaction tx = new CustomTransaction();
        assertEquals(Status.STATUS_ACTIVE, tx.getStatus());
        tx.commit();
        assertEquals(Status.STATUS_COMMITTED, tx.getStatus());
    }

    @Test
    public void rollbackTest() throws Exception {
        CustomTransaction tx = new CustomTransaction();
        tx.rollback();
        assertEquals(Status.STATUS_ROLLEDBACK, tx.getStatus());
    }

    @Test
    public void rollbackOnlyCommitTest() throws Exception {
        CustomTransaction tx = new CustomTransaction();
        tx.setRollbackOnly();
        assertThrows(RollbackException.class, () -> tx.commit());
    }

    @Test
    public void syncTest() throws Exception {
        CustomTransaction tx = new CustomTransaction();
        CustomSync sync = new CustomSync();
        tx.registerSynchronization(sync);
        tx.commit();
        assertEquals(2, sync.getStatus());
    }
}
