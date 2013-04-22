package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import pt.ist.fenixframework.NoDomainMetaObjects;

/**
 * Inner node of a B+-Tree. These nodes do not contain elements. They only
 * contain M keys (ordered) and M+1 sub-nodes (M > 0). The n-th sub-node will
 * contain elements whose keys are all less than the n-th key, except for the
 * last sub-node (L) which will contain elements whose keys will be greater
 * than or equal to the M-th key.
 */
@NoDomainMetaObjects
public class InnerNode extends InnerNode_Base {

    private InnerNode() {
        super();
    }

    InnerNode(AbstractNode leftNode, AbstractNode rightNode, Comparable splitKey) {
        TreeMap<Comparable, AbstractNode> newMap =
                new TreeMap<Comparable, AbstractNode>(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY);
        newMap.put(splitKey, leftNode);
        newMap.put(BPlusTree.LAST_KEY, rightNode);

        setSubNodes(newMap);
        leftNode.setParent(this);
        rightNode.setParent(this);
    }

    private InnerNode(TreeMap<Comparable, AbstractNode> subNodes) {
        setSubNodes(subNodes);
        for (AbstractNode subNode : subNodes.values()) { // smf: either don't do this or don't setParent when making new
            subNode.setParent(this);
        }
    }

    private TreeMap<Comparable, AbstractNode> duplicateMap() {
        return new TreeMap<Comparable, AbstractNode>(getSubNodes());
    }

    @Override
    public AbstractNode insert(Comparable key, Serializable value) {
        return findSubNode(key).insert(key, value);
    }

    // this method is invoked when a node in the next depth level got full, it
    // was split and now needs to pass a new key to its parent (this)
    AbstractNode rebase(AbstractNode subLeftNode, AbstractNode subRightNode, Comparable middleKey) {
        TreeMap<Comparable, AbstractNode> newMap = justInsertUpdatingParentRelation(middleKey, subLeftNode, subRightNode);
        if (newMap.size() <= BPlusTree.MAX_NUMBER_OF_ELEMENTS) { // this node can accommodate the new split
            return getRoot();
        } else { // must split this node
            // find middle position (key to move up amd sub-node to move left)
            Iterator<Map.Entry<Comparable, AbstractNode>> entriesIterator = newMap.entrySet().iterator();
            for (int i = 0; i < BPlusTree.LOWER_BOUND; i++) {
                entriesIterator.next();
            }
            Map.Entry<Comparable, AbstractNode> splitEntry = entriesIterator.next();
            Comparable keyToSplit = splitEntry.getKey();
            AbstractNode subNodeToMoveLeft = splitEntry.getValue();
            Comparable nextKey = entriesIterator.next().getKey();

            // Split node in two.  Notice that the 'keyToSplit' is left out of
            // this level.  It will be moved up.
            TreeMap<Comparable, AbstractNode> leftSubNodes = new TreeMap<Comparable, AbstractNode>(newMap.headMap(keyToSplit));
            leftSubNodes.put(BPlusTree.LAST_KEY, subNodeToMoveLeft);
            InnerNode leftNode = new InnerNode(leftSubNodes);
            subNodeToMoveLeft.setParent(leftNode); // smf: maybe it is not necessary because of the code in the constructor

            InnerNode rightNode = new InnerNode(new TreeMap<Comparable, AbstractNode>(newMap.tailMap(nextKey)));

            // propagate split to parent
            if (this.getParent() == null) {
                InnerNode newRoot = new InnerNode(leftNode, rightNode, keyToSplit);
                return newRoot;
            } else {
                return this.getParent().rebase(leftNode, rightNode, keyToSplit);
            }
        }
    }

    private TreeMap<Comparable, AbstractNode> justInsert(Comparable middleKey, AbstractNode subLeftNode, AbstractNode subRightNode) {
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();

        // find smallest key greater than middleKey
        Comparable keyJustAfterMiddleKey = newMap.higherKey(middleKey);
        newMap.put(keyJustAfterMiddleKey, subRightNode); // this replaces the previous mapping
        newMap.put(middleKey, subLeftNode); // this adds the new split
        setSubNodes(newMap);
        return newMap;
    }

