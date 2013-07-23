package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeafNodeArray extends LeafNodeArray_Base {
    private static final Logger logger = LoggerFactory.getLogger(LeafNodeArray.class);

    public LeafNodeArray() {
        setEntries(new DoubleArray<Serializable>(Serializable.class));
    }

    private LeafNodeArray(DoubleArray<Serializable> entries) {
        setEntries(entries);
    }

    @Override
    public AbstractNodeArray insert(Comparable key, Serializable value) {
        DoubleArray<Serializable> localArr = justInsert(key, value);

        if (localArr == null) {		// no insertion occurred
            return null;	// insert will return false
        }
        if (localArr.length() <= BPlusTreeArray.MAX_NUMBER_OF_ELEMENTS) { // it still fits :-)
            return getRoot();
        } else { // must split this node
            // find middle position
            Comparable keyToSplit = localArr.findRightMiddlePosition();

            // split node in two
            LeafNodeArray leftNode = new LeafNodeArray(localArr.leftPart(BPlusTreeArray.LOWER_BOUND + 1));
            LeafNodeArray rightNode = new LeafNodeArray(localArr.rightPart(BPlusTreeArray.LOWER_BOUND + 1));
            fixLeafNodeArraysListAfterSplit(leftNode, rightNode);

            // propagate split to parent
            if (getParent() == null) {  // make new root node
                InnerNodeArray newRoot = new InnerNodeArray(leftNode, rightNode, keyToSplit);
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
        // this check suffices because we do not allow null values
        if (currentValue == value) {
            logger.trace("Existing key. No change required");
            return null;
        } else {
            logger.trace("Will add new entry. Must duplicate 'entries'.");
            DoubleArray<Serializable> newArr = localEntries.addKeyValue(key, value);
            setEntries(newArr);
            return newArr;
        }
    }

    private void fixLeafNodeArraysListAfterSplit(LeafNodeArray leftNode, LeafNodeArray rightNode) {
	LeafNodeArray myPrevious = this.getPrevious();
        LeafNodeArray myNext = this.getNext();

        if (myPrevious != null) {
            leftNode.setPrevious(myPrevious);
        }
        rightNode.setPrevious(leftNode);
        if (myNext != null) {
            rightNode.setNext(myNext);
        }
        leftNode.setNext(rightNode);
        
        if (myPrevious != null) {
            myPrevious.setNext(leftNode);
        }
        if (myNext != null) {
            myNext.setPrevious(rightNode);
        }
    }

    @Override
    public AbstractNodeArray remove(Comparable key) {
        DoubleArray<Serializable> localArr = justRemove(key);

        if (localArr == null) {
            return null;	// remove will return false
        }
        if (getParent() == null) {
            return this;
        } else {
            // if the removed key was the first we need to replace it in some parent's index
            Comparable replacementKey = getReplacementKeyIfNeeded(key);

            if (localArr.length() < BPlusTreeArray.LOWER_BOUND) {
                return getParent().underflowFromLeaf(key, replacementKey);
            } else if (replacementKey != null) {
                return getParent().replaceDeletedKey(key, replacementKey);
            } else {
                return getParent().getRoot();  // maybe a tiny faster than just getRoot() ?!
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

    @Override
    DoubleArray<Serializable>.KeyVal removeBiggestKeyValue() {
        DoubleArray<Serializable> entries = this.getEntries();
        DoubleArray<Serializable>.KeyVal lastEntry = entries.getBiggestKeyValue();
        setEntries(entries.removeBiggestKeyValue());
        return lastEntry;
    }

    @Override
    DoubleArray<Serializable>.KeyVal removeSmallestKeyValue() {
        DoubleArray<Serializable> entries = this.getEntries();
        DoubleArray<Serializable>.KeyVal firstEntry = entries.getSmallestKeyValue();
        setEntries(entries.removeSmallestKeyValue());
        return firstEntry;
    }

    @Override
    Comparable getSmallestKey() {
        return this.getEntries().firstKey();
    }

    @Override
    void addKeyValue(DoubleArray.KeyVal keyValue) {
        setEntries(this.getEntries().addKeyValue(keyValue));
    }

    @Override
    void mergeWithLeftNode(AbstractNodeArray leftNode, Comparable splitKey) {
        LeafNodeArray left = (LeafNodeArray) leftNode; // this node does not know how to merge with another kind
        setEntries(getEntries().mergeWith(left.getEntries()));

        LeafNodeArray nodeBefore = left.getPrevious();
        this.setPrevious(nodeBefore);
        if (nodeBefore != null) {
            nodeBefore.setNext(this);
        }

        // no need to update parents, because they are always the same for the two merging leaf nodes
        assert (this.getParent() == leftNode.getParent());
    }

    @Override
    public Serializable get(Comparable key) {
        return this.getEntries().get(key);
    }

    @Override
    public Serializable getIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (index < shallowSize()) { // the required position is here
            return this.getEntries().values[index];
        } else {
            LeafNodeArray next = this.getNext();
            if (next == null) {
                throw new IndexOutOfBoundsException();
            }
            return next.getIndex(index - shallowSize());
        }
    }

    @Override
    public AbstractNodeArray removeIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (index < shallowSize()) { // the required position is here
            return remove(this.getEntries().keys[index]);
        } else {
            LeafNodeArray next = this.getNext();
            if (next == null) {
                throw new IndexOutOfBoundsException();
            }
            return next.removeIndex(index - shallowSize());
        }
    }

    @Override
    public boolean containsKey(Comparable key) {
        return this.getEntries().containsKey(key);
    }

    @Override
    int shallowSize() {
        return this.getEntries().length();
    }

    @Override
    public int size() {
        return this.getEntries().length();
    }

    @Override
    public Iterator<Serializable> iterator() {
        return new LeafNodeArrayIterator(this);
    }

    private class LeafNodeArrayIterator implements Iterator<Serializable> {
        private int index;
        private Serializable[] values;
        private LeafNodeArray current;

        LeafNodeArrayIterator(LeafNodeArray LeafNodeArray) {
            this.index = 0;
            this.values = LeafNodeArray.getEntries().values;
            this.current = LeafNodeArray;
        }

        @Override
        public boolean hasNext() {
            if (index < values.length) {
                return true;
            } else {
                return this.current.getNext() != null;
            }
        }

        @Override
        public Serializable next() {
            if (index >= values.length) {
                LeafNodeArray nextNode = this.current.getNext();
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

        @Override
        public void remove() {
            throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
        }

    }

    @Override
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
