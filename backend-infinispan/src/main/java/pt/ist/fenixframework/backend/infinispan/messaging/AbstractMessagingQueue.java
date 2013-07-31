package pt.ist.fenixframework.backend.infinispan.messaging;

import org.infinispan.distribution.ch.ConsistentHash;
import org.infinispan.marshall.Marshaller;
import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.jmx.annotations.ManagedAttribute;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;
import pt.ist.fenixframework.messaging.MessagingQueue;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * {@link MessagingQueue} implementation to dispatch the requests in {@link pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd}.
 *
 * @author Pedro Ruivo
 * @since 2.8-cloudtm
 */
public abstract class AbstractMessagingQueue implements MessagingQueue, AsyncRequestHandler, MembershipListener {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Marshaller marshaller;
    private final ThreadPoolRequestProcessor threadPoolRequestProcessor;
    private final String appName;
    private final MessageDispatcher messageDispatcher;
    private final JChannel channel;
    private final AddressCache addressCache;
    private final ConcurrentMap<Address, Stats> globalStats;
    private State state;
    private int nextIndex;
    private volatile boolean primaryBackup;

    protected AbstractMessagingQueue(String appName, Marshaller marshaller, String jgrpConfigFile,
                                     ThreadPoolRequestProcessor threadPoolRequestProcessor) throws Exception {
        this.appName = appName;
        this.marshaller = marshaller;
        this.threadPoolRequestProcessor = threadPoolRequestProcessor;
        this.channel = new JChannel(jgrpConfigFile);
        this.messageDispatcher = new MessageDispatcher();
        this.addressCache = new AddressCache();
        this.globalStats = new ConcurrentHashMap<Address, Stats>();
        this.primaryBackup = false;
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
        switch (type) {
            case WORK_REQUEST:
                if (!canHandleRequests()) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Work request received but this queue cannot handle it!");
                    }
                    throw new IllegalStateException("Cannot handle request because it does not have access to the cache");
                }
                threadPoolRequestProcessor.execute(buffer.readUTF(), response);
                break;
            case SET_PB:
                primaryBackup = true;
                reply(response, null);
                break;
            case UNSET_PB:
                primaryBackup = false;
                reply(response, null);
                break;
            default:
                handleData(from, type, buffer, response);
                break;
        }
    }

    @Override
    public Object handle(Message msg) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Object sendRequest(String data, String localityHint, boolean sync, boolean write) throws Exception {
        if (getState() != State.RUNNING) {
            if (logger.isErrorEnabled()) {
                logger.error("Tried to send a work request but the queue is not running. State=" + getState());
            }
            throw new IllegalStateException("Messaging Queue was stopped!");
        }
        Address destination = locate(localityHint, write);
        if (destination == null) {
            logger.error("Trying to send a work request but not worker was found.");
            throw new IllegalStateException("Trying to send a work request but not worker was found.");
        } else if (logger.isTraceEnabled()) {
            logger.trace("Sending work request to " + destination);
        }
        SendBuffer sendBuffer = new SendBuffer();
        sendBuffer.writeByte(MessageType.WORK_REQUEST.type());
        sendBuffer.writeUTF(data);

        long end;
        long start = sync ? System.nanoTime() : 0;
        try {
            //wait for all the replies
            return send(destination, sendBuffer, sync ? new RequestOptions(ResponseMode.GET_ALL, 0) :
                    new RequestOptions(ResponseMode.GET_NONE, 0));
        } finally {
            end = sync ? System.nanoTime() : 0;
            Stats workerStats = new Stats();
            Stats existing = globalStats.putIfAbsent(destination, workerStats);
            if (existing != null) {
                workerStats = existing;
            }
            if (sync) {
                workerStats.addSync(end - start);
            } else {
                workerStats.addAsync();
            }
        }
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

    @ManagedAttribute(description = "Returns the workers members as a string.")
    public final List<String> getWorkersCluster() {
        ConsistentHash consistentHash = getConsistentHash();
        if (consistentHash != null) {
            ArrayList<String> members = new ArrayList<String>(consistentHash.getMembers().size());
            for (org.infinispan.remoting.transport.Address address : consistentHash.getMembers()) {
                members.add(address.toString());
            }
            return members;
        }
        Address localWorker = localWorker();
        if (localWorker != null) {
            return Arrays.asList(localWorker.toString());
        }
        return Arrays.asList("N/A");
    }

    @ManagedAttribute(description = "Returns the messaging members as a string. This involves workers and clients.")
    public final List<String> getMessagingCluster() {
        List<Address> jgroupsMembers = channel.getView().getMembers();
        ArrayList<String> members = new ArrayList<String>(jgroupsMembers.size());
        for (Address address : jgroupsMembers) {
            members.add(address.toString());
        }
        return members;
    }

    @ManagedAttribute(description = "Returns the load-balance mode.")
    public final String getMode() {
        ConsistentHash consistentHash = getConsistentHash();
        if (consistentHash != null) {
            return consistentHash.getNumSegments() == 1 ? "Round-robin" : "Dynamic";
        }
        if (localWorker() != null) {
            return "Single Worker";
        }
        return "Unknown";
    }

    @ManagedAttribute(description = "Translation table between Infinispan addresses to JGroups addresses")
    public final Map<String, String> getTranslationTable() {
        return addressCache.cacheAsString();
    }

    @ManagedAttribute(description = "Number of synchronous request made per worker.")
    public final Map<String, Integer> getSyncRequestPerWorker() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (Map.Entry<Address, Stats> entry : globalStats.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().sync());
        }
        return result;
    }

    @ManagedAttribute(description = "Number of asynchronous request made per worker.")
    public final Map<String, Integer> getAsyncRequestPerWorker() {
        Map<String, Integer> result = new HashMap<String, Integer>();
        for (Map.Entry<Address, Stats> entry : globalStats.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().async());
        }
        return result;
    }

    @ManagedAttribute(description = "Average service time per worker.")
    public final Map<String, Double> getAverageServiceTimePerWorker() {
        Map<String, Double> result = new HashMap<String, Double>();
        for (Map.Entry<Address, Stats> entry : globalStats.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().avgServiceTime());
        }
        return result;
    }

    @ManagedAttribute(description = "Total number of synchronous request made,")
    public final int getSyncRequest() {
        int sum = 0;
        for (Stats stats : globalStats.values()) {
            sum += stats.sync();
        }
        return sum;
    }

    @ManagedAttribute(description = "Total number of asynchronous request made.")
    public final int getAsyncRequest() {
        int sum = 0;
        for (Stats stats : globalStats.values()) {
            sum += stats.async();
        }
        return sum;
    }

    @ManagedAttribute(description = "Average service time.")
    public final double getAverageServiceTime() {
        double sum = 0;
        int count = 0;
        for (Stats stats : globalStats.values()) {
            sum += stats.avgServiceTime();
            count++;
        }
        return count == 0 ? 0 : sum / count;
    }

    @ManagedOperation(description = "Reset statistics")
    public final void reset() {
        globalStats.clear();
    }

    @Override
    public Map<String, String> printLocationInfo(Collection<String> localityHintsList) {
        if (localityHintsList == null || localityHintsList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, LocatedLocalityHint> intermediateResults = new HashMap<String, LocatedLocalityHint>();
        Map<String, String> result = new HashMap<String, String>();
        for (String hint : localityHintsList) {
            try {
                String owner = String.valueOf(locate(hint, true));
                LocatedLocalityHint locatedLocalityHint = intermediateResults.get(owner);
                if (locatedLocalityHint == null) {
                    locatedLocalityHint = new LocatedLocalityHint();
                    intermediateResults.put(owner, locatedLocalityHint);
                }
                locatedLocalityHint.localityHintList.add(hint);
            } catch (Throwable e) {
                //ops...
            }
        }
        for (Map.Entry<String, LocatedLocalityHint> entry : intermediateResults.entrySet()) {
            result.put(entry.getKey(), entry.getValue().prettyPrint());
        }
        return result;
    }

    protected synchronized final State getState() {
        return state;
    }

    protected final Address locate(String localityHint, boolean write) throws Exception {
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
        } else if (primaryBackup) {
            //full replicated or not locality hint.
            address = primayBackup(consistentHash, localityHint, write);
            if (logger.isTraceEnabled()) {
                logger.trace("Locate owner for hint [" + localityHint + "]. Using primary backup: " + address);
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
        return nextMember(consistentHash.getMembers());
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

    private Address nextMember(List<org.infinispan.remoting.transport.Address> members) throws Exception {
        if (members.size() == 0) {
            return toJGroupsAddress(members.get(0));
        }
        int index;
        synchronized (this) {
            nextIndex = (nextIndex + 1) % members.size();
            index = nextIndex;
        }
        return toJGroupsAddress(members.get(index));
    }

    private Address primayBackup(ConsistentHash consistentHash, String hint, boolean write) throws Exception {
        if (write || consistentHash.getMembers().size() == 0) {
            return toJGroupsAddress(consistentHash.getMembers().get(0));
        } else if (hint == null) {
            List<org.infinispan.remoting.transport.Address> backups = new ArrayList<org.infinispan.remoting.transport.Address>(consistentHash.getMembers());
            backups.remove(0);
            return nextMember(backups);
        }
        return toJGroupsAddress(consistentHash.locatePrimaryOwner(hint));
    }

    private <T> T send(Address address, SendBuffer buffer, RequestOptions requestOptions) throws Exception {
        buffer.flush();
        if (logger.isTraceEnabled()) {
            logger.trace("Send unicast message to " + address + " with " + buffer.size() + " bytes. " + requestOptions);
        }
        return messageDispatcher.sendMessage(createMessage(address, buffer), requestOptions);
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

    private class Stats {
        private int sync;
        private int async;
        private long serviceTime;

        public synchronized final void addSync(long duration) {
            sync++;
            serviceTime += duration;
        }

        public synchronized final void addAsync() {
            async++;
        }

        public synchronized final int sync() {
            return sync;
        }

        public synchronized final int async() {
            return async;
        }

        public synchronized final double avgServiceTime() {
            if (sync == 0) {
                return 0;
            }
            return TimeUnit.NANOSECONDS.toMillis(serviceTime) * 1.0 / sync;
        }
    }

    private class LocatedLocalityHint {
        private final List<String> localityHintList;

        private LocatedLocalityHint() {
            localityHintList = new ArrayList<String>(4);
        }

        public final String prettyPrint() {
            return localityHintList.size() + ": " + localityHintList;
        }
    }
}
