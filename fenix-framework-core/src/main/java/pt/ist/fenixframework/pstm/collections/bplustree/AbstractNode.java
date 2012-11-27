package pt.ist.fenixframework.pstm.collections.bplustree;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.Externalization;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.NoDomainMetaObjects;

/** The keys comparison function should be consistent with equals. */
@NoDomainMetaObjects
public abstract class AbstractNode<T extends DomainObject> extends AbstractNode_Base implements Iterable {
    /* Node Interface */

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    abstract AbstractNode insert(Long key, T value);
    /** Removes the element with the given key */
    abstract AbstractNode remove(Long key);
    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    abstract T get(Long key);
    /** Returns the value at the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract T getIndex(int index);
    /** Returns the value that was removed from  the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract AbstractNode removeIndex(int index);
    /** Returns <code>true</code> if this map contains a mapping for the specified key.  */
    abstract boolean containsKey(Long key);
    /** Returns the number os key-value mappings in this map */
    abstract int size();

    abstract String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds);


    /* **** Uncomment the following to support pretty printing of nodes **** */
    
    // static final AtomicInteger GLOBAL_COUNTER = new AtomicInteger(0);
    // protected int counter = GLOBAL_COUNTER.getAndIncrement();
    // public String toString() {
    // 	return "" + counter;
    // }

    /* *********** */

    
    public  AbstractNode() {
        super();
    }

    AbstractNode getRoot() {
	InnerNode thisParent = this.getParent();
	return thisParent == null ? this : thisParent.getRoot();
    }

    /**
     * Deletes this <code>AbstractNode</code>, and any other child
     * {@link AbstractNode}s. Does not delete any {@link DomainObject} linked by
     * the Node.
     */
    abstract void delete();

    abstract Map.Entry<Long,T> removeBiggestKeyValue();
    abstract Map.Entry<Long,T> removeSmallestKeyValue();
    abstract Long getSmallestKey();
    abstract void addKeyValue(Map.Entry keyValue);
    // merge elements from the left node into this node. smf: maybe LeafNode can be a subclass of InnerNode
    abstract void mergeWithLeftNode(AbstractNode leftNode, Long splitKey);
    // the number of _elements_ in this node (not counting sub-nodes)
    abstract int shallowSize();

    public static byte[] externalizeTreeMap(TreeMap treeMap) {
	return Externalization.externalizeObject(new TreeMapExternalization(treeMap));
    }

    public static TreeMap internalizeTreeMap(byte[] externalizedTreeMap) {
	TreeMapExternalization treeMapExternalization = Externalization.internalizeObject(externalizedTreeMap);

	return treeMapExternalization.toTreeMap();
    }

    private static class TreeMapExternalization implements Serializable {
	private static final long serialVersionUID = 1L;

	private final long[] keyOids;
	private final long[] valueOids;

	TreeMapExternalization(TreeMap treeMap) {
	    int size = treeMap.size();
	    this.keyOids = new long[size];
	    this.valueOids = new long[size];

	    int i = 0;
	    for (Map.Entry entry : (java.util.Set<Map.Entry>)treeMap.entrySet()) {
		this.keyOids[i] = (Long)entry.getKey();
		Object value = entry.getValue();
		this.valueOids[i] = (value == null ? -1 : ((DomainObject)value).getOid());
		i++;
	    }
	}

	TreeMap toTreeMap() {
	    TreeMap treeMap = new TreeMap(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY);

	    for (int i = 0; i < this.keyOids.length; i++) {
		long value = this.keyOids[i];
		treeMap.put(this.keyOids[i], (value == -1 ? null : AbstractDomainObject.fromOID(this.valueOids[i])));
	    }
	    return treeMap;
	}
    }
}