    private TreeMap<Comparable, AbstractNode> justInsertUpdatingParentRelation(Comparable middleKey, AbstractNode subLeftNode,
            AbstractNode subRightNode) {
        TreeMap<Comparable, AbstractNode> newMap = justInsert(middleKey, subLeftNode, subRightNode);
        subLeftNode.setParent(this);
        subRightNode.setParent(this);
        return newMap;
    }

    @Override
    public AbstractNode remove(Comparable key) {
        return findSubNode(key).remove(key);
    }

    @Override
    void delete() {
        for (AbstractNode subNode : getSubNodes().values()) {
            subNode.delete();
        }
        setParent(null);
        deleteDomainObject();
    }

    AbstractNode replaceDeletedKey(Comparable deletedKey, Comparable replacementKey) {
        AbstractNode subNode = this.getSubNodes().get(deletedKey);
        if (subNode != null) { // found the key a this level
            return replaceDeletedKey(deletedKey, replacementKey, subNode);
        } else if (this.getParent() != null) {
            return this.getParent().replaceDeletedKey(deletedKey, replacementKey);
        } else {
            return this;
        }
    }

    // replaces the key for the given sub-node.  The deletedKey is expected to exist in this node
    private AbstractNode replaceDeletedKey(Comparable deletedKey, Comparable replacementKey, AbstractNode subNode) {
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(deletedKey);
        newMap.put(replacementKey, subNode);
        setSubNodes(newMap);
        return getRoot();
    }

    /*
     * Deal with underflow from LeafNode
     */

    // null in replacement key means that deletedKey does not have to be
    // replaced. Corollary: the deleted key was not the first key in its leaf
    // node
    AbstractNode underflowFromLeaf(Comparable deletedKey, Comparable replacementKey) {
        Iterator<Map.Entry<Comparable, AbstractNode>> it = this.getSubNodes().entrySet().iterator();
        Map.Entry<Comparable, AbstractNode> previousEntry = null;
        Map.Entry<Comparable, AbstractNode> entry = it.next();
        Map.Entry<Comparable, AbstractNode> nextEntry = null;;

        // first, identify the deletion point
        while (BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY.compare(entry.getKey(), deletedKey) <= 0) {
            previousEntry = entry;
            entry = it.next();
        }
        // Now, the value() of 'entry' holds the child where the deletion occurred.

        /*
         * Decide whether to shift or merge, and whether to use the left
         * or the right sibling.  We prefer merging to shifting.
         *
         * Also, we may need to replace the deleted key in some scenarios
         * (namely when the key was deleted from the left side of a node
         * AND that side was not changed by a merge/move with/from the left.
         */
        if (previousEntry == null) { // the deletedKey was removed from the first sub-node
            nextEntry = it.next(); // always exists because of LAST_KEY
            if (nextEntry.getValue().shallowSize() == BPlusTree.LOWER_BOUND) { // can we merge with the right?
                rightLeafMerge(entry, nextEntry);
            } else { // cannot merge with the right. We have to move an element from the right to here
                moveChildFromRightToLeft(entry, nextEntry);
            }
            if (replacementKey != null && this.getParent() != null) { // the deletedKey occurs somewhere atop only
                this.getParent().replaceDeletedKey(deletedKey, replacementKey);
            }
        } else if (previousEntry.getValue().shallowSize() == BPlusTree.LOWER_BOUND) { // can we merge with the left?
            leftLeafMerge(previousEntry, entry);
        } else {  // cannot merge with the left
            if (!it.hasNext() || (nextEntry = it.next()).getValue().shallowSize() > BPlusTree.LOWER_BOUND) { // caution: tricky test!!
                // either there is no next or the next is above the lower bound
                moveChildFromLeftToRight(previousEntry, entry);
            } else {
                rightLeafMerge(entry, nextEntry);
                if (replacementKey != null) { // the deletedKey occurs anywhere (or at this level ONLY?)
                    this.replaceDeletedKey(deletedKey, replacementKey, previousEntry.getValue());
                }
            }
        }
        return checkForUnderflow();
    }

