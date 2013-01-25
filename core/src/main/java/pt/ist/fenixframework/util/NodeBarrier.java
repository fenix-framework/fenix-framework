package pt.ist.fenixframework.util;

import org.jgroups.*;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Behaves as synchronization point between nodes running Fenix Framework
 *
 * @author Pedro Ruivo
 * @since 2.1
 */
public class NodeBarrier extends ReceiverAdapter {

    private final JChannel barrierChannel;
    private final ConcurrentMap<String, Sync> barriers;

    public NodeBarrier(String config) throws Exception {
        barrierChannel = new JChannel(config);
        barrierChannel.setReceiver(this);
        barriers = new ConcurrentHashMap<String, Sync>();
    }

    private synchronized void connectIfNeeded() throws Exception {
        if (!barrierChannel.isConnected()) {
            barrierChannel.connect("ff-barrier");
        }
    }

    /**
     * Blocks the thread execution until {@param expectedMembers} nodes has reached this {@param barrierName}
     *
     * @param barrierName     the barrier name
     * @param expectedMembers the number of expected members
     * @throws Exception if this node has failed to send the barrier synchronization message or it was
     *                   interrupted while waiting
     */
    public final void blockUntil(String barrierName, int expectedMembers) throws Exception {
        connectIfNeeded();
        sendBarrierAcquired(barrierName);
        getOrAdd(barrierName).blockUntil(expectedMembers);
    }

    private void sendBarrierAcquired(String barrierName) throws Exception {
        barrierChannel.send(null, barrierName == null ? new byte[0] : barrierName.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * shutdowns this barrier
     */
    public final void shutdown() {
        barrierChannel.close();
        barriers.clear();
    }

    private Sync getOrAdd(String barrierName) {
        Sync sync = barriers.get(barrierName);
        if (sync == null) {
            sync = new Sync();
            Sync old = barriers.putIfAbsent(barrierName, sync);
            if (old != null) {
                sync = old;
            }
        }
        return sync;
    }

    @Override
    public void receive(Message msg) {
        super.receive(msg);
        String barrierName = new String(msg.getBuffer());
        getOrAdd(barrierName).addMember(msg.getSrc());
    }

    @Override
    public void viewAccepted(View view) {
        for (Map.Entry<String, Sync> barrier : barriers.entrySet()) {
            barrier.getValue().notifyNewView(barrier.getKey(), view);
        }
    }

    private class Sync {
        private final Set<Address> membersInBarrier;

        private Sync() {
            this.membersInBarrier = new HashSet<Address>();
        }

        public synchronized final void addMember(Address from) {
            if (membersInBarrier.add(from)) {
                notify();
            }
        }

        public synchronized final void blockUntil(int expectedMembers) throws InterruptedException {
            membersInBarrier.add(barrierChannel.getAddress());
            while (membersInBarrier.size() < expectedMembers) {
                wait();
            }
        }

        //new members list
        public synchronized void notifyNewView(String barrierName, View view) {
            if (membersInBarrier.containsAll(view.getMembers())) {
                //no new node
                return;
            }
            if (membersInBarrier.contains(barrierChannel.getAddress())) {
                try {
                    sendBarrierAcquired(barrierName);
                } catch (Exception e) {
                    //ignored
                }
            }
        }
    }

}
