package pt.ist.fenixframework.core.adt.bplustree;

import java.util.Iterator;

import pt.ist.fenixframework.core.AbstractDomainObject;

/** 
 * Inner node of a B+-Tree.  These nodes do not contain elements.  They only
 * contain M keys (ordered) and M+1 sub-nodes (M > 0).  The n-th sub-node will
 * contain elements whose keys are all less than the n-th key, except for the
 * last sub-node (L) which will contain elements whose keys will be greater
 * than or equal to the M-th key.
 */
public class InnerNodeArrayUnsafe extends InnerNodeArrayUnsafe_Base {

    private InnerNodeArrayUnsafe() {
	super();
    }

    InnerNodeArrayUnsafe(AbstractNodeArrayUnsafe leftNode, AbstractNodeArrayUnsafe rightNode, Comparable splitKey) {
	setSubNodes(new DoubleArray<AbstractNodeArrayUnsafe>(AbstractNodeArrayUnsafe.class, splitKey, leftNode, rightNode));
	leftNode.setParent(this);
	rightNode.setParent(this);
    }

    private InnerNodeArrayUnsafe(DoubleArray<AbstractNodeArrayUnsafe> subNodes) {
	setSubNodes(subNodes);
	for (int i = 0; i < subNodes.length(); i++) { // smf: either don't do this or don't setParent when making new
	    subNodes.values[i].setParent(this);
	}
    }

    @Override
    public AbstractNodeArrayUnsafe insert(Comparable key, AbstractDomainObject value) {
	return findSubNode(key).insert(key, value);
    }

    // this method is invoked when a node in the next depth level got full, it
    // was split and now needs to pass a new key to its parent (this)
    AbstractNodeArrayUnsafe rebase(AbstractNodeArrayUnsafe subLeftNode, AbstractNodeArrayUnsafe subRightNode, Comparable middleKey) {
	DoubleArray<AbstractNodeArrayUnsafe> newArr = justInsertUpdatingParentRelation(middleKey, subLeftNode, subRightNode);
	if (newArr.length() <= BPlusTreeArray.MAX_NUMBER_OF_ELEMENTS) { // this node can accommodate the new split
	    return getRootUnsafe();
	} else { // must split this node
	    // find middle position (key to move up amd sub-node to move left)
	    Comparable keyToSplit = newArr.keys[BPlusTreeArray.LOWER_BOUND];
	    AbstractNodeArrayUnsafe subNodeToMoveLeft = newArr.values[BPlusTreeArray.LOWER_BOUND];

	    // Split node in two.  Notice that the 'keyToSplit' is left out of this level.  It will be moved up.
	    DoubleArray<AbstractNodeArrayUnsafe> leftSubNodes = newArr.leftPart(BPlusTreeArray.LOWER_BOUND, 1);
	    leftSubNodes.keys[leftSubNodes.length() - 1] = BPlusTreeArray.LAST_KEY;
	    leftSubNodes.values[leftSubNodes.length() - 1] = subNodeToMoveLeft;
	    InnerNodeArrayUnsafe leftNode = new InnerNodeArrayUnsafe(leftSubNodes);
	    subNodeToMoveLeft.setParent(leftNode); // smf: maybe it is not necessary because of the code in the constructor

	    InnerNodeArrayUnsafe rightNode = new InnerNodeArrayUnsafe(newArr.rightPart(BPlusTreeArray.LOWER_BOUND + 1));

	    // propagate split to parent
	    if (this.getParent() == null) {
		InnerNodeArrayUnsafe newRoot = new InnerNodeArrayUnsafe(leftNode, rightNode, keyToSplit);
		return newRoot;
	    } else {
		return this.getParent().rebase(leftNode, rightNode, keyToSplit);
	    }
	}
    }

    private DoubleArray<AbstractNodeArrayUnsafe> justInsert(Comparable middleKey, AbstractNodeArrayUnsafe subLeftNode, AbstractNodeArrayUnsafe subRightNode) {
	DoubleArray<AbstractNodeArrayUnsafe> newArr = getSubNodes().duplicateAndAddKey(middleKey, subLeftNode, subRightNode);
	setSubNodes(newArr);
	return newArr;
    }

