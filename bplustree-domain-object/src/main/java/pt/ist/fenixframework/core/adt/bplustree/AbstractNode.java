package pt.ist.fenixframework.core.adt.bplustree;

import java.io.Serializable;
// import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.TreeMap;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.core.AbstractDomainObject;
import pt.ist.fenixframework.core.Externalization;

/** The keys comparison function should be consistent with equals. */
public abstract class AbstractNode<T extends AbstractDomainObject> extends AbstractNode_Base implements Iterable {
    /* Node Interface */

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    abstract AbstractNode insert(Comparable key, T value);
    /** Removes the element with the given key */
    abstract AbstractNode remove(Comparable key);
    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    abstract T get(Comparable key);
    /** Returns the value at the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract T getIndex(int index);
    /** Returns the value that was removed from  the given index 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size()) */
    abstract AbstractNode removeIndex(int index);
    /** Returns <code>true</code> if this map contains a mapping for the specified key.  */
    abstract boolean containsKey(Comparable key);
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

    abstract Map.Entry<Comparable,T> removeBiggestKeyValue();
    abstract Map.Entry<Comparable,T> removeSmallestKeyValue();
    abstract Comparable getSmallestKey();
    abstract void addKeyValue(Map.Entry keyValue);
    // merge elements from the left node into this node. smf: maybe LeafNode can be a subclass of InnerNode
    abstract void mergeWithLeftNode(AbstractNode leftNode, Comparable splitKey);
    // the number of _elements_ in this node (not counting sub-nodes)
    abstract int shallowSize();

    public static Serializable /*byte[]*/ externalizeTreeMap(TreeMap treeMap) {
        // return Externalization.externalizeObject(new TreeMapExternalization(treeMap));
        return new TreeMapExternalization(treeMap);
        // return treeMap;
    }

    public static TreeMap internalizeTreeMap(Serializable/*byte[]*/ externalizedTreeMap) {
        // TreeMapExternalization treeMapExternalization = Externalization.internalizeObject(externalizedTreeMap);
        return ((TreeMapExternalization)externalizedTreeMap).toTreeMap();
        // return (TreeMap)externalizedTreeMap;
    }

    private static class TreeMapExternalization implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] serializedTreeMap;

        TreeMapExternalization(TreeMap<Comparable,? extends Serializable> treeMap) {
            this.serializedTreeMap = Externalization.externalizeSerializable(treeMap);
        }

        TreeMap toTreeMap() {
            return (TreeMap)Externalization.internalizeSerializable(serializedTreeMap);
        }
    }

    ///////////////////////////////////////////////////////////////////////
    //  Buggy implementation because not all Comparables are OIDs.  In the InnerNode instances the
    //  last is an instance of LAST_KEY.

    // private static class TreeMapExternalization implements Serializable {
    //     private static final long serialVersionUID = 1L;

    //     private Comparable[] keyOids;

    //     TreeMapExternalization(TreeMap<Comparable,? extends AbstractDomainObject> treeMap) {
    //         int size = treeMap.size();
    //         this.keyOids = new Comparable[size];

    //         int i = 0;
    //         for (Map.Entry<Comparable,? extends AbstractDomainObject> entry : treeMap.entrySet()) {
    //     	this.keyOids[i] = entry.getKey();
    //     	i++;
    //         }
    //     }

    //     TreeMap toTreeMap() {
    //         TreeMap treeMap = new TreeMap(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY);

    //         for (int i = 0; i < this.keyOids.length; i++) {
    //     	Comparable key = this.keyOids[i];
    //     	treeMap.put(key, FenixFramework.getConfig().getBackEnd().fromOid(key));
    //         }
    //         return treeMap;
    //     }
    // }
}
