package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.marshall.Marshaller;
import org.jgroups.blocks.Response;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

/**
 * Used by Fenix Framework instances that does not have direct access to the cache.
 *
 * @author Pedro Ruivo
 * @since 2.8
 */
public class RemoteMessagingQueue extends AbstractMessagingQueue {

    private ConsistentHash consistentHash;
    private org.jgroups.Address singleWorker;

    public RemoteMessagingQueue(String appName, String jgrpConfigFile, Marshaller marshaller) throws Exception {
        super(appName, marshaller, jgrpConfigFile, null);
    }

    @Override
    protected final void innerInit() {
        //nothing to initialize
    }

    @Override
    protected final void innerShutdown() {
        //nothing to shutdown.
    }

    @Override
    protected final void initConsistentHashIfNeeded() throws Exception {
        if (consistentHash == null && singleWorker == null) {
            synchronized (this) {
                if (consistentHash != null || singleWorker != null) {
                    return;
                }
                SendBuffer buffer = new SendBuffer();
                buffer.writeByte(MessageType.CH_REQUEST.type());
                RspList<?> rspList = broadcastRequest(buffer, true);
                for (Rsp<?> rsp : rspList.values()) {
                    Object response = rsp.getValue();
                    if (response != null && response instanceof byte[]) {
                        ReceivedBuffer receivedBuffer = new ReceivedBuffer((byte[]) response);
                        if (receivedBuffer.readBoolean()) { //local mode
                            singleWorker = rsp.getSender();
                        } else {
                            this.consistentHash = (ConsistentHash) marshaller.objectFromByteBuffer(receivedBuffer.readByteArray());
                        }
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected final ConsistentHash getConsistentHash() {
        return consistentHash;
    }

    @Override
    protected final org.jgroups.Address localWorker() {
        return singleWorker;
    }

    @Override
    protected final void handleData(org.jgroups.Address from, MessageType type, ReceivedBuffer buffer, Response response)
            throws Exception {
        switch (type) {
            case CH_UPDATE:
                synchronized (this) {
                    this.consistentHash = (ConsistentHash) marshaller.objectFromByteBuffer(buffer.readByteArray());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Updating consistent hash " + consistentHash);
                }
                break;
        }
        if (response != null) {
            response.send(null, false);
        }
    }

}
