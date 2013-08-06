package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import eu.cloudtm.LocalityHints;

public class LeafNode extends LeafNode_Base {
    
    public LeafNode(LocalityHints hints) {
	super(hints);
	setEntries(new TreeMap<Comparable,Serializable>(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY));
    }
    
    private LeafNode(LocalityHints hints, TreeMap<Comparable,Serializable> entries) {
	super(hints);
	setEntries(entries);
    }

    private TreeMap<Comparable,Serializable> duplicateMap() {
        return new TreeMap<Comparable,Serializable>(getEntries());
    }

    public AbstractNode insert(Comparable key, Serializable value) {
	TreeMap<Comparable,Serializable> localMap = justInsert(key, value);

	if (localMap == null) {		// no insertion occurred
	    return null;	// insert will return false
	}
	if (localMap.size() <= BPlusTree.MAX_NUMBER_OF_ELEMENTS) { // it still fits :-)
	    return getRoot();
	} else { // must split this node
	    // find middle position
	    Comparable keyToSplit = findRightMiddlePosition(localMap.keySet());

	    // split node in two
	    LeafNode leftNode = new LeafNode(this.getLocalityHints(), new TreeMap<Comparable,Serializable>(localMap.headMap(keyToSplit)));
	    LeafNode rightNode = new LeafNode(this.getLocalityHints(), new TreeMap<Comparable,Serializable>(localMap.tailMap(keyToSplit)));
	    fixLeafNodesListAfterSplit(leftNode, rightNode);

	    // propagate split to parent
	    if (getParent() == null) {  // make new root node
		InnerNode newRoot = new InnerNode(this.getLocalityHints(), leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		// leftNode.parent = getParent();
		// rightNode.parent = getParent();
		return getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }
    
    private <T extends Comparable> Comparable findRightMiddlePosition(Collection<T> keys) {
	Iterator<T> keysIterator = keys.iterator();

	for (int i = 0; i < BPlusTree.LOWER_BOUND + 1; i++) {
	    keysIterator.next();
	}
	return keysIterator.next();
    }

    private TreeMap<Comparable,Serializable> justInsert(Comparable key, Serializable value) {
	TreeMap<Comparable,Serializable> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	Serializable currentValue = localEntries.get(key);
	// this check suffices because we do not allow null values
	if (currentValue == value) {
	    return null;
	} else {
	    TreeMap<Comparable,Serializable> newMap = duplicateMap();
	    newMap.put(key, value);
            setEntries(newMap);
	    return newMap;
	}
    }

    private void fixLeafNodesListAfterSplit(LeafNode leftNode, LeafNode rightNode) {
        leftNode.setPrevious(this.getPrevious());
        rightNode.setNext(this.getNext());
        leftNode.setNext(rightNode);
    }

    public AbstractNode remove(Comparable key) {
	TreeMap<Comparable,Serializable> localMap = justRemove(key);

	if (localMap == null) {
	    return null;	// remove will return false
	}
	if (getParent() == null) {
	    return this;
	} else {
	    // if the removed key was the first we need to replace it in some parent's index
	    Comparable replacementKey = getReplacementKeyIfNeeded(key);

	    if (localMap.size() < BPlusTree.LOWER_BOUND) {
		return getParent().underflowFromLeaf(key, replacementKey);
	    } else if (replacementKey != null) {
		return getParent().replaceDeletedKey(key, replacementKey);
	    } else {
		return getParent().getRoot();  // maybe a tiny faster than just getRoot() ?!
	    }
	}
    }

    private TreeMap<Comparable,Serializable> justRemove(Comparable key) {
	TreeMap<Comparable,Serializable> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	if (!localEntries.containsKey(key)) {
	    return null;
	} else {
	    TreeMap<Comparable,Serializable> newMap = duplicateMap();
	    newMap.remove(key);
            setEntries(newMap);
	    return newMap;
	}
    }

    // This method assumes that there is at least one more key (which is
    // always true if this is not the root node)
    private Comparable getReplacementKeyIfNeeded(Comparable deletedKey) {
	Comparable firstKey = this.getEntries().firstKey();
	if (BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY.compare(deletedKey, firstKey) < 0) {
	    return firstKey;
	} else {
	    return null; // null means that key does not need replacement
	}
    }

    Map.Entry<Comparable,Serializable> removeBiggestKeyValue() {
	TreeMap<Comparable,Serializable> newMap = duplicateMap();
	Map.Entry<Comparable,Serializable> lastEntry = newMap.pollLastEntry();
        setEntries(newMap);
	return lastEntry;
    }

    Map.Entry<Comparable,Serializable> removeSmallestKeyValue() {
	TreeMap<Comparable,Serializable> newMap = duplicateMap();
	Map.Entry<Comparable,Serializable> firstEntry = newMap.pollFirstEntry();
        setEntries(newMap);
	return firstEntry;
    }

    Comparable getSmallestKey() {
	return this.getEntries().firstKey();
    }

    void addKeyValue(Map.Entry keyValue) {
	TreeMap<Comparable,Serializable> newMap = duplicateMap();
	newMap.put((Comparable)keyValue.getKey(), (Serializable)keyValue.getValue());
        setEntries(newMap);
    }

    void mergeWithLeftNode(AbstractNode leftNode, Comparable splitKey) {
	LeafNode left = (LeafNode)leftNode; // this node does not know how to merge with another kind
	
	TreeMap<Comparable,Serializable> newMap = duplicateMap();
	newMap.putAll(left.getEntries());
        setEntries(newMap);

	LeafNode nodeBefore = left.getPrevious();

	this.setPrevious(nodeBefore);
	if (nodeBefore != null) {
	    nodeBefore.setNext(this);
	}

	// no need to update parents, because they are always the same for the two merging leaf nodes
	assert(this.getParent() == leftNode.getParent());
    }

    public Serializable get(Comparable key) {
	return this.getEntries().get(key);
    }
    
    public Serializable get(boolean forceMiss, Comparable key) {
	return this.getEntriesCached(forceMiss).get(key);
    }

    public Serializable getIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
    	    Iterator<Serializable> values = this.getEntries().values().iterator();
    	    for (int i = 0; i < index; i++) {
    	    	values.next();
    	    }
	    return values.next();
	} else {
	    LeafNode next = this.getNext();
	    if (next == null) {
		throw new IndexOutOfBoundsException();
	    }
	    return next.getIndex(index - shallowSize());
	}
    }

    public AbstractNode removeIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    Iterator<Comparable> keys = this.getEntries().keySet().iterator();
	    for (int i = 0; i < index; i++) {
		keys.next();
	    }
	    return this.remove(keys.next());
	} else {
	    LeafNode next = this.getNext();
	    if (next == null) {
		throw new IndexOutOfBoundsException();
	    }
	    return next.removeIndex(index - shallowSize());
	}
    }

    public boolean containsKey(Comparable key) {
	return this.getEntries().containsKey(key);
    }

    int shallowSize() {
	return this.getEntries().size();
    }

    public int size() {
	return this.getEntries().size();
    }

    @Override
    Iterator<? extends Comparable> keysIterator() {
        return new LeafNodeKeysIterator(this);
    }

    public Iterator<Serializable> iterator() {
	return new LeafNodeValuesIterator(this);
    }
    
    public Iterator<Serializable> iteratorCached(boolean forceMiss) {
	return new LeafNodeValuesCachedIterator(forceMiss, this);
    }

    protected abstract class GenericLeafNodeIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private LeafNode current;
	

	GenericLeafNodeIterator(LeafNode leafNode) {
            this.iterator = getInternalIterator(leafNode);
	    this.current = leafNode;
	}

        protected abstract Iterator<T> getInternalIterator(LeafNode leafNode);

	public boolean hasNext() {
	    if (this.iterator.hasNext()) {
		return true;
	    } else {
		return this.current.getNext() != null;
	    }
	}

        public T next() {
	    if (!this.iterator.hasNext()) {
		LeafNode nextNode = this.current.getNext();
		if (nextNode != null) {
		    this.current = nextNode;
		    this.iterator = getInternalIterator(this.current);
		} else {
		    throw new NoSuchElementException();
		}
	    }
	    return this.iterator.next();
	}

        public void remove() {
	    throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	}

    }

    private class LeafNodeValuesIterator extends GenericLeafNodeIterator<Serializable> {

	LeafNodeValuesIterator(LeafNode leafNode) {
            super(leafNode);
	}

        protected Iterator<Serializable> getInternalIterator(LeafNode leafNode) {
            return leafNode.getEntries().values().iterator();
        }

    }
    
    protected abstract class GenericLeafNodeCacheableIterator<T> implements Iterator<T> {
	private Iterator<T> iterator;
	private LeafNode current;
	private boolean forceMiss;


	GenericLeafNodeCacheableIterator(boolean forceMiss, LeafNode leafNode) {
	    this.forceMiss = forceMiss;
	    this.iterator = getInternalIterator(leafNode);
	    this.current = leafNode;
	}

	protected abstract Iterator<T> getInternalIterator(LeafNode leafNode);

	public boolean hasNext() {
	    if (this.iterator.hasNext()) {
		return true;
	    } else {
		return this.current.getNextCached(forceMiss) != null;
	    }
	}

	public T next() {
	    if (!this.iterator.hasNext()) {
		LeafNode nextNode = this.current.getNextCached(forceMiss);
		if (nextNode != null) {
		    this.current = nextNode;
		    this.iterator = getInternalIterator(this.current);
		} else {
		    throw new NoSuchElementException();
		}
	    }
	    return this.iterator.next();
	}

	public void remove() {
	    throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	}

    }
    
    private class LeafNodeValuesCachedIterator extends GenericLeafNodeCacheableIterator<Serializable> {

	LeafNodeValuesCachedIterator(boolean forceMiss, LeafNode leafNode) {
            super(forceMiss, leafNode);
	}

        protected Iterator<Serializable> getInternalIterator(LeafNode leafNode) {
            return leafNode.getEntriesCached(super.forceMiss).values().iterator();
        }

    }

    private class LeafNodeKeysIterator extends GenericLeafNodeIterator<Comparable> {

	LeafNodeKeysIterator(LeafNode leafNode) {
            super(leafNode);
	}

        protected Iterator<Comparable> getInternalIterator(LeafNode leafNode) {
            return leafNode.getEntries().keySet().iterator();
        }

    }

    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	StringBuilder str = new StringBuilder();
	str.append(BPlusTree.spaces(level));
	if (dumpNodeIds) {
	    str.append(this.getPrevious() + "<-[" + this + ": ");
	} else {
	    str.append("[: ");
	}

	for (Map.Entry<Comparable, Serializable> entry : this.getEntries().entrySet()) {
	    Comparable key = entry.getKey();
	    Serializable value = entry.getValue();
	    str.append("(" + key);
	    str.append(dumpKeysOnly ? ") " : "," + value + ") ");
	}
	if (dumpNodeIds) {
	    str.append("]->" + this.getNext() + " ^" + getParent() + "\n");
	} else {
	    str.append("]\n");
	}

	return str.toString();
    }

    @Override
    Collection<? extends Comparable> getKeys() {
	return this.getEntries().keySet();
    }

}