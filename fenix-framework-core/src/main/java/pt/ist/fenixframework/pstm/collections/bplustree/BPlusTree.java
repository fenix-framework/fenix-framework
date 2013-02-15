package pt.ist.fenixframework.pstm.collections.bplustree;

import java.util.Comparator;
import java.util.Iterator;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.NoDomainMetaObjects;

@NoDomainMetaObjects
public class BPlusTree<T extends DomainObject> extends BPlusTree_Base implements Iterable<T> {

    // public  BPlusTree() {
    //     super();
    // }

    // static final Comparable LAST_KEY = new Comparable() {
    // 	    public int compareTo(Object obj) {
    // 		if (LAST_KEY == obj) return 0;
    // 		else return 1;
    // 	    }

    // 	    public boolean equals(Object obj) {
    // 		return (LAST_KEY == obj);
    // 	    }

    // 	    public String toString() {
    // 		return "LK";
    // 	    }
    // 	};
    static final Long LAST_KEY = Long.MAX_VALUE;

    // public static <K> K lastKey() {
    //     return (K)LAST_KEY;
    // }

    static final Comparator COMPARATOR_SUPPORTING_LAST_KEY = new Comparator<Comparable>() {
        // Others don't know how to compare with LAST_KEY
        @Override
        public int compare(Comparable o1, Comparable o2) {
            if (o1 == BPlusTree.LAST_KEY) {
                return o1.compareTo(o2);
            } else {
                return -o2.compareTo(o1);
            }
        }
    };

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

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    public void insert(Long key, T value) {
        AbstractNode rootNode = this.getRoot();
        AbstractNode resultNode = rootNode.insert(key, value);
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }
    }

    /** Removes the element with the given key */
    public void remove(Long key) {
        AbstractNode rootNode = this.getRoot();
        AbstractNode resultNode = rootNode.remove(key);
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }
    }

    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    public T get(Long key) {
        return ((AbstractNode<T>) this.getRoot()).get(key);
    }

    public T getIndex(int index) {
        return ((AbstractNode<T>) this.getRoot()).getIndex(index);
    }

    public T removeIndex(int index) {
        T value = getIndex(index);

        AbstractNode<T> rootNode = this.getRoot();
        AbstractNode<T> resultNode = rootNode.removeIndex(index);
        if (rootNode != resultNode) {
            this.setRoot(resultNode);
        }

        return value;
    }

    /** Returns <code>true</code> if this map contains a mapping for the specified key. */
    public boolean containsKey(Long key) {
        return this.getRoot().containsKey(key);
    }

    /** Returns the number of key-value mappings in this map */
    public int size() {
        return this.getRoot().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Completly deletes this <code>BPlusTree</code>, and all its {@link AbstractNode}s. Does not delete any {@link DomainObject}
     * contained
     * in the tree.
     */
    public void delete() {
        AbstractNode<T> rootNode = getRoot();
        removeRoot();
        rootNode.delete();
        deleteDomainObject();
    }

    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
        return this.getRoot().dump(level, dumpKeysOnly, dumpNodeIds);
    }

    @Override
    public Iterator iterator() {
        return this.getRoot().iterator();
    }

    public boolean myEquals(BPlusTree other) {
        Iterator<DomainObject> it1 = this.iterator();
        Iterator<DomainObject> it2 = other.iterator();

        while (it1.hasNext() && it2.hasNext()) {
            DomainObject o1 = it1.next();
            DomainObject o2 = it2.next();

            if (!((o1 == null && o2 == null) || (o1.equals(o2)))) {
                return false;
            }
        }
        return true;
    }
}
