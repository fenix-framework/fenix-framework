package pt.ist.fenixframework.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

import pt.ist.fenixframework.Transaction;

public class TxMap {

    /**
     * Key based on a WeakReference, to be used with the txToLocalMap. This
     * design allows old TxLocal objects to be GC'd along with their
     * corresponding Transaction objects.
     */
    private static final class MapKey extends WeakReference<javax.transaction.Transaction> {
	private static final ReferenceQueue<javax.transaction.Transaction> REFERENCE_QUEUE = new ReferenceQueue<javax.transaction.Transaction>();

	static {
	    // This thread monitors the reference queue and removes the
	    // TxLocal object corresponding
	    // to a GC'd transaction from the txToLocalMap
	    (new Thread(TxMap.class.getName() + " GC Thread") {
		{
		    setDaemon(true);
		}

		@Override
		public void run() {
		    while (true)
			try {
			    Object ref = REFERENCE_QUEUE.remove();
			    while (TxMap.txToLocalMap.remove(ref) != null)
				;
			} catch (InterruptedException e) {
			    throw new RuntimeException(e);
			}
		}
	    }).start();
	}

	private final int hashCode;

	private MapKey(javax.transaction.Transaction transaction) {
	    super(transaction, REFERENCE_QUEUE);
	    hashCode = transaction.hashCode();
	}

	@Override
	public int hashCode() {
	    return hashCode;
	}

	@Override
	public boolean equals(Object other) {
	    if (other instanceof MapKey) {
		Object myObject = get();
		Object otherObject = ((MapKey) other).get();
		// NOTE: By design, two mapkeys originally pointing to
		// different transactions are
		// considered equal once both are cleared (for GC purposes)
		return myObject == otherObject || (myObject != null && myObject.equals(otherObject));
	    }
	    return false;
	}
    }

    private static final ConcurrentHashMap<MapKey, Transaction> txToLocalMap = new ConcurrentHashMap<MapKey, Transaction>();

    public static Transaction getTx(javax.transaction.Transaction transaction) {
	MapKey mapKey = new MapKey(transaction);
	Transaction txLocal = txToLocalMap.get(mapKey);
	if (txLocal == null) {
	    txLocal = new JTADelegatingTransaction(transaction);
	    Transaction oldTxLocal = txToLocalMap.putIfAbsent(mapKey, txLocal);
	    if (oldTxLocal != null) {
		txLocal = oldTxLocal;
		mapKey.clear(); // not going to be needed anymore, help out
				// GC
	    }
	} else {
	    mapKey.clear(); // not going to be needed anymore, help out GC
	}
	return txLocal;
    }

}
