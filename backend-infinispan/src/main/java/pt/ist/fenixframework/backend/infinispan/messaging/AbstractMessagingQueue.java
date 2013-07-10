package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.marshall.Marshaller;
import org.jgroups.*;
import org.jgroups.blocks.AsyncRequestHandler;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.Response;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.messaging.MessagingQueue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public abstract class AbstractMessagingQueue implements MessagingQueue, AsyncRequestHandler, MembershipListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Marshaller marshaller;
    private final ThreadPoolRequestProcessor threadPoolRequestProcessor;
    private final String appName;
    private final MessageDispatcher messageDispatcher;
    private final JChannel channel;
    private final AddressCache addressCache;
    private State state;
    private int nextIndex;

    protected AbstractMessagingQueue(String appName, Marshaller marshaller, String jgrpConfigFile,
                                     ThreadPoolRequestProcessor threadPoolRequestProcessor) throws Exception {
        this.appName = appName;
        this.marshaller = marshaller;
        this.threadPoolRequestProcessor = threadPoolRequestProcessor;
        this.channel = new JChannel(jgrpConfigFile);
        this.messageDispatcher = new MessageDispatcher();
        this.addressCache = new AddressCache();
        messageDispatcher.setRequestHandler(this);
        messageDispatcher.setMembershipListener(this);
        messageDispatcher.setChannel(channel);
        messageDispatcher.asyncDispatching(true);
        state = State.BOOT;
    }

    @Override
    public final void viewAccepted(View new_view) {
        addressCache.clean(new_view.getMembers());
    }

    @Override
    public final void suspect(Address suspected_mbr) {
        //no-op
    }

    @Override
    public final void block() {
        //no-op
    }

    @Override
    public final void unblock() {
        //no-op
    }

    @Override
    public void handle(Message request, Response response) throws Exception {
        Address from = request.getSrc();
        ReceivedBuffer buffer = new ReceivedBuffer(request.getBuffer());
        MessageType type = MessageType.from(buffer.readByte());
        if (type == MessageType.WORK_REQUEST) {
            if (!canHandleRequests()) {
                logger.error("Work request received but this queue cannot handle it!");
                throw new IllegalStateException("Cannot handle request because it does not have access to the cache");
            }
            threadPoolRequestProcessor.execute(buffer.readUTF(), response);
            return;
        }
        handleData(from, type, buffer, response);
    }

    @Override
    public Object handle(Message msg) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object sendRequest(String data, String localityHint, boolean sync) throws Exception {
        if (getState() != State.RUNNING) {
            logger.error("Tried to send a work request but the queue is not running. State=" + getState());
            throw new IllegalStateException("Messaging Queue was stopped!");
        }
        Address destination = locate(localityHint);
        if (destination == null) {
            logger.error("Trying to send a work request but not worker was found.");
            throw new IllegalStateException("Trying to send a work request but not worker was found.");
        } else if (logger.isTraceEnabled()) {
            logger.trace("Sending work request to " + destination);
        }
        SendBuffer sendBuffer = new SendBuffer();
        sendBuffer.writeByte(MessageType.WORK_REQUEST.type());
        sendBuffer.writeUTF(data);
        return send(destination, sendBuffer, sync);
    }

    @Override
    public final synchronized void init() throws Exception {
        if (getState() != State.BOOT) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot initialize again message queue. State=" + getState());
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing message queue...");
        }
        nextIndex = 0;
        channel.connect("ff-" + appName + "-messaging");
        messageDispatcher.start();
        state = State.RUNNING;
        innerInit();
        if (logger.isDebugEnabled()) {
            logger.debug("Message queue initialized!");
        }
    }

    @Override
    public final synchronized void shutdown() {
        if (getState() != State.RUNNING) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot shutdown again message queue. State=" + getState());
            }
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Shutting down message queue...");
        }
        innerShutdown();
        state = State.STOPPED;
        messageDispatcher.stop();
        channel.disconnect();
        channel.close();
        if (logger.isDebugEnabled()) {
            logger.debug("Message queue shut down!");
        }
    }

    protected synchronized final State getState() {
        return state;
    }

    protected final Address locate(String localityHint) throws Exception {
        initConsistentHashIfNeeded();
        Address address;
        ConsistentHash consistentHash = getConsistentHash();
        if (consistentHash == null) {
            //local mode
            address = localWorker();
            if (logger.isTraceEnabled()) {
                logger.trace("Locate owner for hint [" + localityHint + "]. Cache is local only: " + address);
            }
        } else if (localityHint == null || consistentHash.getNumSegments() == 1) {
            //full replicated or not locality hint.
            address = roundRobin(consistentHash);
            if (logger.isTraceEnabled()) {
                logger.trace("Locate owner for hint [" + localityHint + "]. Using round robin: " + address);
            }
        } else {
            address = toJGroupsAddress(consistentHash.locatePrimaryOwner(localityHint));
            if (logger.isTraceEnabled()) {
                logger.trace("Locate owner for hint [" + localityHint + "]. Primary owner: " + address);
            }

        }
        return address;
    }

    protected final Address roundRobin(ConsistentHash consistentHash) throws Exception {
        List<org.infinispan.remoting.transport.Address> members = consistentHash.getMembers();
        int index;
        synchronized (this) {
            nextIndex = (nextIndex + 1) % members.size();
            index = nextIndex;
        }
        return toJGroupsAddress(members.get(index));
    }

    protected abstract void innerInit();

    protected abstract void innerShutdown();

    protected abstract void initConsistentHashIfNeeded() throws Exception;

    protected abstract ConsistentHash getConsistentHash();

    protected abstract Address localWorker();

    protected abstract void handleData(Address from, MessageType type, ReceivedBuffer buffer, Response response)
            throws Exception;

    protected final Address localAddress() {
        return channel.getAddress();
    }

    protected final <T> T send(Address address, SendBuffer buffer, boolean sync) throws Exception {
        buffer.flush();
        if (logger.isTraceEnabled()) {
            logger.trace("Send " + (sync ? "sync" : "async") + " unicast message to " + address + " with " +
                    buffer.size() + " bytes.");
        }
        return messageDispatcher.sendMessage(createMessage(address, buffer),
                sync ? RequestOptions.SYNC() : RequestOptions.ASYNC());
    }

    protected final <T> RspList<T> broadcastRequest(SendBuffer buffer, boolean sync) throws Exception {
        buffer.flush();
        if (logger.isTraceEnabled()) {
            logger.trace("Send " + (sync ? "sync" : "async") + " broadcast message with " + buffer.size() + " bytes.");
        }
        return messageDispatcher.castMessage(null, createMessage(null, buffer),
                sync ? RequestOptions.SYNC() : RequestOptions.ASYNC());
    }

    protected final void reply(Response response, Object reply) {
        if (response != null) {
            response.send(reply, false);
        }
    }

    protected final org.jgroups.Address toJGroupsAddress(org.infinispan.remoting.transport.Address address)
            throws Exception {
        org.jgroups.Address jgrpAddress = addressCache.get(address);
        if (jgrpAddress == null) {
            addressCache.lock(address);
            try {
                jgrpAddress = addressCache.get(address);
                if (jgrpAddress != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Convert Infinispan address [" + address + "] to JGroups address [" +
                                jgrpAddress + "]");
                    }
                    return jgrpAddress;
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("Don't have JGroups address for Infinispan Address [" + address + "]. Making a request");
                }

                SendBuffer buffer = new SendBuffer();
                buffer.writeByte(MessageType.ADDRESS_REQUEST.type());
                buffer.writeByteArray(marshaller.objectToByteBuffer(address));
                RspList<Boolean> rspList = broadcastRequest(buffer, true);
                for (Map.Entry<org.jgroups.Address, Rsp<Boolean>> entry : rspList.entrySet()) {
                    if (entry.getValue().wasReceived() && entry.getValue().getValue() == Boolean.TRUE) {
                        addressCache.add(address, entry.getKey());
                        if (logger.isTraceEnabled()) {
                            logger.trace("Convert Infinispan address [" + address + "] to JGroups address [" +
                                    entry.getKey() + "]");
                        }
                        return entry.getKey();
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Don't have JGroups address for Infinispan Address [" + address +
                            "] and no member has replied positively");
                }
            } finally {
                addressCache.unlock(address);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Convert Infinispan address [" + address + "] to JGroups address [" + jgrpAddress + "]");
        }
        return jgrpAddress;
    }

    private Message createMessage(Address destinaton, SendBuffer buffer) throws IOException {
        buffer.flush();
        Message message = new Message(destinaton, buffer.toByteArray());
        return message.setFlag(Message.Flag.NO_TOTAL_ORDER, Message.Flag.OOB);
    }

    private boolean canHandleRequests() {
        return threadPoolRequestProcessor != null;
    }

    protected static enum State {
        BOOT, RUNNING, STOPPED
    }
}
