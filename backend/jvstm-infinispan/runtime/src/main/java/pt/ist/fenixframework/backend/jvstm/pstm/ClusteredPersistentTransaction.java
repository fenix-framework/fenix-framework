package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.ActiveTransactionsRecord;
import jvstm.VBoxBody;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.cluster.ClusterUtils;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;

public class ClusteredPersistentTransaction extends PersistentTransaction {

    private static final Logger logger = LoggerFactory.getLogger(ClusteredPersistentTransaction.class);

    public ClusteredPersistentTransaction(ActiveTransactionsRecord record) {
        super(record);
    }

    @Override
    protected Cons<VBoxBody> performValidCommit() {
        // now revalidate globally
        logger.debug("Will get global cluster lock...");
        ClusterUtils.getInstance().globalLock().lock();
        logger.debug("Acquired global cluster lock");
        try {

            // update local data if needed
            // revalidate

            try {
                Cons<VBoxBody> temp = super.performValidCommit();
                return temp;
            } catch (PersistenceException pe) {
                pe.printStackTrace();
                logger.error("Error while commiting exception. Terminating server.");
                System.exit(-1);
                return null; // never reached, but required by the compiler
            }
        } finally {
            logger.debug("About to release global cluster lock");
            ClusterUtils.getInstance().globalLock().unlock();
        }

    }

}
