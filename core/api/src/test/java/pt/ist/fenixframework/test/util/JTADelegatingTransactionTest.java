package pt.ist.fenixframework.test.util;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.test.Classes.CustomTx;
import pt.ist.fenixframework.util.JTADelegatingTransaction;

import javax.transaction.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JTADelegatingTransactionTest {
    @Test
    public void getStatusTest() throws SystemException {
        Transaction original = new CustomTx();
        JTADelegatingTransaction tx = new JTADelegatingTransaction(original);
        assertEquals(1, tx.getStatus());
    }

    @Test
    public void commitTest()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, SystemException {
        Transaction original = new CustomTx();
        JTADelegatingTransaction tx = new JTADelegatingTransaction(original);
        tx.commit();
    }

    @Test
    public void rollbackTestTest() throws IllegalStateException, SystemException {
        Transaction original = new CustomTx();
        JTADelegatingTransaction tx = new JTADelegatingTransaction(original);
        tx.rollback();
    }

    @Test
    public void setRollbackOnlyTest() throws IllegalStateException, SystemException {
        Transaction original = new CustomTx();
        JTADelegatingTransaction tx = new JTADelegatingTransaction(original);
        tx.setRollbackOnly();
    }
}