    private DoubleArray<AbstractNodeArrayUnsafe> justInsertUpdatingParentRelation(Comparable middleKey, AbstractNodeArrayUnsafe subLeftNode, AbstractNodeArrayUnsafe subRightNode) {
	DoubleArray<AbstractNodeArrayUnsafe> newArr = justInsert(middleKey, subLeftNode, subRightNode);
	subLeftNode.setParent(this);
	subRightNode.setParent(this);
	return newArr;
    }

    @Override
    public AbstractNodeArrayUnsafe remove(Comparable key) {
	return findSubNode(key).remove(key);
    }

    AbstractNodeArrayUnsafe replaceDeletedKey(Comparable deletedKey, Comparable replacementKey) {
	AbstractNodeArrayUnsafe subNode = this.getSubNodes().get(deletedKey);
	if (subNode != null) { // found the key a this level
	    return replaceDeletedKey(deletedKey, replacementKey, subNode);
	} else if (this.getParent() != null) {
	    return this.getParent().replaceDeletedKey(deletedKey, replacementKey);
	} else {
	    return this;
	}
    }

    // replaces the key for the given sub-node.  The deletedKey is expected to exist in this node
    private AbstractNodeArrayUnsafe replaceDeletedKey(Comparable deletedKey, Comparable replacementKey, AbstractNodeArrayUnsafe subNode) {
	setSubNodes(getSubNodes().replaceKey(deletedKey, replacementKey, subNode));
	return getRoot();
    }

    /*
     * Deal with underflow from LeafNodeArrayUnsafe
     */

    // null in replacement key means that deletedKey does not have to be
    // replaced. Corollary: the deleted key was not the first key in its leaf
    // node
    AbstractNodeArrayUnsafe underflowFromLeaf(Comparable deletedKey, Comparable replacementKey) {
	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodes();
	int iter = 0;
	// first, identify the deletion point
	while (BPlusTreeArray.COMPARATOR_SUPPORTING_LAST_KEY.compare(subNodes.keys[iter], deletedKey) <= 0) {
	    iter++;
	}

	// Now, 'entryValue' holds the child where the deletion occurred.
	Comparable entryKey = subNodes.keys[iter];
	AbstractNodeArrayUnsafe entryValue = subNodes.values[iter];

	Comparable previousEntryKey = iter > 0 ? subNodes.keys[iter - 1] : null;
	AbstractNodeArrayUnsafe previousEntryValue = iter > 0 ? subNodes.values[iter - 1] : null;

	Comparable nextEntryKey = null;
	AbstractNodeArrayUnsafe nextEntryValue = null;

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

    private AbstractNodeArrayUnsafe checkForUnderflow() {
	DoubleArray<AbstractNodeArrayUnsafe> localSubNodes = this.getSubNodes();

	// Now, just check for underflow in this node.   The LAST_KEY is fake, so it does not count for the total.
	if (localSubNodes.length() < BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) {
	    // the LAST_KEY is merely an indirection.  This only occurs in the root node.  We can reduce one depth.
	    if (localSubNodes.length() == 1) { // This only occurs in the root node
		// (size == 1) => (parent == null), but NOT the inverse
		assert(this.getParent() == null);
		AbstractNodeArrayUnsafe child = localSubNodes.firstValue();
		child.setParent(null);
		return child;
	    } else if (this.getParent() != null) {
		return this.getParent().underflowFromInner(this);
	    }
	}
	return getRoot();
    }

    private void rightLeafMerge(Comparable entryKey , AbstractNodeArrayUnsafe entryValue, AbstractNodeArrayUnsafe nextEntryValue) {
	leftLeafMerge(entryKey, entryValue, nextEntryValue);
    }

    private void leftLeafMerge(Comparable previousEntryKey, AbstractNodeArrayUnsafe previousEntryValue, AbstractNodeArrayUnsafe entryValue) {
	entryValue.mergeWithLeftNode(previousEntryValue, null);
	// remove the superfluous node
	setSubNodes(getSubNodes().removeKey(previousEntryKey));
    }

    void mergeWithLeftNode(AbstractNodeArrayUnsafe leftNode, Comparable splitKey) {
	InnerNodeArrayUnsafe left = (InnerNodeArrayUnsafe)leftNode;  // this node does not know how to merge with another kind

	// change the parent of all the left sub-nodes
	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodes();
	DoubleArray<AbstractNodeArrayUnsafe> leftSubNodes = left.getSubNodes();
	InnerNodeArrayUnsafe uncle = subNodes.values[subNodes.length() - 1].getParent();
	for (int i = 0; i < leftSubNodes.length(); i++) {
	    leftSubNodes.values[i].setParent(uncle);
	}

	DoubleArray<AbstractNodeArrayUnsafe> newArr = subNodes.mergeWith(splitKey, leftSubNodes);
	setSubNodes(newArr);
    }

    // Get the rightmost key-value pair from the left sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromLeftToRight(Comparable leftEntryKey, AbstractNodeArrayUnsafe leftEntryValue, AbstractNodeArrayUnsafe rightEntryValue) {
	DoubleArray<AbstractDomainObject>.KeyVal leftBiggestKeyValue = leftEntryValue.removeBiggestKeyValue();
	rightEntryValue.addKeyValue(leftBiggestKeyValue);

	// update the split key to be the key we just moved
	setSubNodes(this.getSubNodes().replaceKey(leftEntryKey, leftBiggestKeyValue.key, leftEntryValue));
    }

