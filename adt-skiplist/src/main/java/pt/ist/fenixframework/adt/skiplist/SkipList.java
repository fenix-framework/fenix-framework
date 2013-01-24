package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import pt.ist.fenixframework.dml.runtime.DomainBasedSet;

public class SkipList<T extends Serializable> extends SkipList_Base implements DomainBasedSet<T>{

    private transient final static double probability = 0.25;
    private transient final static int maxLevel = 32;
    private transient final static ThreadLocal<Random> random = new ThreadLocal<Random>() {
	protected Random initialValue() {
	    return new Random();
	}
    };
    private transient final static Comparable minValue = new Comparable() {
	public int compareTo(Object arg0) {
	    return -1;
	}
    };
    private transient final static Comparable maxValue = new Comparable() {
	public int compareTo(Object o) {
	    return 1;
	}
    };

    public SkipList() {
	super();
	setLevel(0);
	SkipListNode<T> head = new SkipListNode<T>(maxLevel, minValue, null);
	SkipListNode<T> tail = new SkipListNode<T>(maxLevel, maxValue, null);
	setHead(head);
	for (int i = 0; i <= maxLevel; i++) {
	    head.setForward(i, tail);
	}
    }

    protected int randomLevel() {
	int l = 0;
	while (l < maxLevel && random.get().nextDouble() < probability)
	    l++;
	return l;
    }

    public boolean insert(Comparable toInsert, T value) {
	boolean result;

	SkipListNode[] update = new SkipListNode[maxLevel + 1];
	SkipListNode node = getHead();
	int level = getLevel();

	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNode next = node.getForward(i);
	    while ((oid = next.getKeyValue().key).compareTo(toInsert) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	    update[i] = node;
	}
	node = node.getForward(0);

	if (node.getKeyValue().key.compareTo(toInsert) == 0) {
	    result = false;
	} else {
	    int newLevel = randomLevel();
	    if (newLevel > level) {
		for (int i = level + 1; i <= level; i++)
		    update[i] = getHead();
		setLevel(level);
	    }
	    node = new SkipListNode<T>(level, toInsert, value);
	    for (int i = 0; i <= level; i++) {
		node.setForward(i, update[i].getForward(i));
		update[i].setForward(i, node);
	    }
	    result = true;
	}

	return result;
    }

    public boolean removeKey(Comparable toRemove) {
	boolean result;

	SkipListNode[] update = new SkipListNode[maxLevel + 1];
	SkipListNode node = getHead();

	int level = getLevel();

	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNode next = node.getForward(i);
	    while ((oid = next.getKeyValue().key).compareTo(toRemove) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	    update[i] = node;
	}
	node = node.getForward(0);

	if (node.getKeyValue().key.compareTo(toRemove) != 0) {
	    result = false;
	} else {
	    for (int i = 0; i <= level; i++) {
		if (update[i].getForward(i).getOid().compareTo(node.getOid()) == 0)
		    update[i].setForward(i, node.getForward(i));
	    }
	    while (level > 0 && getHead().getForward(level).getForward(0) == null) {
		level--;
		setLevel(level);
	    }
	    result = true;
	}

	return result;
    }

    public boolean containsKey(Comparable key) {
	boolean result;

	SkipListNode node = getHead();
	int level = getLevel();

	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNode next = node.getForward(i);
	    while ((oid = next.getKeyValue().key).compareTo(key) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	}
	node = node.getForward(0);

	result = (node.getKeyValue().key.compareTo(key) == 0);

	return result;
    }

    public Iterator<T> iterator() {
	return new Iterator<T>() {

	    private SkipListNode iter = getHead().getForward(0); // skip head tomb

	    @Override
	    public boolean hasNext() {
		return iter != null && iter.getKeyValue().key.compareTo(maxValue) != 0;
	    }

	    @Override
	    public T next() {
		if (iter == null || iter.getKeyValue().key.compareTo(maxValue) == 0) {
		    throw new NoSuchElementException();
		}
		Object value = iter.getKeyValue().value;
		iter = iter.getForward(0);
		return (T)value;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	    }

	};
    }

    @Override
    public boolean add(Comparable key, T e) {
        return insert(key, e);
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
    public int size() {
	Iterator<T> iter = this.iterator();
	int size = 0;
	while (iter.hasNext()) {
	    size++;
	    iter.next();
	}
	return size;
    }

}
