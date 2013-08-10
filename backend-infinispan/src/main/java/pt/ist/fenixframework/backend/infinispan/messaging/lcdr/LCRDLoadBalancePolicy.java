package pt.ist.fenixframework.backend.infinispan.messaging.lcdr;

import org.infinispan.distribution.ch.ConsistentHash;
import org.jgroups.Address;
import pt.ist.fenixframework.backend.infinispan.InfinispanBackEnd;
import pt.ist.fenixframework.backend.infinispan.messaging.LoadBalancePolicy;
import pt.ist.fenixframework.backend.infinispan.messaging.ReceivedBuffer;
import pt.ist.fenixframework.backend.infinispan.messaging.SendBuffer;
import pt.ist.fenixframework.jmx.JmxUtil;
import pt.ist.fenixframework.jmx.annotations.MBean;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;

import java.io.IOException;
import java.util.*;

/**
 * Locality-catalyzer Request Dispatching
 *
 * @author Pedro Ruivo
 * @since 2.10
 */
@MBean(category = "loadbalance", description = "Locality-catalyzer Request Dispatching",
        objectName = "LCRDLoadBalancePolicy")
public class LCRDLoadBalancePolicy implements LoadBalancePolicy {

    private final Random random;
    private LoadBalanceTranslation translation;
    private LoadBalanceChannel channel;
    private volatile TransactionClassMapping transactionClassMapping;

    public LCRDLoadBalancePolicy() {
        random = new Random();
    }

    @Override
    public void init(LoadBalanceTranslation translation, LoadBalanceChannel channel, String appName) {
        this.translation = translation;
        this.channel = channel;
        JmxUtil.processInstance(this, appName, InfinispanBackEnd.BACKEND_NAME, null);
    }

    @Override
    public Iterator<Address> locate(ConsistentHash consistentHash, String hint, boolean primaryBackup, boolean write) {
        if (primaryBackup && write) {
            return new SingleAddressIterator(translation.translate(consistentHash.getMembers().get(0)));
        }
        Set<org.infinispan.remoting.transport.Address> orderedAddressSet =
                new LinkedHashSet<org.infinispan.remoting.transport.Address>();
        if (transactionClassMapping == null) {
            copyMembers(consistentHash, orderedAddressSet);
            return new AddressListIterator(translation, orderedAddressSet);
        }
        List<org.infinispan.remoting.transport.Address> members = consistentHash.getMembers();
        MemberInterval interval = transactionClassMapping.interval(hint, members.size());
        if (interval == null) {
            copyMembers(consistentHash, orderedAddressSet);
            return new AddressListIterator(translation, orderedAddressSet);
        }

        for (int i = interval.begin; i < interval.end; ++i) {
            orderedAddressSet.add(members.get(i));
        }

        copyMembers(consistentHash, orderedAddressSet);

        return new AddressListIterator(translation, orderedAddressSet);
    }

    @Override
    public void getState(SendBuffer buffer, boolean worker, boolean coordinator) throws IOException {
        if (coordinator) {
            buffer.writeBoolean(transactionClassMapping != null);
            if (transactionClassMapping != null) {
                transactionClassMapping.toBuffer(buffer);
            }
        }
    }

    @Override
    public void setState(ReceivedBuffer buffer) throws IOException {
        if (buffer != null) {
            if (buffer.readBoolean()) {
                transactionClassMapping = new TransactionClassMapping(buffer);
            }
        }
    }

    @Override
    public Object handle(ReceivedBuffer buffer) throws IOException {
        this.transactionClassMapping = new TransactionClassMapping(buffer);
        return null;
    }

    @ManagedOperation(description = "Updates the LCRD mappings")
    public final void updateMappings(Map<String, Integer> clusterMappings, Map<Integer, Float> clusterWeight) throws Exception {
        final float[] weight = new float[clusterWeight.size()];
        for (int i = 0; i < weight.length; ++i) {
            weight[i] = clusterWeight.get(i);
        }
        TransactionClassMapping temp = new TransactionClassMapping(weight, new Hashtable<String, Integer>(clusterMappings));
        SendBuffer buffer = new SendBuffer();
        temp.toBuffer(buffer);
        channel.broadcast(buffer, false);
        this.transactionClassMapping = temp;
    }

    private void copyMembers(ConsistentHash consistentHash, Set<org.infinispan.remoting.transport.Address> orderedSet) {
        List<org.infinispan.remoting.transport.Address> copy =
                new ArrayList<org.infinispan.remoting.transport.Address>(consistentHash.getMembers());
        shuffle(copy);
        orderedSet.addAll(copy);
    }

    private <T> void shuffle(List<T> list) {
        if (list.size() == 2) {
            if (random.nextInt(100) >= 50) {
                list.set(0, list.set(1, list.get(0)));
            }
        } else if (list.size() > 2) {
            Collections.shuffle(list, random);
        }
    }

    private class TransactionClassMapping {

        private final float[] clusterWeight;
        private final Map<String, Integer> clusterMapping;

        private TransactionClassMapping(float[] clusterWeight, Map<String, Integer> clusterMapping) {
            this.clusterWeight = clusterWeight;
            this.clusterMapping = clusterMapping;
        }

        private TransactionClassMapping(ReceivedBuffer buffer) throws IOException {
            int size = buffer.readInt();
            Map<String, Integer> map = new Hashtable<String, Integer>();
            for (int i = 0; i < size; ++i) {
                map.put(buffer.readUTF(), buffer.readInt());
            }
            clusterMapping = new Hashtable<String, Integer>(map);
            clusterWeight = new float[buffer.readInt()];
            for (int i = 0; i < clusterWeight.length; ++i) {
                clusterWeight[i] = buffer.readFloat();
            }
        }

        public final MemberInterval interval(String txClass, int numberOfNodes) {
            Integer clusterId = clusterMapping.get(txClass);
            if (clusterId == null) {
                return null;
            }
            float weightBefore = 0;
            for (int i = clusterId - 1; i >= 0; --i) {
                weightBefore += clusterWeight[i];
            }
            int begin = (int) (weightBefore * numberOfNodes);
            int end = (int) (begin + (clusterWeight[clusterId] * numberOfNodes));
            return new MemberInterval(begin, end);
        }

        public final void toBuffer(SendBuffer buffer) throws IOException {
            buffer.writeInt(clusterMapping.size());
            for (Map.Entry<String, Integer> entry : clusterMapping.entrySet()) {
                buffer.writeUTF(entry.getKey());
                buffer.writeInt(entry.getValue());
            }
            buffer.writeInt(clusterWeight.length);
            for (float weight : clusterWeight) {
                buffer.writeFloat(weight);
            }
        }

    }

    private class MemberInterval {
        private final int begin;
        private final int end;

        public MemberInterval(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }
    }


}