    // Get the leftmost key-value pair from the right sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromRightToLeft(Comparable leftEntryKey , AbstractNodeArrayUnsafe leftValue, AbstractNodeArrayUnsafe rightValue) {
	DoubleArray<AbstractDomainObject>.KeyVal rightSmallestKeyValue = rightValue.removeSmallestKeyValue();
	leftValue.addKeyValue(rightSmallestKeyValue);

	// update the split key to be the key after the one we just moved
	Comparable rightNextSmallestKey = rightValue.getSmallestKey();
	DoubleArray<AbstractNodeArrayUnsafe> entries = this.getSubNodes();
	setSubNodes(entries.replaceKey(leftEntryKey, rightNextSmallestKey, leftValue));
    }

    /*
     * Deal with underflow from InnerNodeArrayUnsafe
     */

    AbstractNodeArrayUnsafe underflowFromInner(InnerNodeArrayUnsafe deletedNode) {
	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodes();
	int iter = 0;

	Comparable entryKey = null;
	AbstractNodeArrayUnsafe entryValue = null;

	// first, identify the deletion point
	do {
	    entryValue = subNodes.values[iter];
	    iter++;
	} while (entryValue != deletedNode);
	// Now, the value() of 'entry' holds the child where the deletion occurred.
	entryKey = subNodes.keys[iter - 1];
	iter--;

	Comparable previousEntryKey = iter > 0 ? subNodes.keys[iter - 1] : null;
	AbstractNodeArrayUnsafe previousEntryValue = iter > 0 ? subNodes.values[iter - 1] : null;
	Comparable nextEntryKey = null;
	AbstractNodeArrayUnsafe nextEntryValue = null;

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
		rotateRightToLeft(entryKey, (InnerNodeArrayUnsafe)entryValue, (InnerNodeArrayUnsafe)nextEntryValue);
	    }
	} else if (previousEntryValue.shallowSize() == BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) { // can we merge with the left?
	    leftInnerMerge(previousEntryKey, previousEntryValue, entryValue);
	} else {  // cannot merge with the left
	    if (iter >= (subNodes.length() - 1) || (nextEntryValue = subNodes.values[iter + 1]).shallowSize() > BPlusTreeArray.LOWER_BOUND_WITH_LAST_KEY) { // caution: tricky test!!
		// either there is no next or the next is above the lower bound
		rotateLeftToRight(previousEntryKey, (InnerNodeArrayUnsafe)previousEntryValue, (InnerNodeArrayUnsafe)entryValue);
	    } else {
		rightInnerMerge(entryKey, entryValue, nextEntryValue);
	    }
	}

	return checkForUnderflow();
    }

    private void rightInnerMerge(Comparable entryKey, AbstractNodeArrayUnsafe entryValue, AbstractNodeArrayUnsafe nextEntryValue) {
	leftInnerMerge(entryKey, entryValue, nextEntryValue);
    }

    private void leftInnerMerge(Comparable previousEntryKey, AbstractNodeArrayUnsafe previousEntryValue, AbstractNodeArrayUnsafe entryValue) {
	entryValue.mergeWithLeftNode(previousEntryValue, previousEntryKey);
	// remove the superfluous node
	setSubNodes(getSubNodes().removeKey(previousEntryKey));
    }

    private void rotateLeftToRight(Comparable leftEntryKey, InnerNodeArrayUnsafe leftSubNode, InnerNodeArrayUnsafe rightSubNode) {
	DoubleArray<AbstractNodeArrayUnsafe> leftSubNodes = leftSubNode.getSubNodes();
	DoubleArray<AbstractNodeArrayUnsafe> rightSubNodes = rightSubNode.getSubNodes();

	Comparable leftHighestKey = leftSubNodes.lowerKeyThanHighest();
	AbstractNodeArrayUnsafe leftHighestValue = leftSubNodes.lastValue();

	// move the highest value from the left to the right.  Use the split-key as the index.
	DoubleArray<AbstractNodeArrayUnsafe> newRightSubNodes = rightSubNodes.addKeyValue(leftEntryKey, leftHighestValue);
	leftHighestValue.setParent(rightSubNode);

	// shift a new child to the last entry on the left
	leftHighestValue = leftSubNodes.get(leftHighestKey);
	DoubleArray<AbstractNodeArrayUnsafe> newLeftSubNodes = leftSubNodes.removeKey(leftHighestKey);
	// this is already a duplicated array, no need to go through that process again
	newLeftSubNodes.values[newLeftSubNodes.length() - 1] = leftHighestValue;

	leftSubNode.setSubNodes(newLeftSubNodes);
	rightSubNode.setSubNodes(newRightSubNodes);

	// update the split-key to be the key we just removed from the left
	setSubNodes(getSubNodes().replaceKey(leftEntryKey, leftHighestKey, leftSubNode));
    }

    private void rotateRightToLeft(Comparable leftEntryKey, InnerNodeArrayUnsafe leftSubNode, InnerNodeArrayUnsafe rightSubNode) {
	DoubleArray<AbstractNodeArrayUnsafe> leftSubNodes = leftSubNode.getSubNodes();
	DoubleArray<AbstractNodeArrayUnsafe> rightSubNodes = rightSubNode.getSubNodes();

	// remove right's lowest entry
	DoubleArray<AbstractNodeArrayUnsafe>.KeyVal rightLowestEntry = rightSubNodes.getSmallestKeyValue();
	DoubleArray<AbstractNodeArrayUnsafe> newRightSubNodes = rightSubNodes.removeSmallestKeyValue();

	// re-index the left highest value under the split-key, which is moved down
	AbstractNodeArrayUnsafe leftHighestValue = leftSubNodes.lastValue();
	DoubleArray<AbstractNodeArrayUnsafe> newLeftSubNodes = leftSubNodes.addKeyValue(leftEntryKey, leftHighestValue);
	// and add the right's lowest entry on the left
	AbstractNodeArrayUnsafe rightLowestValue = rightLowestEntry.val;
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
    public AbstractDomainObject get(Comparable key) {
	return findSubNode(key).get(key);
    }

    // travels to the leftmost leaf and goes from there;
    @Override
    public AbstractDomainObject getIndex(int index) {
	return this.getSubNodes().firstValue().getIndex(index);
    }

    // travels to the leftmost leaf and goes from there;
    @Override
    public AbstractNodeArrayUnsafe removeIndex(int index) {
	return this.getSubNodes().firstValue().removeIndex(index);
    }

    @Override
    public boolean containsKey(Comparable key) {
	return findSubNode(key).containsKey(key);
    }

    private AbstractNodeArrayUnsafe findSubNode(Comparable key) {
	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodesUnsafe();
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
	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodes();
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
    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	StringBuilder str = new StringBuilder();
	StringBuilder spaces = BPlusTreeArray.spaces(level);
	str.append(spaces);
	str.append("[" + (dumpNodeIds ? this : "") + ": ");

	DoubleArray<AbstractNodeArrayUnsafe> subNodes = this.getSubNodes();
	for (int i = 0; i < subNodes.length(); i++) {
	    Comparable key = subNodes.keys[i];
	    AbstractNodeArrayUnsafe value = subNodes.values[i];
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