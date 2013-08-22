package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import eu.cloudtm.LocalityHints;

import pt.ist.fenixframework.dml.runtime.DomainBasedMap;

public class SkipList<T extends Serializable> extends SkipList_Base implements DomainBasedMap<T>{

    private transient final static double probability = 0.25;
    private transient final static int maxLevel = 32;
    private transient final static ThreadLocal<Random> random = new ThreadLocal<Random>() {
	protected Random initialValue() {
	    return new Random();
	}
    };
    
    private transient final static Comparable MIN_VALUE = new TombKey(-1);
    private transient final static Comparable MAX_VALUE = new TombKey(1);
    
    public SkipList() {
	super();
	setLevel(0);
	SkipListNode<T> head = new SkipListNode<T>(maxLevel, MIN_VALUE, null);
	SkipListNode<T> tail = new SkipListNode<T>(maxLevel, MAX_VALUE, null);
	setHead(head);
	for (int i = 0; i <= maxLevel; i++) {
	    head.setForward(i, tail);
	}
    }
    
    public SkipList(LocalityHints hints) {
	super(hints);
	setLevel(0);
	SkipListNode<T> head = new SkipListNode<T>(maxLevel, MIN_VALUE, null);
	SkipListNode<T> tail = new SkipListNode<T>(maxLevel, MAX_VALUE, null);
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

    @Override
    public T get(Comparable key) {
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

	if (node.getKeyValue().key.compareTo(key) == 0) {
	    return (T) node.getKeyValue().value;
	} else {
	    return null;
	}
    }
    
    @Override
    public T getCached(boolean forceMiss, Comparable key) {
	boolean result;

	SkipListNode node = getHeadCached(forceMiss);
	int level = getLevelCached(forceMiss);

	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNode next = node.getForwardCached(forceMiss, i);
	    while ((oid = next.getKeyValueCached(forceMiss).key).compareTo(key) < 0) {
		node = next;
		next = node.getForwardCached(forceMiss, i);
	    }
	}
	node = node.getForwardCached(forceMiss, 0);

	if (node.getKeyValueCached(forceMiss).key.compareTo(key) == 0) {
	    return (T) node.getKeyValueCached(forceMiss).value;
	} else {
	    return null;
	}
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
	return get(key) != null;
    }

    public Iterator<T> iterator() {
	return new Iterator<T>() {

	    private SkipListNode iter = getHead().getForward(0); // skip head tomb

	    @Override
	    public boolean hasNext() {
		return iter.getForward(0) != null;
	    }

	    @Override
	    public T next() {
		if (iter.getForward(0) == null) {
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

    public Iterator<T> iteratorCached(final boolean forceMiss) {
	return new Iterator<T>() {

	    private SkipListNode iter = getHeadCached(forceMiss).getForwardCached(forceMiss, 0); // skip head tomb

	    @Override
	    public boolean hasNext() {
		return iter.getForwardCached(forceMiss, 0) != null;
	    }

	    @Override
	    public T next() {
		if (iter.getForwardCached(forceMiss, 0) == null) {
		    throw new NoSuchElementException();
		}
		Object value = iter.getKeyValueCached(forceMiss).value;
		iter = iter.getForwardCached(forceMiss, 0);
		return (T)value;
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException("This implementation does not allow element removal via the iterator");
	    }

	};
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
    
    @Override
    public void put(Comparable key, T value) {
	insert(key, value);
    }

    @Override
    public boolean putIfMissing(Comparable key, T value) {
	return insert(key, value);
    }
}
