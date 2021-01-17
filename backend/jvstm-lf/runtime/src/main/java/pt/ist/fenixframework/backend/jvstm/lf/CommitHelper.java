package pt.ist.fenixframework.backend.jvstm.lf;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.util.FenixFrameworkThread;

/**
 * This thread helps to process any commit requests that linger for too long in the commit requests queue. This can occur if the
 * node is not processing any transactions and it may prevent other nodes from joining the cluster. If, when this thread wakes up,
 * the commit requests queue still has the same element to process, this thread will help process the queue as much as possible,
 * and then sleep again for some time.
 */
public class CommitHelper extends FenixFrameworkThread {

    private static final Logger logger = LoggerFactory.getLogger(CommitHelper.class);
    private static final long SLEEP_INTERVAL = 2000;

    private static final AtomicInteger helperCount = new AtomicInteger(0);

    protected CommitHelper() {
        super("Commit helper " + helperCount.incrementAndGet());
    }

    @Override
    public void run() {
        logger.debug("Commit helper working.");
        CommitRequest lastProcessed = processCommitRequests(LockFreeClusterUtils.getCommitRequestAtHead());
        logger.debug("Initializing last processed commit request to {}", lastProcessed.getId().toString());

        while (true) {
            try {
                Thread.sleep(SLEEP_INTERVAL);
            } catch (InterruptedException e) {
                logger.info("Commit helper thread was interrupted. Terminating.");
                break;
            }

            /* by reading tail only after reading head, we ensure that tail is greater
            than or equal to head */
            CommitRequest head = LockFreeClusterUtils.getCommitRequestAtHead();
            CommitRequest tail = LockFreeClusterUtils.getCommitRequestsTail();

            /* we help to process when either there is something (head != tail)
            or current head hasn't been processed yet (head != lastProcessed)
            */
            if (head != tail || head != lastProcessed) {
                logger.debug("Helping to process the queue (head={}, tail={}, lastProcessed={}).", head.getId().toString(),
                        tail.getId().toString(), lastProcessed.getId().toString());
                lastProcessed = processCommitRequests(head);
                logger.debug("Finished processing (lastProcessed={}).", lastProcessed.getId().toString());
            }
        }
    }

    private CommitRequest processCommitRequests(CommitRequest currentRequest) {
        CommitRequest lastRequestToHandle;

        do {
            lastRequestToHandle = currentRequest;
            currentRequest = currentRequest.handle();
        } while (currentRequest != null);

        return lastRequestToHandle;
    }

}
