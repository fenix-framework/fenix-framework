package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Iterator;

import pt.ist.fenixframework.core.Externalization;
import eu.cloudtm.LocalityHints;

/** The keys comparison function should be consistent with equals. */
public abstract class AbstractNodeArray<T extends Serializable> extends AbstractNodeArray_Base implements Iterable {
    /* Node Interface */

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    abstract AbstractNodeArray insert(Comparable key, T value);

    /** Removes the element with the given key */
    abstract AbstractNodeArray remove(Comparable key);

    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    abstract T get(Comparable key);

    abstract T get(boolean forceMiss, Comparable key);
    
    /**
     * Returns the value at the given index
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    abstract T getIndex(int index);

    abstract Iterator iteratorCached(boolean forceMiss);
    
    /**
     * Returns the value that was removed from the given index
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
     */
    abstract AbstractNodeArray removeIndex(int index);

    /** Returns <code>true</code> if this map contains a mapping for the specified key. */
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

    public AbstractNodeArray() {
        this((LocalityHints) null);
    }

    public AbstractNodeArray(eu.cloudtm.LocalityHints hints) {
        super(hints);
    }

    AbstractNodeArray getRoot() {
        InnerNodeArray thisParent = this.getParent();
        return thisParent == null ? this : thisParent.getRoot();
    }

    abstract DoubleArray.KeyVal removeBiggestKeyValue();

    abstract DoubleArray.KeyVal removeSmallestKeyValue();

    abstract Comparable getSmallestKey();

    abstract void addKeyValue(DoubleArray.KeyVal keyValue);

    // merge elements from the left node into this node. smf: maybe LeafNodeArray can be a subclass of InnerNodeArray
    abstract void mergeWithLeftNode(AbstractNodeArray leftNode, Comparable splitKey);

    // the number of _elements_ in this node (not counting sub-nodes)
    abstract int shallowSize();

    public static Serializable externalizeArrays(DoubleArray array) {
        return new ArrayExternalization(array);
    }

    public static DoubleArray internalizeArrays(Serializable externalizedArray) {
        return ((ArrayExternalization) externalizedArray).toArray();
    }

    private static class ArrayExternalization implements Serializable {
        private static final long serialVersionUID = 1L;

        private final byte[] serializedArray;

        ArrayExternalization(DoubleArray array) {
            this.serializedArray = Externalization.externalizeSerializable(array);
        }

        DoubleArray toArray() {
            return (DoubleArray) Externalization.internalizeSerializable(serializedArray);
        }
    }

}