    private AbstractNode checkForUnderflow() {
        TreeMap<Comparable, AbstractNode> localSubNodes = this.getSubNodes();

        // Now, just check for underflow in this node.   The LAST_KEY is fake, so it does not count for the total.
        if (localSubNodes.size() < BPlusTree.LOWER_BOUND_WITH_LAST_KEY) {
            // the LAST_KEY is merely an indirection.  This only occurs in the root node.  We can reduce one depth.
            if (localSubNodes.size() == 1) { // This only occurs in the root node
                // (size == 1) => (parent == null), but NOT the inverse
                assert (this.getParent() == null);
                AbstractNode child = localSubNodes.firstEntry().getValue();
                child.setParent(null);
                return child;
            } else if (this.getParent() != null) {
                return this.getParent().underflowFromInner(this);
            }
        }
        return getRoot();
    }

    private void rightLeafMerge(Map.Entry<Comparable, AbstractNode> entry, Map.Entry<Comparable, AbstractNode> nextEntry) {
        leftLeafMerge(entry, nextEntry);
    }

    private void leftLeafMerge(Map.Entry<Comparable, AbstractNode> previousEntry, Map.Entry<Comparable, AbstractNode> entry) {
        entry.getValue().mergeWithLeftNode(previousEntry.getValue(), null);
        // remove the superfluous node
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(previousEntry.getKey());
        setSubNodes(newMap);
    }

    @Override
    void mergeWithLeftNode(AbstractNode leftNode, Comparable splitKey) {
        InnerNode left = (InnerNode) leftNode;  // this node does not know how to merge with another kind

        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        TreeMap<Comparable, AbstractNode> newLeftSubNodes = left.duplicateMap();

        // change the parent of all the left sub-nodes
        InnerNode uncle = newMap.get(BPlusTree.LAST_KEY).getParent();
        for (AbstractNode leftSubNode : newLeftSubNodes.values()) {
            leftSubNode.setParent(uncle);
        }

        // remove the entry for left's LAST_KEY
        Map.Entry<Comparable, AbstractNode> higherLeftValue = newLeftSubNodes.pollLastEntry();

        // add the higher left value associated with the split-key
        newMap.put(splitKey, higherLeftValue.getValue());

        // merge the remaining left sub-nodes
        newMap.putAll(newLeftSubNodes);
        setSubNodes(newMap);
    }

