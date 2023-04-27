package pt.ist.fenixframework;

import java.util.HashMap;
import java.util.Map;

import jvstm.PerTxBox;

/**
 * A auxiliary object to keep the size slot for a {@link DomainBPlusTreeJVSTM}.
 * 
 * To avoid write conflicts, the writing to this slot is done with the help of PerTxBoxes.
 * This delays the read of the size box until after the transaction validation.
 * 
 * Thus, two transactions can add elements to different LeafNodes of the same tree.
 * Although these transactions will both increment the same size slot, they will not conflict.
 * 
 * This auxiliary object is necessary, because of the one-box layout used in the JVSTM+OJB backend.
 * If the size slot was declared directly in the {@link DomainBPlusTreeJVSTM}, the read of the box
 * would occur as soon as a transaction would get the root node to insert/remove elements.
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 */
@NoDomainMetaObjects
public class DomainBPlusTreeData extends DomainBPlusTreeData_Base {

    public DomainBPlusTreeData() {
        super();
        super.setSize(0);
    }

    /**
     * The size difference to increment (or decrement, if negative).
     * This PerTxBox needs to be static, and store a map with different values for each object.
     * This is to keep different threads from initializing and editing the object concurrently, which can cause inconsistencies.
     */
    private static final PerTxBox<Map<DomainBPlusTreeData, Integer>> sizeDiffMap =
            new PerTxBox<Map<DomainBPlusTreeData, Integer>>(null) {
                @Override
                public void commit(Map<DomainBPlusTreeData, Integer> diffMap) {
                    if (diffMap != null) {
                        for (DomainBPlusTreeData data : diffMap.keySet()) {
                            data.consolidateSize();
                        }
                    }
                }
            };

    private static Map<DomainBPlusTreeData, Integer> getAndSetSizeDiffMap() {
        if (sizeDiffMap.get() == null) {
            sizeDiffMap.put(new HashMap<DomainBPlusTreeData, Integer>());
        }
        return sizeDiffMap.get();
    }

    private Integer getSizeDiff() {
        if ((sizeDiffMap.get() == null) || (sizeDiffMap.get().get(this) == null)) {
            return 0;
        }
        return sizeDiffMap.get().get(this);
    }

    private void consolidateSize() {
        super.setSize(getSize());
    }

    protected void incSize() {
        getAndSetSizeDiffMap().put(this, getSizeDiff() + 1);
    }

    protected void decSize() {
        getAndSetSizeDiffMap().put(this, getSizeDiff() - 1);
    }

    @Override
    public int getSize() {
        return super.getSize() + getSizeDiff();
    }

    @Override
    public void setSize(int size) {
        throw new UnsupportedOperationException("Cannot directly change the size of a DomainBPlusTree");
    }

    @Override
    public void setTree(DomainBPlusTreeJVSTM tree) {
        throw new UnsupportedOperationException("The DomainBPlusTreeData cannot be changed.");
    }

    protected void delete() {
        super.setTree(null);
        deleteDomainObject();
    }
}
