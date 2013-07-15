package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.Cache;
import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.distribution.group.GroupingConsistentHash;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.TopologyChanged;
import org.infinispan.notifications.cachelistener.event.TopologyChangedEvent;
import org.infinispan.reconfigurableprotocol.protocol.PassiveReplicationCommitProtocol;
import org.infinispan.remoting.rpc.RpcManager;
import org.infinispan.remoting.transport.Address;
import org.infinispan.statetransfer.StateTransferManager;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;
import pt.ist.fenixframework.jmx.JmxUtil;
import pt.ist.fenixframework.jmx.annotations.MBean;
import pt.ist.fenixframework.jmx.annotations.ManagedAttribute;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;

import java.io.IOException;
import java.util.Collections;

/**
 * Used by Fenix Framework instances that have direct access to the cache.
 *
 * @author Pedro Ruivo
 * @since 2.8
 */
@MBean(category = "messaging", description = "Messaging worker that sends and process requests from others client/workers",
        objectName = "Worker")
public class LocalMessagingQueue extends AbstractMessagingQueue implements RequestHandler {

    private final Cache cache;
    private final RpcManager rpcManager;
    private final ConsistentHashListener consistentHashListener;
    private ConsistentHash consistentHash;
    private boolean isCoordinator;

    public LocalMessagingQueue(String appName, Cache cache, String jgrpConfigFile,
                               ThreadPoolRequestProcessor threadPoolRequestProcessor)
            throws Exception {
        super(appName, cache.getAdvancedCache().getComponentRegistry().getCacheMarshaller(), jgrpConfigFile, threadPoolRequestProcessor);
        this.cache = cache;
        this.rpcManager = cache.getAdvancedCache().getRpcManager();
        this.consistentHashListener = new ConsistentHashListener();
        JmxUtil.processInstance(this, appName, InfinispanBackEnd.BACKEND_NAME,
                Collections.singletonMap("remoteApplication", appName));
    }

    @ManagedAttribute(description = "Returns true if this worker is the coordinator.")
    public final boolean isCoordinator() {
        return isCoordinator;
    }

    @ManagedOperation(description = "Sets the protocol used by Infinispan in the load balancer")
    public final void setProtocol(String protocol) {
        if (protocol == null || protocol.isEmpty()) {
            return;
        }
        boolean primaryBackup = protocol.equals(PassiveReplicationCommitProtocol.UID);
        try {
            SendBuffer buffer = new SendBuffer();
            buffer.writeByte(primaryBackup ? MessageType.SET_PB.type() : MessageType.UNSET_PB.type());
            broadcastRequest(buffer, false);
        } catch (IOException e) {
            logger.error("Error setting protocol to " + protocol, e);
        } catch (Exception e) {
            logger.error("Error setting protocol to " + protocol, e);
        }

    }

    @Override
    protected final void innerInit() {
        cache.addListener(consistentHashListener);
        try {
            StateTransferManager stateTransferManager = cache.getAdvancedCache().getComponentRegistry()
                    .getStateTransferManager();
            if (stateTransferManager != null) {
                updateConsistentHash(stateTransferManager.getCacheTopology().getCurrentCH());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected final void innerShutdown() {
        cache.removeListener(consistentHashListener);
    }

    @Override
    protected final void initConsistentHashIfNeeded() {
        //no-op
    }

    @Override
    protected final ConsistentHash getConsistentHash() {
        return consistentHash;
    }

    @Override
    protected final org.jgroups.Address localWorker() {
        return localAddress();
    }

    @Override
    protected final void handleData(org.jgroups.Address from, MessageType type, ReceivedBuffer buffer, Response response) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace("Received [" + type + "] from " + from);
        }
        switch (type) {
            case CH_REQUEST:
                if (isCoordinator) {
                    SendBuffer sendBuffer = new SendBuffer();
                    boolean localMode = consistentHash == null;
                    sendBuffer.writeBoolean(localMode);
                    if (!localMode) {
                        sendBuffer.writeByteArray(marshaller.objectToByteBuffer(consistentHash));
                    }
                    reply(response, sendBuffer.toByteArray());
                    sendBuffer.close();
                    if (logger.isTraceEnabled()) {
                        logger.trace("Replied to [" + type + "] from " + from);
                    }
                    return;
                }
                break;
            case ADDRESS_REQUEST:
                if (rpcManager != null) {
                    Address address = (Address) marshaller.objectFromByteBuffer(buffer.readByteArray());
                    reply(response, rpcManager.getAddress().equals(address));
                } else {
                    reply(response, true);
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Replied to [" + type + "] from " + from);
                }
                return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Ignoring [" + type + "] from " + from);
        }

        reply(response, null);
    }

    private void broadcastConsistentHash(ConsistentHash consistentHash) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Broadcast new consistent hash: " + consistentHash);
        }
        SendBuffer sendBuffer = new SendBuffer();
        sendBuffer.writeByte(MessageType.CH_UPDATE.type());
        sendBuffer.writeByteArray(marshaller.objectToByteBuffer(consistentHash));
        broadcastRequest(sendBuffer, false);
        sendBuffer.close();
    }

    private void updateConsistentHash(ConsistentHash consistentHash) throws Exception {
        if (getState() != State.RUNNING) {
            logger.error("Tried to update consistent hash but this queue is not running. State=" + getState());
            return;
        }
        if (rpcManager == null) {
            isCoordinator = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Tried to update consistent hash but this cache is local only");
            }
            return;
        }
        this.consistentHash = consistentHash instanceof GroupingConsistentHash ?
                ((GroupingConsistentHash) consistentHash).getConsistentHash() :
                consistentHash;
        Address localAddress = rpcManager.getAddress();
        Address coordinatorAddress = consistentHash.getMembers().get(0);
        isCoordinator = localAddress.equals(coordinatorAddress);
        if (isCoordinator) {
            broadcastConsistentHash(this.consistentHash);
        }
    }

    @Listener
    public class ConsistentHashListener {

        @TopologyChanged
        public final void topologyChanged(TopologyChangedEvent event) {
            if (!event.isPre()) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("New topology detected! " + event.getConsistentHashAtEnd());
                    }
                    updateConsistentHash(event.getConsistentHashAtEnd());
                } catch (Throwable e) {
                    //ignored
                }
            }
        }

    }
}
