package pt.ist.fenixframework.core.adt.skiplist;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import pt.ist.fenixframework.core.AbstractDomainObject;

public class SkipListUnsafe<T extends AbstractDomainObject> extends SkipListUnsafe_Base implements Set<T>{

    private transient final static double probability = 0.25;
    private transient final static int maxLevel = 32;
    private transient final static ThreadLocal<Random> random = new ThreadLocal<Random>() {
	protected Random initialValue() {
	    return new Random();
	}
    };
    private transient final static AbstractDomainObject minValue = new AbstractDomainObject() {
	protected void ensureOid() {};
	public Comparable getOid() {
	    return Long.MIN_VALUE;
	};
    };
    private transient final static AbstractDomainObject maxValue = new AbstractDomainObject() {
	protected void ensureOid() {};
	public Comparable getOid() {
	    return Long.MAX_VALUE;
	};
    };

    public SkipListUnsafe() {
	super();
	setLevel(0);
	SkipListNodeUnsafe head = new SkipListNodeUnsafe(maxLevel, minValue);
	SkipListNodeUnsafe tail = new SkipListNodeUnsafe(maxLevel, maxValue);
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

    public boolean insert(T value) {
	boolean result;

	SkipListNodeUnsafe[] update = new SkipListNodeUnsafe[maxLevel + 1];
	SkipListNodeUnsafe node = getHeadUnsafe();
	int level = getLevelUnsafe();

	Comparable toInsert = value.getOid();
	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNodeUnsafe next = node.getForward(i);
	    while ((oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	    update[i] = node;
	}
	node.registerGetForward();
	node = node.getForward(0);

	if (node.getValueUnsafe().getOid().compareTo(toInsert) == 0) {
	    result = false;
	} else {
	    int newLevel = randomLevel();
	    if (newLevel > level) {
		for (int i = level + 1; i <= level; i++)
		    update[i] = getHeadUnsafe();
		registerGetLevel();
		setLevel(level);
	    }
	    node = new SkipListNodeUnsafe(level, value);
	    for (int i = 0; i <= level; i++) {
		node.setForward(i, update[i].getForward(i));
		update[i].registerGetForward();
		update[i].setForward(i, node);
	    }
	    result = true;
	}

	return result;
    }

    public boolean remove(T value) {
	boolean result;

	SkipListNodeUnsafe[] update = new SkipListNodeUnsafe[maxLevel + 1];
	SkipListNodeUnsafe node = getHeadUnsafe();

	int level = getLevelUnsafe();

	Comparable toInsert = value.getOid();
	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNodeUnsafe next = node.getForward(i);
	    while ((oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	    update[i] = node;
	}
	node.registerGetForward();
	node = node.getForward(0);

	if (node.getValueUnsafe().getOid().compareTo(toInsert) != 0) {
	    result = false;
	} else {
	    for (int i = 0; i <= level; i++) {
		update[i].registerGetForward();
		if (update[i].getForward(i).getOid().compareTo(node.getOid()) == 0) {
		    node.registerGetForward();
		    update[i].setForward(i, node.getForward(i));
		}
	    }
	    boolean changedLevel = false;
	    while (level > 0 && getHeadUnsafe().getForward(level).getForward(0) == null) {
		changedLevel = true;
		level--;
	    }
	    if (changedLevel) {
		registerGetLevel();
		setLevel(level);
	    }
	    result = true;
	}

	return result;
    }

    public boolean contains(T value) {
	boolean result;

	SkipListNodeUnsafe node = getHeadUnsafe();
	int level = getLevelUnsafe();

	Comparable toInsert = value.getOid();
	Comparable oid = node.getOid();

	for (int i = level; i >= 0; i--) {
	    SkipListNodeUnsafe next = node.getForward(i);
	    while ((oid = next.getValueUnsafe().getOid()).compareTo(toInsert) < 0) {
		node = next;
		next = node.getForward(i);
	    }
	}
	node.registerGetForward();
	node = node.getForward(0);

	result = (node.getValue().getOid().compareTo(toInsert) == 0);

	return result;
    }

    public Iterator<T> iterator() {
	return new Iterator<T>() {

	    private SkipListNodeUnsafe iter = getHeadUnsafe().getForward(0); // skip head tomb

	    @Override
	    public boolean hasNext() {
		return iter != null && iter.getValue().getOid().compareTo(maxValue.getOid()) != 0;
	    }

	    @Override
	    public T next() {
		if (iter == null || iter.getValue().getOid().compareTo(maxValue.getOid()) == 0) {
		    throw new NoSuchElementException();
		}
		Object value = iter.getValue();
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
    public boolean add(T e) {
	return insert(e);
    }

    @Override
    public boolean remove(Object o) {
	if (! (o instanceof AbstractDomainObject)) {
	    return false;
	}
	return remove((T)o);
    }

    @Override
    public boolean contains(Object o) {
	if (! (o instanceof AbstractDomainObject)) {
	    return false;
	}
	return contains((T)o);
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

    /* The following methods are not needed at the moment but we need to implement Set */

    @Override
    public boolean addAll(Collection<? extends T> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
	throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
	throw new UnsupportedOperationException();
    }
}
