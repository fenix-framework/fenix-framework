package pt.ist.fenixframework.core.adt.bplustree;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

// import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.core.AbstractDomainObject;

public class BPlusTree<T extends AbstractDomainObject> extends BPlusTree_Base {
    
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


    /* Special last key */
    private static class ComparableLastKey implements Comparable, Serializable {
        public int compareTo(Object c) {
            if (c == null) {
                // because comparing the other way around would cause a NullPointerException
                throw new NullPointerException();
            } else if (c == this) {
                return 0;
            }
            return 1; // this key is always greater than any other, except itself.
        }
            
        public String toString() {
            return "LAST_KEY";
        }
    }
    static final Comparable LAST_KEY = new ComparableLastKey();

    /* Special comparator that takes into account LAST_KEY */
    private static class ComparatorSupportingLastKey implements Comparator<Comparable>, Serializable {
        // only LAST_KEY knows how to compare itself with others, so we must check for it before
        // delegating the comparison to the Comparables.
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

    /** Inserts the given key-value pair and returns the (possibly new) root node */
    // public void insert(Comparable key, T value) {
    public void insert(T value) {
        if (value == null) {
            throw new UnsupportedOperationException("This B+Tree does not support nulls");
        }
	AbstractNode rootNode = this.getRoot();
	AbstractNode resultNode = rootNode.insert(value.getOid(), value);
	if (rootNode != resultNode) {
	    this.setRoot(resultNode);
	}
    }

    // /** Removes the given element */
    // public void remove(T obj) {
    //     remove(obj.getOid());
    // }

    /** Removes the element with the given key */
    public void remove(Comparable key) {
	AbstractNode rootNode = this.getRoot();
	AbstractNode resultNode = rootNode.remove(key);
	if (rootNode != resultNode) {
	    this.setRoot(resultNode);
	}
    }

    /** Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. */
    public T get(Comparable key) {
	return ((AbstractNode<T>)this.getRoot()).get(key);
    }

    public T getIndex(int index) {
	return ((AbstractNode<T>)this.getRoot()).getIndex(index);
    }

    public T removeIndex(int index) {
	T value = getIndex(index);

	AbstractNode rootNode = this.getRoot();
	AbstractNode resultNode = rootNode.removeIndex(index);
	if (rootNode != resultNode) {
	    this.setRoot(resultNode);
	}

	return value;
    }

    /** Returns <code>true</code> if this map contains a mapping for the specified key.  */
    public boolean containsKey(Comparable key) {
	return this.getRoot().containsKey(key);
    }

    /** Returns the number os key-value mappings in this map */
    public int size() {
	return this.getRoot().size();
    }

    public String dump(int level, boolean dumpKeysOnly, boolean dumpNodeIds) {
	return this.getRoot().dump(level, dumpKeysOnly, dumpNodeIds);
    }

    public Iterator iterator() {
	return this.getRoot().iterator();
    }
    
    public boolean myEquals(BPlusTree other) {
	Iterator<AbstractDomainObject> it1 = this.iterator();
	Iterator<AbstractDomainObject> it2 = other.iterator();
	
	while (it1.hasNext() && it2.hasNext()) {
	    AbstractDomainObject o1 = it1.next();
	    AbstractDomainObject o2 = it2.next();

	    if (!((o1 == null && o2 == null) || (o1.equals(o2)))) {
		    return false;
	    }
	}
	return true;
    }
}
