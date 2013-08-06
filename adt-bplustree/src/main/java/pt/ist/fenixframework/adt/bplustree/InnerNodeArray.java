package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Iterator;

/** 
 * Inner node of a B+-Tree.  These nodes do not contain elements.  They only
 * contain M keys (ordered) and M+1 sub-nodes (M > 0).  The n-th sub-node will
 * contain elements whose keys are all less than the n-th key, except for the
 * last sub-node (L) which will contain elements whose keys will be greater
 * than or equal to the M-th key.
 */
public class InnerNodeArray extends InnerNodeArray_Base {

    private InnerNodeArray() {
	super();
    }

    InnerNodeArray(AbstractNodeArray leftNode, AbstractNodeArray rightNode, Comparable splitKey) {
	setSubNodes(new DoubleArray<AbstractNodeArray>(AbstractNodeArray.class, splitKey, leftNode, rightNode));
	leftNode.setParent(this);
	rightNode.setParent(this);
    }

    private InnerNodeArray(DoubleArray<AbstractNodeArray> subNodes) {
	setSubNodes(subNodes);
	for (int i = 0; i < subNodes.length(); i++) { // smf: either don't do this or don't setParent when making new
	    subNodes.values[i].setParent(this);
	}
    }

    @Override
    public AbstractNodeArray insert(Comparable key, Serializable value) {
	return findSubNode(key).insert(key, value);
    }

