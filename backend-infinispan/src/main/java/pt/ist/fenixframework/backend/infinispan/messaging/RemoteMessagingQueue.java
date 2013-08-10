package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.marshall.Marshaller;
import org.jgroups.blocks.Response;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;
import pt.ist.fenixframework.jmx.JmxUtil;
import pt.ist.fenixframework.jmx.annotations.MBean;

import java.util.Collections;

/**
 * Used by Fenix Framework instances that does not have direct access to the cache.
 *
 * @author Pedro Ruivo
 * @since 2.8
 */
@MBean(category = "messaging", description = "Messaging client that sends requests to the workers",
        objectName = "Client")
public class RemoteMessagingQueue extends AbstractMessagingQueue {

    private volatile ConsistentHash consistentHash;
    private volatile org.jgroups.Address singleWorker;

    public RemoteMessagingQueue(String remoteAppName, String localAppName, String jgrpConfigFile, Marshaller marshaller,
                                LoadBalancePolicy loadBalancePolicy)
            throws Exception {
        super(null, remoteAppName, jgrpConfigFile, marshaller, loadBalancePolicy);
        JmxUtil.processInstance(this, localAppName, InfinispanBackEnd.BACKEND_NAME,
                Collections.singletonMap("remoteApplication", remoteAppName));
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
                RspList<?> rspList = broadcastRequest(buffer, true, false);
                for (Rsp<?> rsp : rspList.values()) {
                    Object response = rsp.getValue();
                    if (response != null && response instanceof byte[]) {
                        ReceivedBuffer receivedBuffer = new ReceivedBuffer((byte[]) response);
                        if (receivedBuffer.readBoolean()) { //local mode
                            singleWorker = rsp.getSender();
                        } else {
                            this.consistentHash = (ConsistentHash) marshaller.objectFromByteBuffer(receivedBuffer.readByteArray());
                        }
                        primaryBackup = receivedBuffer.readBoolean();
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
    protected boolean isCoordinator() {
        return false;
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
