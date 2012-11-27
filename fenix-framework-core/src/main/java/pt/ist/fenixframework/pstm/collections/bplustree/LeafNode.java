package pt.ist.fenixframework.pstm.collections.bplustree;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.NoDomainMetaObjects;

@NoDomainMetaObjects
public class LeafNode extends LeafNode_Base {
    
    public LeafNode() {
	setEntries(new TreeMap<Long,DomainObject>(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY));
    }

    private LeafNode(TreeMap<Long,DomainObject> entries) {
	setEntries(entries);
    }

    private TreeMap<Long,DomainObject> replacePreviousMap(LeafNode leafNode) {
	TreeMap<Long,DomainObject> newMap = new TreeMap<Long,DomainObject>(leafNode.getEntries());
	leafNode.setEntries(newMap);
	return newMap;
    }

    @Override
    public AbstractNode insert(Long key, DomainObject value) {
	TreeMap<Long,DomainObject> localMap = justInsert(key, value);

	if (localMap.size() <= BPlusTree.MAX_NUMBER_OF_ELEMENTS) { // it still fits :-)
	    return getRoot();
	} else { // must split this node
	    // find middle position
	    Long keyToSplit = findRightMiddlePosition(localMap.keySet());

	    // split node in two
	    LeafNode leftNode = new LeafNode(new TreeMap<Long,DomainObject>(localMap.headMap(keyToSplit)));
	    LeafNode rightNode = new LeafNode(new TreeMap<Long,DomainObject>(localMap.tailMap(keyToSplit)));
	    fixLeafNodesListAfterSplit(leftNode, rightNode);

	    // propagate split to parent
	    if (getParent() == null) {  // make new root node
		InnerNode newRoot = new InnerNode(leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		// leftNode.parent = getParent();
		// rightNode.parent = getParent();
		return getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }
    
    private Long findRightMiddlePosition(Collection<Long> keys) {
	Iterator<Long> keysIterator = keys.iterator();

	for (int i = 0; i < BPlusTree.LOWER_BOUND + 1; i++) {
	    keysIterator.next();
	}
	return keysIterator.next();
    }

    private TreeMap<Long,DomainObject> justInsert(Long key, DomainObject value) {
	TreeMap<Long,DomainObject> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Objects inside VBoxes must be immutable.
	DomainObject currentValue = localEntries.get(key);
	if (currentValue == value && localEntries.containsKey(key)) {
	    return localEntries;
	} else {
	    TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	    newMap.put(key, value);
	    return newMap;
	}
    }

    private void fixLeafNodesListAfterSplit(LeafNode leftNode, LeafNode rightNode) {
	leftNode.setPrevious(this.getPrevious());
	rightNode.setNext(this.getNext());
	leftNode.setNext(rightNode);
    }

    @Override
    public AbstractNode remove(Long key) {
	TreeMap<Long,DomainObject> localMap = justRemove(key);

	if (getParent() == null) {
	    return this;
	} else {
	    // if the removed key was the first we need to replace it in some parent's index
	    Long replacementKey = getReplacementKeyIfNeeded(key);

	    if (localMap.size() < BPlusTree.LOWER_BOUND) {
		return getParent().underflowFromLeaf(key, replacementKey);
	    } else if (replacementKey != null) {
		return getParent().replaceDeletedKey(key, replacementKey);
	    } else {
		return getParent().getRoot();  // maybe a tiny faster than just getRoot() ?!
	    }
	}
    }

    @Override
    void delete() {
	removeNext();
	removePrevious();
	removeParent();
	deleteDomainObject();
    }

    private TreeMap<Long,DomainObject> justRemove(Long key) {
	TreeMap<Long,DomainObject> localEntries = this.getEntries();

	// this test is performed because we need to return a new structure in
	// case an update occurs.  Objects inside VBoxes must be immutable.
	if (!localEntries.containsKey(key)) {
	    return localEntries;
	} else {
	    TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	    newMap.remove(key);
	    return newMap;
	}
    }

    // This method assumes that there is at least one more key (which is
    // always true if this is not the root node)
    private Long getReplacementKeyIfNeeded(Long deletedKey) {
	Long firstKey = this.getEntries().firstKey();
	if (deletedKey.compareTo(firstKey) < 0) {
	    return firstKey;
	} else {
	    return null; // null means that key does not need replacement
	}
    }

    @Override
    Map.Entry<Long,DomainObject> removeBiggestKeyValue() {
	TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	Map.Entry<Long,DomainObject> lastEntry = newMap.pollLastEntry();
	return lastEntry;
    }

    @Override
    Map.Entry<Long,DomainObject> removeSmallestKeyValue() {
	TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	Map.Entry<Long,DomainObject> firstEntry = newMap.pollFirstEntry();
	return firstEntry;
    }

    @Override
    Long getSmallestKey() {
	return this.getEntries().firstKey();
    }

    @Override
    void addKeyValue(Map.Entry keyValue) {
	TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	newMap.put((Long)keyValue.getKey(), (DomainObject)keyValue.getValue());
    }

    @Override
    void mergeWithLeftNode(AbstractNode leftNode, Long splitKey) {
	LeafNode left = (LeafNode)leftNode; // this node does not know how to merge with another kind
	
	TreeMap<Long,DomainObject> newMap = replacePreviousMap(this);
	newMap.putAll(left.getEntries());

	LeafNode nodeBefore = left.getPrevious();

	this.setPrevious(nodeBefore);
	if (nodeBefore != null) {
	    nodeBefore.setNext(this);
	}

	// no need to update parents, because they are always the same for the two merging leaf nodes
	assert(this.getParent() == leftNode.getParent());
    }

    @Override
    public DomainObject get(Long key) {
	return this.getEntries().get(key);
    }

    @Override
    public DomainObject getIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
    	    Iterator<DomainObject> values = this.getEntries().values().iterator();
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

    @Override
    public AbstractNode removeIndex(int index) {
	if (index < 0) {
	    throw new IndexOutOfBoundsException();
	}

	if (index < shallowSize()) { // the required position is here
	    Iterator<Long> keys = this.getEntries().keySet().iterator();
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

    @Override
    public boolean containsKey(Long key) {
	return this.getEntries().containsKey(key);
    }

    @Override
    int shallowSize() {
	return this.getEntries().size();
    }

    @Override
    public int size() {
	return this.getEntries().size();
    }

    @Override
    public Iterator<DomainObject> iterator() {
	return new LeafNodeIterator(this);
    }

    private class LeafNodeIterator implements Iterator<DomainObject> {
	private Iterator<DomainObject> iterator;
	private LeafNode current;
	

	LeafNodeIterator(LeafNode leafNode) {
	    this.iterator = leafNode.getEntries().values().iterator();
	    this.current = leafNode;
	}

	@Override
	public boolean hasNext() {
	    if (this.iterator.hasNext()) {
		return true;
	    } else {
		return this.current.getNext() != null;
	    }
	}

        @Override
	public DomainObject next() {
	    if (!this.iterator.hasNext()) {
		LeafNode nextNode = this.current.getNext();
		if (nextNode != null) {
		    this.current = nextNode;
		    this.iterator = this.current.getEntries().values().iterator();
		} else {
		    throw new NoSuchElementException();
		}
	    }
	    return this.iterator.next();
	}

        @Override
	public void remove() {
	    throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	}

    }

    @Override
    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	StringBuilder str = new StringBuilder();
	str.append(BPlusTree.spaces(level));
	if (dumpNodeIds) {
	    str.append(this.getPrevious() + "<-[" + this + ": ");
	} else {
	    str.append("[: ");
	}

	for (Map.Entry<Long, DomainObject> entry : this.getEntries().entrySet()) {
	    Long key = entry.getKey();
	    DomainObject value = entry.getValue();
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