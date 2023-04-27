package pt.ist.fenixframework.test.util;

import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.test.Classes.CustomTx;
import pt.ist.fenixframework.util.JTADelegatingTransaction;
import pt.ist.fenixframework.util.TxMap;

import javax.transaction.Transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TxMapTest {
    @Test
    public void getTxTest() {
        Transaction tx = new CustomTx();
        assertEquals(JTADelegatingTransaction.class, TxMap.getTx(tx).getClass());
    }

    @Test
    public void getDuplicateTxTest() {
        Transaction tx = new CustomTx();
        TxMap.getTx(tx);
        assertEquals(JTADelegatingTransaction.class, TxMap.getTx(tx).getClass());
    }
}