    // Get the rightmost key-value pair from the left sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromLeftToRight(Map.Entry<Comparable, AbstractNode> leftEntry,
            Map.Entry<Comparable, AbstractNode> rightEntry) {
        AbstractNode leftSubNode = leftEntry.getValue();

        Map.Entry<Comparable, Serializable> leftBiggestKeyValue = leftSubNode.removeBiggestKeyValue();
        rightEntry.getValue().addKeyValue(leftBiggestKeyValue);

        // update the split key to be the key we just moved
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(leftEntry.getKey());
        newMap.put(leftBiggestKeyValue.getKey(), leftSubNode);
        setSubNodes(newMap);
    }

    // Get the leftmost key-value pair from the right sub-node and move it to the given sub-node.  Update the split key
    private void moveChildFromRightToLeft(Map.Entry<Comparable, AbstractNode> leftEntry,
            Map.Entry<Comparable, AbstractNode> rightEntry) {
        AbstractNode rightSubNode = rightEntry.getValue();

        Map.Entry<Comparable, Serializable> rightSmallestKeyValue = rightSubNode.removeSmallestKeyValue();
        AbstractNode leftSubNode = leftEntry.getValue();
        leftSubNode.addKeyValue(rightSmallestKeyValue);

        // update the split key to be the key after the one we just moved
        Comparable rightNextSmallestKey = rightSubNode.getSmallestKey();
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(leftEntry.getKey());
        newMap.put(rightNextSmallestKey, leftSubNode);
        setSubNodes(newMap);
    }

    /*
     * Deal with underflow from InnerNode
     */

    AbstractNode underflowFromInner(InnerNode deletedNode) {
        Iterator<Map.Entry<Comparable, AbstractNode>> it = this.getSubNodes().entrySet().iterator();
        Map.Entry<Comparable, AbstractNode> previousEntry = null;
        Map.Entry<Comparable, AbstractNode> entry = null;
        Map.Entry<Comparable, AbstractNode> nextEntry = null;;

        // first, identify the deletion point
        do {
            previousEntry = entry;
            entry = it.next();
        } while (entry.getValue() != deletedNode);
        // Now, the value() of 'entry' holds the child where the deletion occurred.

        /*
         * Decide whether to shift or merge, and whether to use the left
         * or the right sibling.  We prefer merging to shifting.
         */
        if (previousEntry == null) { // the deletion occurred in the first sub-node
            nextEntry = it.next(); // always exists because of LAST_KEY
            if (nextEntry.getValue().shallowSize() == BPlusTree.LOWER_BOUND_WITH_LAST_KEY) { // can we merge with the right?
                rightInnerMerge(entry, nextEntry);
            } else { // cannot merge with the right. We have to move an element from the right to here
                rotateRightToLeft((Map.Entry) entry, (Map.Entry) nextEntry);
            }
        } else if (previousEntry.getValue().shallowSize() == BPlusTree.LOWER_BOUND_WITH_LAST_KEY) { // can we merge with the left?
            leftInnerMerge(previousEntry, entry);
        } else {  // cannot merge with the left
            if (!it.hasNext() || (nextEntry = it.next()).getValue().shallowSize() > BPlusTree.LOWER_BOUND_WITH_LAST_KEY) { // caution: tricky test!!
                // either there is no next or the next is above the lower bound
                rotateLeftToRight((Map.Entry) previousEntry, (Map.Entry) entry);
            } else {
                rightInnerMerge(entry, nextEntry);
            }
        }

        return checkForUnderflow();
    }

    private void rightInnerMerge(Map.Entry<Comparable, AbstractNode> entry, Map.Entry<Comparable, AbstractNode> nextEntry) {
        leftInnerMerge(entry, nextEntry);
    }

    private void leftInnerMerge(Map.Entry<Comparable, AbstractNode> previousEntry, Map.Entry<Comparable, AbstractNode> entry) {
        Comparable splitKey = previousEntry.getKey();
        entry.getValue().mergeWithLeftNode(previousEntry.getValue(), splitKey);
        // remove the superfluous node
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(splitKey);
        setSubNodes(newMap);
    }

    private void rotateLeftToRight(Map.Entry<Comparable, InnerNode> leftEntry, Map.Entry<Comparable, InnerNode> rightEntry) {
        InnerNode leftSubNode = leftEntry.getValue();
        InnerNode rightSubNode = rightEntry.getValue();

        TreeMap<Comparable, AbstractNode> newLeftSubNodeSubNodes = leftSubNode.duplicateMap();
        TreeMap<Comparable, AbstractNode> newRightSubNodeSubNodes = rightSubNode.duplicateMap();

        Comparable leftHighestKey = newLeftSubNodeSubNodes.lowerKey(BPlusTree.LAST_KEY);
        AbstractNode leftHighestValue = newLeftSubNodeSubNodes.get(BPlusTree.LAST_KEY);

        // move the highest value from the left to the right.  Use the split-key as the index.
        newRightSubNodeSubNodes.put(leftEntry.getKey(), leftHighestValue);
        leftHighestValue.setParent(rightSubNode);

        // shift a new child to the last entry on the left
        leftHighestValue = newLeftSubNodeSubNodes.remove(leftHighestKey);
        newLeftSubNodeSubNodes.put(BPlusTree.LAST_KEY, leftHighestValue);

        leftSubNode.setSubNodes(newLeftSubNodeSubNodes);
        rightSubNode.setSubNodes(newRightSubNodeSubNodes);

        // update the split-key to be the key we just removed from the left
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(leftEntry.getKey());
        newMap.put(leftHighestKey, leftSubNode);
        setSubNodes(newMap);
    }

    private void rotateRightToLeft(Map.Entry<Comparable, InnerNode> leftEntry, Map.Entry<Comparable, InnerNode> rightEntry) {
        InnerNode leftSubNode = leftEntry.getValue();
        InnerNode rightSubNode = rightEntry.getValue();

        TreeMap<Comparable, AbstractNode> newLeftSubNodeSubNodes = leftSubNode.duplicateMap();
        TreeMap<Comparable, AbstractNode> newRightSubNodeSubNodes = rightSubNode.duplicateMap();

        // re-index the left highest value under the split-key, which is moved down
        AbstractNode leftHighestValue = newLeftSubNodeSubNodes.get(BPlusTree.LAST_KEY);
        newLeftSubNodeSubNodes.put(leftEntry.getKey(), leftHighestValue);

        // remove right's lowest entry
        Map.Entry<Comparable, AbstractNode> rightLowestEntry = newRightSubNodeSubNodes.pollFirstEntry();

        // set its value on the left
        AbstractNode rightLowestValue = rightLowestEntry.getValue();
        newLeftSubNodeSubNodes.put(BPlusTree.LAST_KEY, rightLowestValue);
        rightLowestValue.setParent(leftSubNode);

        leftSubNode.setSubNodes(newLeftSubNodeSubNodes);
        rightSubNode.setSubNodes(newRightSubNodeSubNodes);

        // update the split-key to be the key we just removed from the right
        TreeMap<Comparable, AbstractNode> newMap = duplicateMap();
        newMap.remove(leftEntry.getKey());
        newMap.put(rightLowestEntry.getKey(), leftSubNode);
        setSubNodes(newMap);
    }

    @Override
    Map.Entry removeBiggestKeyValue() {
        throw new UnsupportedOperationException("not yet implemented: removeBiggestKeyValue from inner node");
    }

    @Override
    Map.Entry removeSmallestKeyValue() {
        throw new UnsupportedOperationException("not yet implemented: removeSmallestKeyValue from inner node");
    }

    @Override
    Comparable getSmallestKey() {
        throw new UnsupportedOperationException("not yet implemented: getSmallestKey from inner node");
    }

    @Override
    void addKeyValue(Map.Entry keyValue) {
        throw new UnsupportedOperationException("not yet implemented: addKeyValue to inner node should account for LAST_KEY ?!?");
    }

    @Override
    public Serializable get(Comparable key) {
        return findSubNode(key).get(key);
    }

    // travels to the leftmost leaf and goes from there;
    @Override
    public Serializable getIndex(int index) {
        return this.getSubNodes().firstEntry().getValue().getIndex(index);
    }

    // travels to the leftmost leaf and goes from there;
    @Override
    public AbstractNode removeIndex(int index) {
        return this.getSubNodes().firstEntry().getValue().removeIndex(index);
    }

    @Override
    public boolean containsKey(Comparable key) {
        return findSubNode(key).containsKey(key);
    }

    private AbstractNode findSubNode(Comparable key) {
        for (Map.Entry<Comparable, AbstractNode> subNode : this.getSubNodes().entrySet()) {
            Comparable splitKey = subNode.getKey();
            if (BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY.compare(splitKey, key) > 0) { // this will eventually be true because the LAST_KEY is greater than all
                return subNode.getValue();
            }
        }
        throw new RuntimeException("findSubNode() didn't find a suitable sub-node!?");
    }

    @Override
    int shallowSize() {
        return this.getSubNodes().size();
    }

    @Override
    public int size() {
        int total = 0;
        for (AbstractNode subNode : this.getSubNodes().values()) {
            total += subNode.size();
        }
        return total;
    }

    @Override
    Iterator<? extends Comparable> keysIterator() {
        return this.getSubNodes().firstEntry().getValue().keysIterator();
    }

    @Override
    public Iterator iterator() {
        return this.getSubNodes().firstEntry().getValue().iterator();
    }

    @Override
    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
        StringBuilder str = new StringBuilder();
        StringBuilder spaces = BPlusTree.spaces(level);
        str.append(spaces);
        str.append("[" + (dumpNodeIds ? this : "") + ": ");

        for (Map.Entry<Comparable, AbstractNode> entry : this.getSubNodes().entrySet()) {
            Comparable key = entry.getKey();
            AbstractNode value = entry.getValue();
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

    @Override
    Collection<? extends Comparable> getKeys() {
        return Collections.emptySet();
    }

}