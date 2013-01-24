package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafNodeArrayShadow extends LeafNodeArrayShadow_Base {
    private static final Logger logger = LoggerFactory.getLogger(LeafNodeArrayShadow.class);
    
    public LeafNodeArrayShadow() {
	setEntries(new DoubleArray<Serializable>(Serializable.class));
    }

    private LeafNodeArrayShadow(DoubleArray<Serializable> entries) {
	setEntries(entries);
    }

    public AbstractNodeArrayShadow insert(Comparable key, Serializable value) {
	DoubleArray<Serializable> localArr = justInsert(key, value);

	if (localArr == null) {
	    return null;	// insert will return false
	}
	if (localArr.length() <= BPlusTreeArray.MAX_NUMBER_OF_ELEMENTS) { // it still fits :-)
	    return getRootShadow();
	} else { // must split this node
	    // find middle position
	    Comparable keyToSplit = localArr.findRightMiddlePosition();

	    // split node in two
	    LeafNodeArrayShadow leftNode = new LeafNodeArrayShadow(localArr.leftPart(BPlusTreeArray.LOWER_BOUND + 1));
	    LeafNodeArrayShadow rightNode = new LeafNodeArrayShadow(localArr.rightPart(BPlusTreeArray.LOWER_BOUND + 1));
	    fixLeafNodeArrayShadowsListAfterSplit(leftNode, rightNode);

	    // propagate split to parent
	    if (getParent() == null) {  // make new root node
		InnerNodeArrayShadow newRoot = new InnerNodeArrayShadow(leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		return getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }
    
    private DoubleArray<Serializable> justInsert(Comparable key, Serializable value) {
        logger.trace("Getting 'entries' slot");
        DoubleArray<Serializable> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	Serializable currentValue = localEntries.get(key);
	if (currentValue != null && currentValue == value ) {
            logger.trace("Existing key. No change required");
	    return null;
	} else {
            logger.trace("Will add new entry. Must duplicate 'entries'.");
            DoubleArray<Serializable> newArr = localEntries.addKeyValue(key, value);
            setEntries(newArr);
            return newArr;
	}
    }

    private void fixLeafNodeArrayShadowsListAfterSplit(LeafNodeArrayShadow leftNode, LeafNodeArrayShadow rightNode) {
	leftNode.setPrevious(this.getPrevious());
	rightNode.setNext(this.getNext());
	leftNode.setNext(rightNode);
    }

    public AbstractNodeArrayShadow remove(Comparable key) {
	DoubleArray<Serializable> localArr = justRemove(key);

	if (localArr == null) {
	    return null;	// remove will return false
	}
	if (getParentShadow() == null) {
	    return this;
	} else {
	    // if the removed key was the first we need to replace it in some parent's index
	    Comparable replacementKey = getReplacementKeyIfNeeded(key);

	    if (localArr.length() < BPlusTreeArray.LOWER_BOUND) {
		return getParent().underflowFromLeaf(key, replacementKey);
	    } else if (replacementKey != null) {
		return getParent().replaceDeletedKey(key, replacementKey);
	    } else {
		return getParentShadow().getRootShadow();  // maybe a tiny faster than just getRoot() ?!
	    }
	}
    }

    private DoubleArray<Serializable> justRemove(Comparable key) {
	DoubleArray<Serializable> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	if (!localEntries.containsKey(key)) {
	    return null;
	} else {
	    DoubleArray<Serializable> newArr = localEntries.removeKey(key);
	    setEntries(newArr);
	    return newArr;
	}
    }

    // This method assumes that there is at least one more key (which is
    // always true if this is not the root node)
    private Comparable getReplacementKeyIfNeeded(Comparable deletedKey) {
	Comparable firstKey = this.getEntries().firstKey();
	if (BPlusTreeArray.COMPARATOR_SUPPORTING_LAST_KEY.compare(deletedKey, firstKey) < 0) {
	    return firstKey;
	} else {
	    return null; // null means that key does not need replacement
	}
    }

    DoubleArray<Serializable>.KeyVal removeBiggestKeyValue() {
	DoubleArray<Serializable> entries = this.getEntries();
	DoubleArray<Serializable>.KeyVal lastEntry = entries.getBiggestKeyValue();
	setEntries(entries.removeBiggestKeyValue());
	return lastEntry;
    }

    DoubleArray<Serializable>.KeyVal removeSmallestKeyValue() {
	DoubleArray<Serializable> entries = this.getEntries();
	DoubleArray<Serializable>.KeyVal firstEntry = entries.getSmallestKeyValue();
	setEntries(entries.removeSmallestKeyValue());
	return firstEntry;
    }

    Comparable getSmallestKey() {
	return this.getEntries().firstKey();
    }

    void addKeyValue(DoubleArray.KeyVal keyValue) {
	setEntries(this.getEntries().addKeyValue(keyValue));
    }

    void mergeWithLeftNode(AbstractNodeArrayShadow leftNode, Comparable splitKey) {
	LeafNodeArrayShadow left = (LeafNodeArrayShadow)leftNode; // this node does not know how to merge with another kind
        setEntries(getEntries().mergeWith(left.getEntries()));

	LeafNodeArrayShadow nodeBefore = left.getPrevious();
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

    public Serializable getIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    return this.getEntries().values[index];
	} else {
	    LeafNodeArrayShadow next = this.getNext();
	    if (next == null) {
		throw new IndexOutOfBoundsException();
	    }
	    return next.getIndex(index - shallowSize());
	}
    }

    public AbstractNodeArrayShadow removeIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    return remove(this.getEntries().keys[index]);
	} else {
	    LeafNodeArrayShadow next = this.getNext();
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
	return this.getEntries().length();
    }

    public int size() {
	return this.getEntries().length();
    }

    public Iterator<Serializable> iterator() {
	return new LeafNodeArrayShadowIterator(this);
    }

    private class LeafNodeArrayShadowIterator implements Iterator<Serializable> {
	private int index;
	private Serializable[] values;
	private LeafNodeArrayShadow current;
	

	LeafNodeArrayShadowIterator(LeafNodeArrayShadow LeafNodeArrayShadow) {
	    this.index = 0;
	    this.values = LeafNodeArrayShadow.getEntries().values;
	    this.current = LeafNodeArrayShadow;
	}

	public boolean hasNext() {
	    if (index < values.length) {
		return true;
	    } else {
		return this.current.getNext() != null;
	    }
	}

        public Serializable next() {
	    if (index >= values.length) {
		LeafNodeArrayShadow nextNode = this.current.getNext();
		if (nextNode != null) {
		    this.current = nextNode;
		    this.index = 0;
		    this.values = this.current.getEntries().values;
		} else {
		    throw new NoSuchElementException();
		}
	    }
	    index++;
	    return values[index - 1];
	}

        public void remove() {
	    throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	}

    }

    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	StringBuilder str = new StringBuilder();
	str.append(BPlusTreeArray.spaces(level));
	if (dumpNodeIds) {
	    str.append(this.getPrevious() + "<-[" + this + ": ");
	} else {
	    str.append("[: ");
	}

    	DoubleArray<Serializable> subNodes = this.getEntries();
	for (int i = 0; i < subNodes.length(); i++) {
    	    Comparable key = subNodes.keys[i];
    	    Serializable value = subNodes.values[i];
	    str.append("(" + value);
	    str.append(dumpKeysOnly ? ") " : "," + key + ") ");
	}
	if (dumpNodeIds) {
	    str.append("]->" + this.getNext() + " ^" + getParent() + "\n");
	} else {
	    str.append("]\n");
	}

	return str.toString();
    }

}
