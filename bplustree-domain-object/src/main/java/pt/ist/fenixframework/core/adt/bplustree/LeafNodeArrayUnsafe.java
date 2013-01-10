package pt.ist.fenixframework.core.adt.bplustree;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class LeafNodeArrayUnsafe extends LeafNodeArrayUnsafe_Base {
    private static final Logger logger = LoggerFactory.getLogger(LeafNodeArrayUnsafe.class);
    
    public LeafNodeArrayUnsafe() {
	setEntries(new DoubleArray<AbstractDomainObject>(AbstractDomainObject.class));
    }

    private LeafNodeArrayUnsafe(DoubleArray<AbstractDomainObject> entries) {
	setEntries(entries);
    }

    public AbstractNodeArrayUnsafe insert(Comparable key, AbstractDomainObject value) {
	DoubleArray<AbstractDomainObject> localArr = justInsert(key, value);

	if (localArr == null) {
	    return null;	// insert will return false
	}
	if (localArr.length() <= BPlusTreeArray.MAX_NUMBER_OF_ELEMENTS) { // it still fits :-)
	    return getRootUnsafe();
	} else { // must split this node
	    // find middle position
	    Comparable keyToSplit = localArr.findRightMiddlePosition();

	    // split node in two
	    LeafNodeArrayUnsafe leftNode = new LeafNodeArrayUnsafe(localArr.leftPart(BPlusTreeArray.LOWER_BOUND + 1));
	    LeafNodeArrayUnsafe rightNode = new LeafNodeArrayUnsafe(localArr.rightPart(BPlusTreeArray.LOWER_BOUND + 1));
	    fixLeafNodeArrayUnsafesListAfterSplit(leftNode, rightNode);

	    // propagate split to parent
	    if (getParent() == null) {  // make new root node
		InnerNodeArrayUnsafe newRoot = new InnerNodeArrayUnsafe(leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		return getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }
    
    private DoubleArray<AbstractDomainObject> justInsert(Comparable key, AbstractDomainObject value) {
        logger.trace("Getting 'entries' slot");
        DoubleArray<AbstractDomainObject> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	AbstractDomainObject currentValue = localEntries.get(key);
	if (currentValue != null && currentValue == value ) {
            logger.trace("Existing key. No change required");
	    return null;
	} else {
            logger.trace("Will add new entry. Must duplicate 'entries'.");
            DoubleArray<AbstractDomainObject> newArr = localEntries.addKeyValue(key, value);
            setEntries(newArr);
            return newArr;
	}
    }

    private void fixLeafNodeArrayUnsafesListAfterSplit(LeafNodeArrayUnsafe leftNode, LeafNodeArrayUnsafe rightNode) {
	leftNode.setPrevious(this.getPrevious());
	rightNode.setNext(this.getNext());
	leftNode.setNext(rightNode);
    }

    public AbstractNodeArrayUnsafe remove(Comparable key) {
	DoubleArray<AbstractDomainObject> localArr = justRemove(key);

	if (localArr == null) {
	    return null;	// remove will return false
	}
	if (getParentUnsafe() == null) {
	    return this;
	} else {
	    // if the removed key was the first we need to replace it in some parent's index
	    Comparable replacementKey = getReplacementKeyIfNeeded(key);

	    if (localArr.length() < BPlusTreeArray.LOWER_BOUND) {
		return getParent().underflowFromLeaf(key, replacementKey);
	    } else if (replacementKey != null) {
		return getParent().replaceDeletedKey(key, replacementKey);
	    } else {
		return getParentUnsafe().getRootUnsafe();  // maybe a tiny faster than just getRoot() ?!
	    }
	}
    }

    private DoubleArray<AbstractDomainObject> justRemove(Comparable key) {
	DoubleArray<AbstractDomainObject> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Value types must be immutable.
	if (!localEntries.containsKey(key)) {
	    return null;
	} else {
	    DoubleArray<AbstractDomainObject> newArr = localEntries.removeKey(key);
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

    DoubleArray<AbstractDomainObject>.KeyVal removeBiggestKeyValue() {
	DoubleArray<AbstractDomainObject> entries = this.getEntries();
	DoubleArray<AbstractDomainObject>.KeyVal lastEntry = entries.getBiggestKeyValue();
	setEntries(entries.removeBiggestKeyValue());
	return lastEntry;
    }

    DoubleArray<AbstractDomainObject>.KeyVal removeSmallestKeyValue() {
	DoubleArray<AbstractDomainObject> entries = this.getEntries();
	DoubleArray<AbstractDomainObject>.KeyVal firstEntry = entries.getSmallestKeyValue();
	setEntries(entries.removeSmallestKeyValue());
	return firstEntry;
    }

    Comparable getSmallestKey() {
	return this.getEntries().firstKey();
    }

    void addKeyValue(DoubleArray.KeyVal keyValue) {
	setEntries(this.getEntries().addKeyValue(keyValue));
    }

    void mergeWithLeftNode(AbstractNodeArrayUnsafe leftNode, Comparable splitKey) {
	LeafNodeArrayUnsafe left = (LeafNodeArrayUnsafe)leftNode; // this node does not know how to merge with another kind
        setEntries(getEntries().mergeWith(left.getEntries()));

	LeafNodeArrayUnsafe nodeBefore = left.getPrevious();
	this.setPrevious(nodeBefore);
	if (nodeBefore != null) {
	    nodeBefore.setNext(this);
	}

	// no need to update parents, because they are always the same for the two merging leaf nodes
	assert(this.getParent() == leftNode.getParent());
    }

    public AbstractDomainObject get(Comparable key) {
	return this.getEntries().get(key);
    }

    public AbstractDomainObject getIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    return this.getEntries().values[index];
	} else {
	    LeafNodeArrayUnsafe next = this.getNext();
	    if (next == null) {
		throw new IndexOutOfBoundsException();
	    }
	    return next.getIndex(index - shallowSize());
	}
    }

    public AbstractNodeArrayUnsafe removeIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    return remove(this.getEntries().keys[index]);
	} else {
	    LeafNodeArrayUnsafe next = this.getNext();
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

    public Iterator<AbstractDomainObject> iterator() {
	return new LeafNodeArrayUnsafeIterator(this);
    }

    private class LeafNodeArrayUnsafeIterator implements Iterator<AbstractDomainObject> {
	private int index;
	private AbstractDomainObject[] values;
	private LeafNodeArrayUnsafe current;
	

	LeafNodeArrayUnsafeIterator(LeafNodeArrayUnsafe LeafNodeArrayUnsafe) {
	    this.index = 0;
	    this.values = LeafNodeArrayUnsafe.getEntries().values;
	    this.current = LeafNodeArrayUnsafe;
	}

	public boolean hasNext() {
	    if (index < values.length) {
		return true;
	    } else {
		return this.current.getNext() != null;
	    }
	}

        public AbstractDomainObject next() {
	    if (index >= values.length) {
		LeafNodeArrayUnsafe nextNode = this.current.getNext();
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

    	DoubleArray<AbstractDomainObject> subNodes = this.getEntries();
	for (int i = 0; i < subNodes.length(); i++) {
    	    Comparable key = subNodes.keys[i];
    	    AbstractDomainObject value = subNodes.values[i];
	    str.append("(" + key);
	    str.append(dumpKeysOnly ? ") " : "," + value.getOid() + ") ");
	}
	if (dumpNodeIds) {
	    str.append("]->" + this.getNext() + " ^" + getParent() + "\n");
	} else {
	    str.append("]\n");
	}

	return str.toString();
    }

}