    // this method is invoked when a node in the next depth level got full, it
    // was split and now needs to pass a new key to its parent (this)
    AbstractNodeArray rebase(AbstractNodeArray subLeftNode, AbstractNodeArray subRightNode, Comparable middleKey) {
	DoubleArray<AbstractNodeArray> newArr = justInsertUpdatingParentRelation(middleKey, subLeftNode, subRightNode);
	if (newArr.length() <= BPlusTreeArray.MAX_NUMBER_OF_ELEMENTS) { // this node can accommodate the new split
	    return getRoot();
	} else { // must split this node
	    // find middle position (key to move up amd sub-node to move left)
	    Comparable keyToSplit = newArr.keys[BPlusTreeArray.LOWER_BOUND];
	    AbstractNodeArray subNodeToMoveLeft = newArr.values[BPlusTreeArray.LOWER_BOUND];

	    // Split node in two.  Notice that the 'keyToSplit' is left out of this level.  It will be moved up.
	    DoubleArray<AbstractNodeArray> leftSubNodes = newArr.leftPart(BPlusTreeArray.LOWER_BOUND, 1);
	    leftSubNodes.keys[leftSubNodes.length() - 1] = BPlusTreeArray.LAST_KEY;
	    leftSubNodes.values[leftSubNodes.length() - 1] = subNodeToMoveLeft;
	    InnerNodeArray leftNode = new InnerNodeArray(leftSubNodes);
	    subNodeToMoveLeft.setParent(leftNode); // smf: maybe it is not necessary because of the code in the constructor

	    InnerNodeArray rightNode = new InnerNodeArray(newArr.rightPart(BPlusTreeArray.LOWER_BOUND + 1));

	    // propagate split to parent
	    if (this.getParent() == null) {
		InnerNodeArray newRoot = new InnerNodeArray(leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		return this.getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }

    private DoubleArray<AbstractNodeArray> justInsert(Comparable middleKey, AbstractNodeArray subLeftNode, AbstractNodeArray subRightNode) {
	DoubleArray<AbstractNodeArray> newArr = getSubNodes().duplicateAndAddKey(middleKey, subLeftNode, subRightNode);
	setSubNodes(newArr);
	return newArr;
    }

    private DoubleArray<AbstractNodeArray> justInsertUpdatingParentRelation(Comparable middleKey, AbstractNodeArray subLeftNode, AbstractNodeArray subRightNode) {
	DoubleArray<AbstractNodeArray> newArr = justInsert(middleKey, subLeftNode, subRightNode);
	subLeftNode.setParent(this);
	subRightNode.setParent(this);
	return newArr;
    }

    @Override
    public AbstractNodeArray remove(Comparable key) {
	return findSubNode(key).remove(key);
    }

    AbstractNodeArray replaceDeletedKey(Comparable deletedKey, Comparable replacementKey) {
	AbstractNodeArray subNode = this.getSubNodes().get(deletedKey);
	if (subNode != null) { // found the key a this level
	    return replaceDeletedKey(deletedKey, replacementKey, subNode);
	} else if (this.getParent() != null) {
	    return this.getParent().replaceDeletedKey(deletedKey, replacementKey);
	} else {
	    return this;
	}
    }

    // replaces the key for the given sub-node.  The deletedKey is expected to exist in this node
    private AbstractNodeArray replaceDeletedKey(Comparable deletedKey, Comparable replacementKey, AbstractNodeArray subNode) {
	setSubNodes(getSubNodes().replaceKey(deletedKey, replacementKey, subNode));
	return getRoot();
    }

    /*
     * Deal with underflow from LeafNodeArray
     */

    // null in replacement key means that deletedKey does not have to be
    // replaced. Corollary: the deleted key was not the first key in its leaf
    // node
    AbstractNodeArray underflowFromLeaf(Comparable deletedKey, Comparable replacementKey) {
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	int iter = 0;
	// first, identify the deletion point
	while (BPlusTreeArray.COMPARATOR_SUPPORTING_LAST_KEY.compare(subNodes.keys[iter], deletedKey) <= 0) {
	    iter++;
	}

	// Now, 'entryValue' holds the child where the deletion occurred.
	Comparable entryKey = subNodes.keys[iter];
	AbstractNodeArray entryValue = subNodes.values[iter];

	Comparable previousEntryKey = iter > 0 ? subNodes.keys[iter - 1] : null;
	AbstractNodeArray previousEntryValue = iter > 0 ? subNodes.values[iter - 1] : null;

	Comparable nextEntryKey = null;
	AbstractNodeArray nextEntryValue = null;

	/*
	 * Decide whether to shift or merge, and whether to use the left
	 * or the right sibling.  We prefer merging to shifting.
	 *
	 * Also, we may need to replace the deleted key in some scenarios
	 * (namely when the key was deleted from the left side of a node
	 * AND that side was not changed by a merge/move with/from the left.
	 */
	if (iter == 0) { // the deletedKey was removed from the first sub-node
	    nextEntryKey = subNodes.keys[iter + 1]; // always exists because of LAST_KEY
	    nextEntryValue = subNodes.values[iter + 1];

	    if (nextEntryValue.shallowSize() == BPlusTreeArray.LOWER_BOUND) { // can we merge with the right?
		rightLeafMerge(entryKey, entryValue, nextEntryValue);
	    } else { // cannot merge with the right. We have to move an element from the right to here
		moveChildFromRightToLeft(entryKey, entryValue, nextEntryValue);
	    }
	    if (replacementKey != null && this.getParent() != null) { // the deletedKey occurs somewhere atop only
		this.getParent().replaceDeletedKey(deletedKey, replacementKey);
	    }
	} else if (previousEntryValue.shallowSize() == BPlusTreeArray.LOWER_BOUND) { // can we merge with the left?
	    leftLeafMerge(previousEntryKey, previousEntryValue, entryValue);
	} else {  // cannot merge with the left
	    if (iter >= (subNodes.length() - 1) || (nextEntryValue = subNodes.values[iter + 1]).shallowSize() > BPlusTreeArray.LOWER_BOUND) { // caution: tricky test!!
		// either there is no next or the next is above the lower bound
		moveChildFromLeftToRight(previousEntryKey, previousEntryValue, entryValue);
	    } else {
		rightLeafMerge(entryKey, entryValue, nextEntryValue);
		if (replacementKey != null) { // the deletedKey occurs anywhere (or at this level ONLY?)
		    this.replaceDeletedKey(deletedKey, replacementKey, previousEntryValue);
		}
	    }
	}
	return checkForUnderflow();
    }

    private AbstractNodeArray checkForUnderflow() {
	DoubleArray<AbstractNodeArray> localSubNodes = this.getSubNodes();

	// Now, just check for underflow in this node.   The LAST_KEY is fake, so it does not count for the total.
	if (localSubNodes.length() < BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) {
	    // the LAST_KEY is merely an indirection.  This only occurs in the root node.  We can reduce one depth.
	    if (localSubNodes.length() == 1) { // This only occurs in the root node
		// (size == 1) => (parent == null), but NOT the inverse
		assert(this.getParent() == null);
		AbstractNodeArray child = localSubNodes.firstValue();
		child.setParent(null);
		return child;
	    } else if (this.getParent() != null) {
		return this.getParent().underflowFromInner(this);
	    }
	}
	return getRoot();
    }

    private void rightLeafMerge(Comparable entryKey , AbstractNodeArray entryValue, AbstractNodeArray nextEntryValue) {
	leftLeafMerge(entryKey, entryValue, nextEntryValue);
    }

    private void leftLeafMerge(Comparable previousEntryKey, AbstractNodeArray previousEntryValue, AbstractNodeArray entryValue) {
	entryValue.mergeWithLeftNode(previousEntryValue, null);
	// remove the superfluous node
	setSubNodes(getSubNodes().removeKey(previousEntryKey));
    }

    void mergeWithLeftNode(AbstractNodeArray leftNode, Comparable splitKey) {
	InnerNodeArray left = (InnerNodeArray)leftNode;  // this node does not know how to merge with another kind

	// change the parent of all the left sub-nodes
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	DoubleArray<AbstractNodeArray> leftSubNodes = left.getSubNodes();
	InnerNodeArray uncle = subNodes.values[subNodes.length() - 1].getParent();
	for (int i = 0; i < leftSubNodes.length(); i++) {
	    leftSubNodes.values[i].setParent(uncle);
	}

	DoubleArray<AbstractNodeArray> newArr = subNodes.mergeWith(splitKey, leftSubNodes);
	setSubNodes(newArr);
    }

    // Get the rightmost key-value pair from the left sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromLeftToRight(Comparable leftEntryKey, AbstractNodeArray leftEntryValue, AbstractNodeArray rightEntryValue) {
	DoubleArray<Serializable>.KeyVal leftBiggestKeyValue = leftEntryValue.removeBiggestKeyValue();
	rightEntryValue.addKeyValue(leftBiggestKeyValue);

	// update the split key to be the key we just moved
	setSubNodes(this.getSubNodes().replaceKey(leftEntryKey, leftBiggestKeyValue.key, leftEntryValue));
    }

    // Get the leftmost key-value pair from the right sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromRightToLeft(Comparable leftEntryKey , AbstractNodeArray leftValue, AbstractNodeArray rightValue) {
	DoubleArray<Serializable>.KeyVal rightSmallestKeyValue = rightValue.removeSmallestKeyValue();
	leftValue.addKeyValue(rightSmallestKeyValue);

	// update the split key to be the key after the one we just moved
	Comparable rightNextSmallestKey = rightValue.getSmallestKey();
	DoubleArray<AbstractNodeArray> entries = this.getSubNodes();
	setSubNodes(entries.replaceKey(leftEntryKey, rightNextSmallestKey, leftValue));
    }

    /*
     * Deal with underflow from InnerNodeArray
     */

    AbstractNodeArray underflowFromInner(InnerNodeArray deletedNode) {
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	int iter = 0;

	Comparable entryKey = null;
	AbstractNodeArray entryValue = null;

	// first, identify the deletion point
	do {
	    entryValue = subNodes.values[iter];
	    iter++;
	} while (entryValue != deletedNode);
	// Now, the value() of 'entry' holds the child where the deletion occurred.
	entryKey = subNodes.keys[iter - 1];
	iter--;

	Comparable previousEntryKey = iter > 0 ? subNodes.keys[iter - 1] : null;
	AbstractNodeArray previousEntryValue = iter > 0 ? subNodes.values[iter - 1] : null;
	Comparable nextEntryKey = null;
	AbstractNodeArray nextEntryValue = null;

	/*
	 * Decide whether to shift or merge, and whether to use the left
	 * or the right sibling.  We prefer merging to shifting.
	 */
	if (iter == 0) { // the deletion occurred in the first sub-node
	    nextEntryKey = subNodes.keys[iter + 1]; // always exists because of LAST_KEY
	    nextEntryValue = subNodes.values[iter + 1];

	    if (nextEntryValue.shallowSize() == BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) { // can we merge with the right?
		rightInnerMerge(entryKey, entryValue, nextEntryValue);
	    } else { // cannot merge with the right. We have to move an element from the right to here
		rotateRightToLeft(entryKey, (InnerNodeArray)entryValue, (InnerNodeArray)nextEntryValue);
	    }
	} else if (previousEntryValue.shallowSize() == BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) { // can we merge with the left?
	    leftInnerMerge(previousEntryKey, previousEntryValue, entryValue);
	} else {  // cannot merge with the left
	    if (iter >= (subNodes.length() - 1) || (nextEntryValue = subNodes.values[iter + 1]).shallowSize() > BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) { // caution: tricky test!!
		// either there is no next or the next is above the lower bound
		rotateLeftToRight(previousEntryKey, (InnerNodeArray)previousEntryValue, (InnerNodeArray)entryValue);
	    } else {
		rightInnerMerge(entryKey, entryValue, nextEntryValue);
	    }
	}

	return checkForUnderflow();
    }

    private void rightInnerMerge(Comparable entryKey, AbstractNodeArray entryValue, AbstractNodeArray nextEntryValue) {
	leftInnerMerge(entryKey, entryValue, nextEntryValue);
    }

    private void leftInnerMerge(Comparable previousEntryKey, AbstractNodeArray previousEntryValue, AbstractNodeArray entryValue) {
	entryValue.mergeWithLeftNode(previousEntryValue, previousEntryKey);
	// remove the superfluous node
	setSubNodes(getSubNodes().removeKey(previousEntryKey));
    }

    private void rotateLeftToRight(Comparable leftEntryKey, InnerNodeArray leftSubNode, InnerNodeArray rightSubNode) {
	DoubleArray<AbstractNodeArray> leftSubNodes = leftSubNode.getSubNodes();
	DoubleArray<AbstractNodeArray> rightSubNodes = rightSubNode.getSubNodes();

	Comparable leftHighestKey = leftSubNodes.lowerKeyThanHighest();
	AbstractNodeArray leftHighestValue = leftSubNodes.lastValue();

	// move the highest value from the left to the right.  Use the split-key as the index.
	DoubleArray<AbstractNodeArray> newRightSubNodes = rightSubNodes.addKeyValue(leftEntryKey, leftHighestValue);
	leftHighestValue.setParent(rightSubNode);

	// shift a new child to the last entry on the left
	leftHighestValue = leftSubNodes.get(leftHighestKey);
	DoubleArray<AbstractNodeArray> newLeftSubNodes = leftSubNodes.removeKey(leftHighestKey);
	// this is already a duplicated array, no need to go through that process again
	newLeftSubNodes.values[newLeftSubNodes.length() - 1] = leftHighestValue;

	leftSubNode.setSubNodes(newLeftSubNodes);
	rightSubNode.setSubNodes(newRightSubNodes);

	// update the split-key to be the key we just removed from the left
	setSubNodes(getSubNodes().replaceKey(leftEntryKey, leftHighestKey, leftSubNode));
    }

    private void rotateRightToLeft(Comparable leftEntryKey, InnerNodeArray leftSubNode, InnerNodeArray rightSubNode) {
	DoubleArray<AbstractNodeArray> leftSubNodes = leftSubNode.getSubNodes();
	DoubleArray<AbstractNodeArray> rightSubNodes = rightSubNode.getSubNodes();

	// remove right's lowest entry
	DoubleArray<AbstractNodeArray>.KeyVal rightLowestEntry = rightSubNodes.getSmallestKeyValue();
	DoubleArray<AbstractNodeArray> newRightSubNodes = rightSubNodes.removeSmallestKeyValue();

	// re-index the left highest value under the split-key, which is moved down
	AbstractNodeArray leftHighestValue = leftSubNodes.lastValue();
	DoubleArray<AbstractNodeArray> newLeftSubNodes = leftSubNodes.addKeyValue(leftEntryKey, leftHighestValue);
	// and add the right's lowest entry on the left
	AbstractNodeArray rightLowestValue = rightLowestEntry.val;
	// this is already a duplicated array, no need to go through that process again
	newLeftSubNodes.values[newLeftSubNodes.length() - 1] = rightLowestValue;

	rightLowestValue.setParent(leftSubNode);

	leftSubNode.setSubNodes(newLeftSubNodes);
	rightSubNode.setSubNodes(newRightSubNodes);

	// update the split-key to be the key we just removed from the right
	setSubNodes(getSubNodes().replaceKey(leftEntryKey, rightLowestEntry.key, leftSubNode));
    }

    @Override
    DoubleArray.KeyVal removeBiggestKeyValue() {
	throw new UnsupportedOperationException("not yet implemented: removeBiggestKeyValue from inner node");
    }

    @Override
    DoubleArray.KeyVal removeSmallestKeyValue() {
	throw new UnsupportedOperationException("not yet implemented: removeSmallestKeyValue from inner node");
    }

    @Override
    Comparable getSmallestKey() {
	throw new UnsupportedOperationException("not yet implemented: getSmallestKey from inner node");
    }

    @Override
    void addKeyValue(DoubleArray.KeyVal keyValue) {
	throw new UnsupportedOperationException("not yet implemented: addKeyValue to inner node should account for LAST_KEY ?!?");
    }

    @Override
    public Serializable get(Comparable key) {
	return findSubNode(key).get(key);
    }

    @Override
    public Serializable get(boolean forceMiss, Comparable key) {
	return findSubNode(forceMiss, key).get(forceMiss, key);
    }
    
    // travels to the leftmost leaf and goes from there;
    @Override
    public Serializable getIndex(int index) {
	return this.getSubNodes().firstValue().getIndex(index);
    }

    // travels to the leftmost leaf and goes from there;
    @Override
    public AbstractNodeArray removeIndex(int index) {
	return this.getSubNodes().firstValue().removeIndex(index);
    }

    @Override
    public boolean containsKey(Comparable key) {
	return findSubNode(key).containsKey(key);
    }

    private AbstractNodeArray findSubNode(Comparable key) {
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	for (int i = 0; i < subNodes.length(); i++) {
	    Comparable splitKey = subNodes.keys[i];
	    if (BPlusTreeArray.COMPARATOR_SUPPORTING_LAST_KEY.compare(splitKey, key) > 0) { // this will eventually be true because the LAST_KEY is greater than all
		return subNodes.values[i];
	    }
	}
	throw new RuntimeException("findSubNode() didn't find a suitable sub-node!?");
    }

    private AbstractNodeArray findSubNode(boolean forceMiss, Comparable key) {
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodesCached(forceMiss);
	for (int i = 0; i < subNodes.length(); i++) {
	    Comparable splitKey = subNodes.keys[i];
	    if (BPlusTreeArray.COMPARATOR_SUPPORTING_LAST_KEY.compare(splitKey, key) > 0) { // this will eventually be true because the LAST_KEY is greater than all
		return subNodes.values[i];
	    }
	}
	throw new RuntimeException("findSubNode() didn't find a suitable sub-node!?");
    }
    
    @Override
    int shallowSize() {
	return this.getSubNodes().length();
    }

    @Override
    public int size() {
	int total = 0;
	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	for (int i = 0; i < subNodes.length(); i++) {
	    total += subNodes.values[i].size();
	}
	return total;
    }

    @Override
    public Iterator iterator() {
	return this.getSubNodes().firstValue().iterator();
    }
    
    @Override
    public Iterator iteratorCached(boolean forceMiss) {
    	return this.getSubNodesCached(forceMiss).firstValue().iteratorCached(forceMiss);
    }

    @Override
    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	StringBuilder str = new StringBuilder();
	StringBuilder spaces = BPlusTreeArray.spaces(level);
	str.append(spaces);
	str.append("[" + (dumpNodeIds ? this : "") + ": ");

	DoubleArray<AbstractNodeArray> subNodes = this.getSubNodes();
	for (int i = 0; i < subNodes.length(); i++) {
	    Comparable key = subNodes.keys[i];
	    AbstractNodeArray value = subNodes.values[i];
	    str.append("\n");
	    str.append(value.dump(level + 4, dumpKeysOnly, dumpNodeIds));
	    str.append(spaces);
	    str.append("(" + key + ") ");
	}
	str.append("\n");
	str.append(spaces);
	if (dumpNodeIds) {
	    str.append("] ^" + this.getParent() + "\n");
	} else {
	    str.append("]\n");
	}
	return str.toString();
    }
}