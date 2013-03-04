package pt.ist.fenixframework.adt.bplustree;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import pt.ist.fenixframework.dml.runtime.DomainBasedMap;

/**
 * Implementation of a persistence-independent B+Tree. This implementation is modelled in DML and
 * can be used with any backend. This B+Tree can store any value (except nulls) associated with any
 * key as long as the following restrictions are followed: Both the key and the value need to be {@link java.io.Serializable}; the
 * key also needs to be {@link Comparable}; and keys must
 * comparable to each other (e.g. the same BPlusTree instance cannot simultaneously support keys of
 * type Integer and String).
 */
public class BPlusTree<T extends Serializable> extends BPlusTree_Base implements DomainBasedMap<T> {

    /* Special last key */
    private static final class ComparableLastKey implements Comparable, Serializable {
        private static final Serializable LAST_KEY_SERIALIZED_FORM = new Serializable() {
            protected Object readResolve() throws ObjectStreamException {
                return LAST_KEY;
            }
        };

        @Override
        public int compareTo(Object c) {
            if (c == null) {
                // because comparing the other way around would cause a NullPointerException
                throw new NullPointerException();
            } else if (c == this) {
                return 0;
            }
            return 1; // this key is always greater than any other, except itself.
        }

        @Override
        public String toString() {
            return "LAST_KEY";
        }

        // This object's serialization is special.  We need to ensure that two deserializations of
        // the same object will provide the same instance, so that we can compare using == in the
        // ComparatorSupportingLastKey
        protected Object writeReplace() throws ObjectStreamException {
            return LAST_KEY_SERIALIZED_FORM;
        }
    }

    static final Comparable LAST_KEY = new ComparableLastKey();

    /* Special comparator that takes into account LAST_KEY */
    private static class ComparatorSupportingLastKey implements Comparator<Comparable>, Serializable {
        // only LAST_KEY knows how to compare itself with others, so we must check for it before
        // delegating the comparison to the Comparables.
        @Override
        public int compare(Comparable o1, Comparable o2) {
            if (o1 == LAST_KEY) {
                return o1.compareTo(o2);
            } else if (o2 == LAST_KEY) {
                return -o2.compareTo(o1);
            }
            // neither is LAST_KEY.  Compare them normally.
            return o1.compareTo(o2);
        }
    }

    static final Comparator COMPARATOR_SUPPORTING_LAST_KEY = new ComparatorSupportingLastKey();

    // The minimum lower bound is 2 (two).  Only edit this.  The other values are derived.
    static final int LOWER_BOUND = 100;
    // The LAST_KEY takes an entry but will still maintain the lower bound
    static final int LOWER_BOUND_WITH_LAST_KEY = LOWER_BOUND + 1;
    // The maximum number of keys in a node NOT COUNTING with the special LAST_KEY. This number should be a multiple of 2.
    static final int MAX_NUMBER_OF_KEYS = 2 * LOWER_BOUND;
    static final int MAX_NUMBER_OF_ELEMENTS = MAX_NUMBER_OF_KEYS + 1;

    static StringBuilder spaces(int level) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < level; i++) {
            str.append(' ');
        }
        return str;
    }

    // non-static part start here

    public BPlusTree() {
        initRoot();
    }

    private void initRoot() {
        this.setRoot(new LeafNode());
    }

    /** Inserts the given key-value pair, overwriting any previous entry for the same key */
    public boolean insert(Comparable key, T value) {
        if (value == null) {
            throw new UnsupportedOperationException("This B+Tree does not support nulls");
        }
        AbstractNode rootNode = this.getRoot();
        AbstractNode resultNode = rootNode.insert(key, value);

        if (resultNode == null) {
            return false;
        }
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }
        return true;
    }

    /** Removes the element with the given key */
    public boolean removeKey(Comparable key) {
        AbstractNode rootNode = this.getRoot();
        AbstractNode resultNode = rootNode.remove(key);

        if (resultNode == null) {
            return false;
        }
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }
        return true;
    }

    /**
     * Returns the value to which the specified key is mapped, or <code>null</code> if this map
     * contains no mapping for the key.
     */
    @Override
    public T get(Comparable key) {
        return ((AbstractNode<T>) this.getRoot()).get(key);
    }

    /**
     * Return the value at the index-th position (zero-based).
     */
    public T getIndex(int index) {
        return ((AbstractNode<T>) this.getRoot()).getIndex(index);
    }

    /**
     * Remove and return the value at the index-th position (zero-based).
     */
    public T removeIndex(int index) {
        T value = getIndex(index);

        AbstractNode rootNode = this.getRoot();
        AbstractNode resultNode = rootNode.removeIndex(index);
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }

        return value;
    }

    /** Returns <code>true</code> if this map contains a mapping for the specified key. */
    public boolean containsKey(Comparable key) {
        return this.getRoot().containsKey(key);
    }

    /** Returns the number of key-value mappings in this map */
    @Override
    public int size() {
        return this.getRoot().size();
    }

    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
        return this.getRoot().dump(level, dumpKeysOnly, dumpNodeIds);
    }

    @Override
    public Iterator iterator() {
        return this.getRoot().iterator();
    }

    public boolean myEquals(BPlusTree other) {
        Iterator<T> it1 = this.iterator();
        Iterator<T> it2 = other.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            T o1 = it1.next();
            T o2 = it2.next();

            if (!((o1 == null && o2 == null) || (o1.equals(o2)))) {
                return false;
            }
        }
        return true;
    }

    /** Returns the set of keys mapped by this tree */
    public <T extends Comparable> Set<T> getKeys() {
        Set<T> keys = new LinkedHashSet<T>();
        Iterator<T> iter = this.getRoot().keysIterator();
        while (iter.hasNext()) {
            T key = iter.next();
            keys.add(key);
        }
        return keys;
    }

    @Override
    public boolean remove(Comparable key) {
        return removeKey(key);
    }

    @Override
    public boolean contains(Comparable key) {
        return containsKey(key);
    }

    @Override
    public void put(Comparable key, T value) {
        insert(key, value);
    }

    @Override
    public boolean putIfMissing(Comparable key, T value) {
        return insert(key, value);
    }
}
